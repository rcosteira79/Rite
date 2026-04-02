package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.habitlock.di.AppScope
import com.ricardocosteira.habitlock.domain.models.CompletionSource
import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.domain.models.UserStrictnessSettings
import com.ricardocosteira.habitlock.domain.models.motivationalTitleIndexForDate
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.domain.usecases.CompleteHabit
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.habitlock.domain.usecases.ProcessEndOfDay
import com.ricardocosteira.habitlock.domain.usecases.SkipHabit
import com.ricardocosteira.habitlock.domain.usecases.SkipLockedException
import com.ricardocosteira.habitlock.domain.usecases.UndoHabit
import com.ricardocosteira.habitlock.domain.usecases.UndoLastIncrement
import com.ricardocosteira.habitlock.notifications.HabitNotification
import com.ricardocosteira.habitlock.notifications.TrackedHabitInfo
import com.ricardocosteira.habitlock.presentation.mappers.motivationalTitleResource
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import com.ricardocosteira.habitlock.presentation.models.mapToTodayHabitUiModel
import com.ricardocosteira.habitlock.util.toLocalDate
import kotlin.time.Clock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject

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
    private val undoLastIncrement: UndoLastIncrement,
    private val habitNotification: HabitNotification
) : ViewModel() {
    private val _state = MutableStateFlow(TodayState())
    val state: StateFlow<TodayState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TodayEvent>()
    val events: SharedFlow<TodayEvent> = _events.asSharedFlow()

    private var undoJob: Job? = null

    init {
        loadTodayHabits()
    }

    fun loadTodayHabits() {
        viewModelScope.launch {
            // Only show loading spinner on initial load, not on refreshes
            // after actions (complete, skip, undo) to avoid full-screen flash.
            val isInitialLoad: Boolean = _state.value.habits.isEmpty()
            if (isInitialLoad) {
                _state.update { it.copy(isLoading = true) }
            }

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
                            previousTimezone = user.previousTimezone.id
                        )
                    }
                }

                // Derive timezone and strictness preset
                val userTimezone: TimeZone = user?.timezone ?: TimeZone.currentSystemDefault()

                val strictnessPreset: StrictnessPreset? = user?.let {
                    val settings = UserStrictnessSettings(
                        undoPolicy = it.undoPolicy,
                        maxSnoozesPerHabitPerDay = it.maxSnoozesPerHabitPerDay,
                        maxConsecutiveSkips = it.maxConsecutiveSkips,
                        maxSnoozeDurationMinutes = it.maxSnoozeDurationMinutes
                    )
                    StrictnessPreset.fromSettings(settings)
                }

                // Get today's instances
                val today = Clock.System.now().toLocalDate(userTimezone)
                val instances = habitInstanceRepository.getInstancesForDate(today)

                val motivationalTitleRes = motivationalTitleResource(
                    motivationalTitleIndexForDate(today)
                )

                // Map to UI models — runs on Default to keep the main thread free
                val habits: ImmutableList<TodayHabitUiModel> = withContext(Dispatchers.Default) {
                    coroutineScope {
                        instances.mapNotNull { instance ->
                            val habitDeferred = async {
                                habitRepository.getHabitById(instance.habitId)
                            }
                            val scheduleDeferred = async {
                                habitRepository.getScheduleForHabit(instance.habitId)
                            }
                            val habit = habitDeferred.await() ?: return@mapNotNull null
                            val schedule = scheduleDeferred.await() ?: return@mapNotNull null
                            mapToTodayHabitUiModel(
                                instance = instance,
                                habit = habit,
                                schedule = schedule,
                                maxConsecutiveSkips = user?.maxConsecutiveSkips,
                                userTimezone = userTimezone
                            )
                        }.toImmutableList()
                    }
                }

                val counts: TodayCounts = habits.computeCounts()

                val resolvedStatuses: Set<HabitStatus> = setOf(
                    HabitStatus.COMPLETED,
                    HabitStatus.SKIPPED,
                    HabitStatus.FAILED
                )

                val dailyHabits: List<TodayHabitUiModel> = habits.filter {
                    it.isDaily &&
                        !it.isSuspended
                }
                val weeklyHabits: List<TodayHabitUiModel> = habits.filter {
                    it.isWeekly &&
                        !it.isSuspended
                }

                val (pendingDaily: List<TodayHabitUiModel>, resolvedDaily: List<TodayHabitUiModel>) =
                    dailyHabits.partition { it.status !in resolvedStatuses }
                val (pendingWeekly: List<TodayHabitUiModel>, resolvedWeekly: List<TodayHabitUiModel>) =
                    weeklyHabits.partition { it.status !in resolvedStatuses }

                _state.update {
                    it.copy(
                        habits = habits,
                        pendingDaily = pendingDaily.toImmutableList(),
                        resolvedDaily = resolvedDaily.toImmutableList(),
                        pendingWeekly = pendingWeekly.toImmutableList(),
                        resolvedWeekly = resolvedWeekly.toImmutableList(),
                        isLoading = false,
                        pendingCount = counts.pendingCount,
                        dailyProgressDisplay = counts.dailyProgressDisplay,
                        dailyProgressExact = counts.dailyProgressExact,
                        dailyTotal = counts.dailyTotal,
                        motivationalTitleRes = motivationalTitleRes,
                        strictnessPreset = strictnessPreset
                    )
                }

                refreshTrackingNotification()
            } catch (e: Exception) {
                val fallbackTimezone: TimeZone = TimeZone.currentSystemDefault()
                val today = Clock.System.now().toLocalDate(fallbackTimezone)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                        motivationalTitleRes = motivationalTitleResource(
                            motivationalTitleIndexForDate(today)
                        )
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

            result.onSuccess {
                loadTodayHabits()
                // No snackbar — card state change is the feedback
            }.onFailure { error ->
                _events.emit(TodayEvent.ShowError(error.message ?: "Something went wrong"))
            }
        }
    }

    fun completeQuantitativeHabit(instanceId: String, value: Int) {
        viewModelScope.launch {
            _state.update { it.copy(showQuantitativeInputFor = null) }

            val result = completeHabit.executeQuantitative(
                instanceId = instanceId,
                deltaValue = value,
                source = CompletionSource.IN_APP
            )

            result.onSuccess { updatedInstance ->
                loadTodayHabits()
                if (updatedInstance.isQuantitativeComplete()) {
                    // No snackbar — card state change is the feedback
                } else {
                    // No snackbar — progress bar update is the feedback
                }
            }.onFailure { error ->
                _events.emit(TodayEvent.ShowError(error.message ?: "Something went wrong"))
            }
        }
    }

    fun incrementHabitProgress(instanceId: String) {
        viewModelScope.launch {
            val habit: TodayHabitUiModel =
                _state.value.habits.find { it.instanceId == instanceId } ?: return@launch

            val result: Result<HabitInstance> = completeHabit.executeQuantitative(
                instanceId = instanceId,
                deltaValue = habit.defaultIncrement,
                source = CompletionSource.IN_APP
            )

            result.onSuccess { updatedInstance: HabitInstance ->
                loadTodayHabits()
                if (updatedInstance.isQuantitativeComplete()) {
                    // No snackbar — card state change is the feedback
                } else {
                    // No snackbar — progress bar update is the feedback
                }
            }.onFailure { error: Throwable ->
                _events.emit(TodayEvent.ShowError(error.message ?: "Something went wrong"))
            }
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

            result.onSuccess {
                loadTodayHabits()
                // No snackbar — card state change is the feedback
            }.onFailure { error ->
                when (error) {
                    is SkipLockedException -> _events.emit(TodayEvent.SkipLimitReached)

                    else ->
                        _events.emit(
                            TodayEvent.ShowError(error.message ?: "Something went wrong")
                        )
                }
            }
        }
    }

    fun undoHabit(instanceId: String) {
        viewModelScope.launch {
            val result = undoHabit.execute(instanceId)

            result.onSuccess {
                loadTodayHabits()
                // No snackbar — card state revert is the feedback
            }.onFailure { error ->
                _events.emit(TodayEvent.ShowError(error.message ?: "Something went wrong"))
            }
        }
    }

    fun undoLastIncrement(instanceId: String) {
        viewModelScope.launch {
            val result = undoLastIncrement.execute(instanceId)

            result.onSuccess {
                loadTodayHabits()
            }.onFailure { error ->
                _events.emit(TodayEvent.ShowError(error.message ?: "Something went wrong"))
            }
        }
    }

    fun deleteHabit(habitId: String) {
        val habit: TodayHabitUiModel = _state.value.habits.find { it.habitId == habitId } ?: return

        // Commit any previous pending delete before starting a new one
        commitPendingDeleteAndCancelJob()

        removeHabitFromState(habitId)

        _state.update { it.copy(pendingDelete = PendingDelete(habitId, habit.name)) }

        viewModelScope.launch { _events.emit(TodayEvent.HabitDeleted(habit.name)) }

        undoJob = viewModelScope.launch {
            delay(UNDO_TIMEOUT_MS)
            try {
                habitRepository.deleteHabit(habitId)
                _state.update { it.copy(pendingDelete = null) }
            } catch (e: Exception) {
                _events.emit(TodayEvent.ShowError(e.message ?: "Failed to delete habit"))
                loadTodayHabits()
            }
        }
    }

    fun undoDelete() {
        undoJob?.cancel()
        undoJob = null
        _state.update { it.copy(pendingDelete = null) }
        viewModelScope.launch { _events.emit(TodayEvent.UndoCompleted) }
        loadTodayHabits()
    }

    private fun commitPendingDeleteAndCancelJob() {
        val previousDelete: PendingDelete? = _state.value.pendingDelete

        undoJob?.cancel()
        undoJob = null

        if (previousDelete == null) return

        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(previousDelete.habitId)
            } catch (e: Exception) {
                _events.emit(
                    TodayEvent.ShowError(e.message ?: "Failed to delete habit")
                )
            }
        }
    }

    private fun removeHabitFromState(habitId: String) {
        _state.update { state ->
            state.copy(
                habits = state.habits.filter { it.habitId != habitId }.toImmutableList(),
                pendingDaily = state.pendingDaily.filter {
                    it.habitId != habitId
                }.toImmutableList(),
                resolvedDaily = state.resolvedDaily.filter {
                    it.habitId != habitId
                }.toImmutableList(),
                pendingWeekly = state.pendingWeekly.filter {
                    it.habitId != habitId
                }.toImmutableList(),
                resolvedWeekly = state.resolvedWeekly.filter {
                    it.habitId != habitId
                }.toImmutableList()
            )
        }
    }

    fun archiveHabit(habitId: String) {
        viewModelScope.launch {
            try {
                habitRepository.archiveHabit(habitId)
                loadTodayHabits()
                // No snackbar — habit disappears from the list
            } catch (e: Exception) {
                _events.emit(TodayEvent.ShowError(e.message ?: "Something went wrong"))
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

    private suspend fun refreshTrackingNotification() {
        val trackedHabits: List<Habit> = habitRepository.getHabitsWithTrackingEnabled()
        if (trackedHabits.isEmpty()) {
            habitNotification.hideTrackingNotification()
            return
        }

        val today: LocalDate = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
        val trackedInfoList: List<TrackedHabitInfo> = trackedHabits.mapNotNull { habit: Habit ->
            val instance: HabitInstance = habitInstanceRepository.getInstanceForHabitAndDate(
                habit.id,
                today
            ) ?: return@mapNotNull null

            TrackedHabitInfo(
                instanceId = instance.id,
                habitId = habit.id,
                habitName = habit.name,
                type = habit.type,
                currentProgress = instance.currentProgress,
                targetValue = instance.targetValue,
                unit = habit.unit,
                defaultIncrement = habit.defaultIncrement,
                isCompleted = instance.status == HabitStatus.COMPLETED
            )
        }

        if (trackedInfoList.isEmpty()) {
            habitNotification.hideTrackingNotification()
        } else {
            habitNotification.updateTrackingNotification(trackedInfoList)
        }
    }

    private companion object {
        const val UNDO_TIMEOUT_MS: Long = 5_000L
    }
}
