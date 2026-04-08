package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.rite.domain.models.CompletionSource
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import com.ricardocosteira.rite.domain.usecases.CompleteHabit
import com.ricardocosteira.rite.domain.usecases.SkipHabit
import com.ricardocosteira.rite.domain.usecases.UndoHabit
import com.ricardocosteira.rite.domain.usecases.UndoLastIncrement
import com.ricardocosteira.rite.util.todayIn
import kotlin.time.Clock
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

private const val HEATMAP_DAYS = 90

@Inject
class HabitDetailViewModel(
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val userRepository: UserRepository,
    private val completeHabit: CompleteHabit,
    private val skipHabit: SkipHabit,
    private val undoHabit: UndoHabit,
    private val undoLastIncrement: UndoLastIncrement,
    @Assisted private val instanceId: String
) : ViewModel() {

    private val _state = MutableStateFlow(HabitDetailState())
    val state: StateFlow<HabitDetailState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<HabitDetailEvent>()
    val events: SharedFlow<HabitDetailEvent> = _events.asSharedFlow()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            val isInitialLoad: Boolean = _state.value.habit == null
            if (isInitialLoad) {
                _state.update { it.copy(isLoading = true) }
            }

            val instance: HabitInstance = habitInstanceRepository.getInstanceById(instanceId)
                ?: run {
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }

            val habit = habitRepository.getHabitById(instance.habitId) ?: run {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            val user = userRepository.getUser()
            val maxSkips: Int? = user?.maxConsecutiveSkips

            val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val startDate: LocalDate = today.minus(DatePeriod(days = HEATMAP_DAYS))
            val allInstances: List<HabitInstance> =
                habitInstanceRepository.getInstancesForHabit(instance.habitId)

            val heatmapData = allInstances
                .filter { it.date >= startDate && it.date <= today }
                .map { inst ->
                    HeatmapDay(
                        date = inst.date.toString(),
                        completionPercentage = inst.progressPercentage(),
                        status = inst.status
                    )
                }
                .toImmutableList()

            val consecutiveSkips: Int = calculateConsecutiveSkips(allInstances)

            val uiModel = HabitDetailUiModel(
                habitId = habit.id,
                instanceId = instance.id,
                name = habit.name,
                description = habit.description,
                type = habit.type,
                unit = habit.unit,
                defaultIncrement = habit.defaultIncrement,
                status = instance.status,
                currentProgress = instance.currentProgress,
                targetValue = instance.targetValue,
                completedValue = instance.completedValue,
                progressPercentage = instance.progressPercentage(),
                isQuantitativeComplete = instance.isQuantitativeComplete(),
                currentStreak = habit.currentStreak,
                longestStreak = habit.longestStreak,
                habitScore = habit.calculateScore().percentage,
                maxConsecutiveSkips = maxSkips,
                currentConsecutiveSkips = consecutiveSkips
            )

            _state.update {
                it.copy(
                    habit = uiModel,
                    heatmapData = heatmapData,
                    isLoading = false
                )
            }
        }
    }

    fun completeBinary() {
        val instanceId: String = _state.value.habit?.instanceId ?: return
        viewModelScope.launch {
            completeHabit.executeBinary(instanceId, CompletionSource.IN_APP)
            loadDetail()
        }
    }

    fun incrementProgress() {
        val instanceId: String = _state.value.habit?.instanceId ?: return
        val increment: Int = _state.value.habit?.defaultIncrement ?: 1
        viewModelScope.launch {
            completeHabit.executeQuantitative(instanceId, increment, CompletionSource.IN_APP)
            loadDetail()
        }
    }

    fun addCustomProgress(amount: Int) {
        val instanceId: String = _state.value.habit?.instanceId ?: return
        viewModelScope.launch {
            completeHabit.executeQuantitative(instanceId, amount, CompletionSource.IN_APP)
            loadDetail()
        }
    }

    fun skip() {
        val instanceId: String = _state.value.habit?.instanceId ?: return
        viewModelScope.launch {
            skipHabit.execute(instanceId)
            loadDetail()
        }
    }

    fun undo() {
        val habit = _state.value.habit ?: return
        viewModelScope.launch {
            if (habit.type == HabitType.QUANTITATIVE && habit.isCompleted) {
                undoLastIncrement.execute(habit.instanceId)
            } else {
                undoHabit.execute(habit.instanceId)
            }
            loadDetail()
        }
    }

    fun undoIncrement() {
        val instanceId: String = _state.value.habit?.instanceId ?: return
        viewModelScope.launch {
            undoLastIncrement.execute(instanceId)
            loadDetail()
        }
    }

    fun showCustomInput() {
        _state.update { it.copy(showCustomInput = true) }
    }

    fun dismissCustomInput() {
        _state.update { it.copy(showCustomInput = false) }
    }

    fun archiveHabit() {
        val habitId: String = _state.value.habit?.habitId ?: return
        viewModelScope.launch {
            habitRepository.archiveHabit(habitId)
            _events.emit(HabitDetailEvent.NavigateBack)
        }
    }

    fun deleteHabit() {
        val habitId: String = _state.value.habit?.habitId ?: return
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
            _events.emit(HabitDetailEvent.NavigateBack)
        }
    }

    private fun calculateConsecutiveSkips(instances: List<HabitInstance>): Int {
        var count: Int = 0
        for (instance in instances.sortedByDescending { it.date }) {
            if (instance.status == HabitStatus.SKIPPED) {
                count++
            } else {
                break
            }
        }
        return count
    }
}
