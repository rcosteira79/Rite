package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.domain.usecases.ApplyStrictnessPresetUseCase
import com.ricardocosteira.habitlock.domain.usecases.CreateHabitUseCase
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

class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val applyStrictnessPresetUseCase: ApplyStrictnessPresetUseCase,
    private val createHabitUseCase: CreateHabitUseCase
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

        viewModelScope.launch {
            _state.update { it.copy(isCreatingHabit = true) }

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            val result = createHabitUseCase.execute(
                params = CreateHabitUseCase.CreateHabitParams(
                    name = habitName,
                    description = null,
                    type = HabitType.BINARY,
                    targetValue = null,
                    unit = null,
                    reminder = null
                ),
                startDate = today
            )

            result.fold(
                onSuccess = {
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
