package com.ricardocosteira.habitlock.presentation.ui.onboarding

import me.tatarka.inject.annotations.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.domain.usecases.ApplyStrictnessPresetUseCase
import com.ricardocosteira.habitlock.domain.usecases.CreateHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabitsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

@Inject
class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val applyStrictnessPresetUseCase: ApplyStrictnessPresetUseCase,
    private val createHabitUseCase: CreateHabitUseCase,
    private val generateDailyHabitsUseCase: GenerateDailyHabitsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    fun selectPreset(preset: StrictnessPreset) {
        _state.update { it.copy(selectedPreset = preset) }
    }

    fun updateHabitName(name: String) {
        _state.update { it.copy(habitName = name) }
    }

    fun updateHabitType(type: HabitType) {
        _state.update { it.copy(habitType = type) }
    }

    fun updateTargetValue(value: String) {
        _state.update { it.copy(targetValue = value) }
    }

    fun updateUnit(unit: String) {
        _state.update { it.copy(unit = unit) }
    }

    fun continueFromPhilosophy() {
        viewModelScope.launch {
            _events.emit(OnboardingEvent.NavigateToStrictness)
        }
    }

    fun skipToToday() {
        viewModelScope.launch {
            applyPresetAndComplete(StrictnessPreset.BALANCED)
        }
    }

    fun continueFromStrictness() {
        viewModelScope.launch {
            _state.update { it.copy(isApplyingPreset = true) }

            val result = applyStrictnessPresetUseCase.execute(_state.value.selectedPreset)

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isApplyingPreset = false) }
                    _events.emit(OnboardingEvent.NavigateToFirstHabit)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isApplyingPreset = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun createFirstHabit() {
        val habitName = _state.value.habitName.trim()
        if (habitName.isBlank()) {
            viewModelScope.launch {
                _events.emit(OnboardingEvent.ShowError("Please enter a habit name"))
            }
            return
        }

        val habitType = _state.value.habitType
        if (habitType == HabitType.QUANTITATIVE) {
            val targetValueStr = _state.value.targetValue.trim()
            if (targetValueStr.isBlank()) {
                viewModelScope.launch {
                    _events.emit(OnboardingEvent.ShowError("Please enter a target value for quantitative habit"))
                }
                return
            }
            val targetValue = targetValueStr.toIntOrNull()
            if (targetValue == null || targetValue <= 0) {
                viewModelScope.launch {
                    _events.emit(OnboardingEvent.ShowError("Target value must be a positive number"))
                }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isCreatingHabit = true) }

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            val targetValue = if (habitType == HabitType.QUANTITATIVE) {
                _state.value.targetValue.trim().toIntOrNull()
            } else {
                null
            }

            val unit = if (habitType == HabitType.QUANTITATIVE && _state.value.unit.isNotBlank()) {
                _state.value.unit.trim()
            } else {
                null
            }

            val result = createHabitUseCase.execute(
                params = CreateHabitUseCase.CreateHabitParams(
                    name = habitName,
                    description = null,
                    type = habitType,
                    targetValue = targetValue,
                    unit = unit,
                    reminder = null
                ),
                startDate = today
            )

            result.fold(
                onSuccess = {
                    // Generate habit instance for today
                    generateDailyHabitsUseCase.execute()
                    completeOnboarding()
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isCreatingHabit = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun skipFirstHabit() {
        viewModelScope.launch {
            completeOnboarding()
        }
    }

    private suspend fun applyPresetAndComplete(preset: StrictnessPreset) {
        _state.update { it.copy(isApplyingPreset = true) }

        applyStrictnessPresetUseCase.execute(preset)
        completeOnboarding()
    }

    private suspend fun completeOnboarding() {
        val user = userRepository.getUser()
        if (user != null) {
            userRepository.setOnboardingCompleted(user.id, true)
        }

        _state.update { it.copy(isCreatingHabit = false, isApplyingPreset = false) }
        _events.emit(OnboardingEvent.NavigateToToday)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

private fun Clock.System.todayIn(timezone: TimeZone) = now().toLocalDateTime(timezone).date
