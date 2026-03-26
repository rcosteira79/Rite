package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.habitlock.di.AppScope
import com.ricardocosteira.habitlock.domain.models.CompletionSource
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.domain.models.UserStrictnessSettings
import com.ricardocosteira.habitlock.domain.models.motivationalTitleForDate
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.domain.usecases.CompleteHabit
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.habitlock.domain.usecases.ProcessEndOfDay
import com.ricardocosteira.habitlock.domain.usecases.SkipHabit
import com.ricardocosteira.habitlock.domain.usecases.SkipLockedException
import com.ricardocosteira.habitlock.domain.usecases.UndoHabit
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import com.ricardocosteira.habitlock.presentation.models.mapToTodayHabitUiModel
import com.ricardocosteira.habitlock.util.toLocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject
import kotlin.time.Clock

/**
 * Scoped to the application lifetime via [AppScope] rather than a
 * [androidx.lifecycle.ViewModelStoreOwner] because this app uses a single-activity
 * architecture and all ViewModels are obtained directly from the DI component.
 * Composable-scoped ViewModels (rememberViewModelStoreOwner) would be more
 * semantically correct but require lifecycle 2.11.0-alpha02 and a custom
 * ViewModelProvider.Factory bridge. Revisit when that API stabilises.
 */
@AppScope
@Inject
class TodayViewModel(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val generateDailyHabits: GenerateDailyHabits,
    private val processEndOfDay: ProcessEndOfDay,
    private val completeHabit: CompleteHabit,
    private val skipHabit: SkipHabit,
    private val undoHabit: UndoHabit,
) : ViewModel() {
    private val _state = MutableStateFlow(TodayState())
    val state: StateFlow<TodayState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TodayEvent>()
    val events: SharedFlow<TodayEvent> = _events.asSharedFlow()

    init {
        loadTodayHabits()
    }

    fun loadTodayHabits() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Process end of day first
                processEndOfDay.execute()

                // Generate today's habits
                generateDailyHabits.execute()

                // Load user for settings
                val user = userRepository.getUser()

                // Check for timezone warning
                if (user?.previousTimezone != null) {
                    _state.update {
                        it.copy(
                            showTimezoneWarning = true,
                            previousTimezone = user.previousTimezone.id,
                        )
                    }
                }

                // Derive timezone and strictness preset
                val userTimezone: TimeZone = user?.timezone ?: TimeZone.currentSystemDefault()

                val strictnessPreset: StrictnessPreset? =
                    user?.let {
                        val settings =
                            UserStrictnessSettings(
                                undoPolicy = it.undoPolicy,
                                maxSnoozesPerHabitPerDay = it.maxSnoozesPerHabitPerDay,
                                maxConsecutiveSkips = it.maxConsecutiveSkips,
                                maxSnoozeDurationMinutes = it.maxSnoozeDurationMinutes,
                            )
                        StrictnessPreset.fromSettings(settings)
                    }

                // Get today's instances
                val today = Clock.System.now().toLocalDate(userTimezone)
                val instances = habitInstanceRepository.getInstancesForDate(today)

                val motivationalTitle: String = motivationalTitleForDate(today)

                // Map to UI models
                val habits =
                    instances.mapNotNull { instance ->
                        val habit = habitRepository.getHabitById(instance.habitId) ?: return@mapNotNull null
                        val schedule = habitRepository.getScheduleForHabit(habit.id) ?: return@mapNotNull null
                        mapToTodayHabitUiModel(
                            instance = instance,
                            habit = habit,
                            schedule = schedule,
                            maxConsecutiveSkips = user?.maxConsecutiveSkips,
                            userTimezone = userTimezone,
                        )
                    }

                val counts = habits.computeCounts()

                _state.update {
                    it.copy(
                        habits = habits,
                        isLoading = false,
                        pendingCount = counts.pendingCount,
                        dailyResolved = counts.dailyResolved,
                        dailyTotal = counts.dailyTotal,
                        motivationalTitle = motivationalTitle,
                        strictnessPreset = strictnessPreset,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                    )
                }
            }
        }
    }

    fun completeHabit(instanceId: String) {
        viewModelScope.launch {
            val habit = _state.value.habits.find { it.instanceId == instanceId } ?: return@launch

            if (habit.type == HabitType.QUANTITATIVE) {
                _state.update { it.copy(showQuantitativeInputFor = instanceId) }
                return@launch
            }

            val result = completeHabit.executeBinary(instanceId, CompletionSource.IN_APP)

            result.fold(
                onSuccess = {
                    loadTodayHabits()
                    _events.emit(TodayEvent.HabitCompleted)
                },
                onFailure = { error ->
                    _events.emit(TodayEvent.ShowError(error.message))
                },
            )
        }
    }

    fun completeQuantitativeHabit(
        instanceId: String,
        value: Int,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(showQuantitativeInputFor = null) }

            val result =
                completeHabit.executeQuantitative(
                    instanceId = instanceId,
                    deltaValue = value,
                    source = CompletionSource.IN_APP,
                )

            result.fold(
                onSuccess = { updatedInstance ->
                    loadTodayHabits()
                    if (updatedInstance.isQuantitativeComplete()) {
                        _events.emit(TodayEvent.HabitCompleted)
                    } else {
                        _events.emit(TodayEvent.ProgressAdded)
                    }
                },
                onFailure = { error ->
                    _events.emit(TodayEvent.ShowError(error.message))
                },
            )
        }
    }

    fun incrementHabitProgress(instanceId: String) {
        viewModelScope.launch {
            val habit: TodayHabitUiModel =
                _state.value.habits.find { it.instanceId == instanceId } ?: return@launch

            val result: Result<HabitInstance> =
                completeHabit.executeQuantitative(
                    instanceId = instanceId,
                    deltaValue = habit.defaultIncrement,
                    source = CompletionSource.IN_APP,
                )

            result.fold(
                onSuccess = { updatedInstance: HabitInstance ->
                    loadTodayHabits()
                    if (updatedInstance.isQuantitativeComplete()) {
                        _events.emit(TodayEvent.HabitCompleted)
                    } else {
                        _events.emit(TodayEvent.ProgressAdded)
                    }
                },
                onFailure = { error: Throwable ->
                    _events.emit(TodayEvent.ShowError(error.message))
                },
            )
        }
    }

    fun showQuantitativeInput(instanceId: String) {
        _state.update { it.copy(showQuantitativeInputFor = instanceId) }
    }

    fun dismissQuantitativeInput() {
        _state.update { it.copy(showQuantitativeInputFor = null) }
    }

    fun skipHabit(instanceId: String) {
        viewModelScope.launch {
            val result = skipHabit.execute(instanceId)

            result.fold(
                onSuccess = {
                    loadTodayHabits()
                    _events.emit(TodayEvent.HabitSkipped)
                },
                onFailure = { error ->
                    when (error) {
                        is SkipLockedException -> _events.emit(TodayEvent.SkipLimitReached)
                        else -> _events.emit(TodayEvent.ShowError(error.message))
                    }
                },
            )
        }
    }

    fun undoHabit(instanceId: String) {
        viewModelScope.launch {
            val result = undoHabit.execute(instanceId)

            result.fold(
                onSuccess = {
                    loadTodayHabits()
                    _events.emit(TodayEvent.ActionUndone)
                },
                onFailure = { error ->
                    _events.emit(TodayEvent.ShowError(error.message))
                },
            )
        }
    }

    fun archiveHabit(habitId: String) {
        viewModelScope.launch {
            try {
                habitRepository.archiveHabit(habitId)
                loadTodayHabits()
                _events.emit(TodayEvent.HabitArchived)
            } catch (e: Exception) {
                _events.emit(TodayEvent.ShowError(e.message))
            }
        }
    }

    fun dismissTimezoneWarning() {
        _state.update { it.copy(showTimezoneWarning = false, previousTimezone = null) }
    }

    fun navigateToHabitDetail(instanceId: String) {
        viewModelScope.launch {
            _events.emit(TodayEvent.NavigateToHabitDetail(instanceId))
        }
    }

    fun navigateToCreateHabit() {
        viewModelScope.launch {
            _events.emit(TodayEvent.NavigateToCreateHabit)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
