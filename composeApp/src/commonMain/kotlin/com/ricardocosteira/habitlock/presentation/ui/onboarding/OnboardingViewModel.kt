package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.habitlock.di.AppScope
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.domain.usecases.ApplyStrictnessPreset
import com.ricardocosteira.habitlock.domain.usecases.CreateHabit
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.habitlock.util.todayIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject
import kotlin.time.Clock

/**
 * Scoped to the application lifetime via [AppScope] rather than a
 * [androidx.lifecycle.ViewModelStoreOwner] because this app uses a single-activity architecture and
 * all ViewModels are obtained directly from the DI component. Composable-scoped ViewModels
 * (rememberViewModelStoreOwner) would be more semantically correct but require lifecycle
 * 2.11.0-alpha02 and a custom ViewModelProvider.Factory bridge. Revisit when that API stabilises.
 */
@AppScope
@Inject
class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val applyStrictnessPreset: ApplyStrictnessPreset,
    private val createHabit: CreateHabit,
    private val generateDailyHabits: GenerateDailyHabits
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    fun selectPreset(preset: OnboardingStrictnessPreset) {
        _state.update { it.copy(selectedPreset = preset) }
    }

    fun setCurrentStep(step: Int) {
        _state.update { it.copy(currentStep = step) }
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

    fun updateSelectedDays(days: Set<DayOfWeek>) {
        _state.update { it.copy(selectedDays = days) }
    }

    fun skipToToday() {
        viewModelScope.launch { applyPresetAndComplete(StrictnessPreset.BALANCED) }
    }

    fun continueFromStrictness() {
        viewModelScope.launch {
            _state.update { it.copy(isApplyingPreset = true) }

            val result = applyStrictnessPreset.execute(_state.value.selectedPreset.toDomain())

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isApplyingPreset = false, currentStep = 2) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isApplyingPreset = false, error = error.message) }
                }
            )
        }
    }

    fun createFirstHabit() {
        val habitName = _state.value.habitName.trim()
        if (habitName.isBlank()) {
            viewModelScope.launch { _events.emit(OnboardingEvent.EmptyHabitName) }
            return
        }

        val habitType = _state.value.habitType
        if (habitType == HabitType.QUANTITATIVE) {
            val targetValueStr = _state.value.targetValue.trim()
            if (targetValueStr.isBlank()) {
                viewModelScope.launch { _events.emit(OnboardingEvent.MissingTargetValue) }
                return
            }
            val targetValue = targetValueStr.toIntOrNull()
            if (targetValue == null || targetValue <= 0) {
                viewModelScope.launch { _events.emit(OnboardingEvent.InvalidTargetValue) }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isCreatingHabit = true) }

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            val targetValue = if (habitType == HabitType.QUANTITATIVE) {
                _state.value.targetValue
                    .trim()
                    .toIntOrNull()
            } else {
                null
            }

            val unit = if (habitType == HabitType.QUANTITATIVE && _state.value.unit.isNotBlank()) {
                _state.value.unit.trim()
            } else {
                null
            }

            val selectedDays = _state.value.selectedDays
            val specificDays: Set<DayOfWeek>? =
                if (selectedDays.size == DayOfWeek.entries.size) null else selectedDays

            val result = createHabit.execute(
                params = CreateHabit.CreateHabitParams(
                    name = habitName,
                    description = null,
                    type = habitType,
                    targetValue = targetValue,
                    unit = unit,
                    specificDays = specificDays,
                    reminder = null
                ),
                startDate = today
            )

            result.fold(
                onSuccess = {
                    // Generate habit instance for today
                    generateDailyHabits.execute()
                    completeOnboarding()
                },
                onFailure = { error ->
                    _state.update { it.copy(isCreatingHabit = false, error = error.message) }
                }
            )
        }
    }

    fun skipFirstHabit() {
        viewModelScope.launch { completeOnboarding() }
    }

    private suspend fun applyPresetAndComplete(preset: StrictnessPreset) {
        _state.update { it.copy(isApplyingPreset = true) }

        applyStrictnessPreset.execute(preset)
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

    private fun OnboardingStrictnessPreset.toDomain(): StrictnessPreset =
        when (this) {
            OnboardingStrictnessPreset.FLEXIBLE -> StrictnessPreset.FLEXIBLE
            OnboardingStrictnessPreset.BALANCED -> StrictnessPreset.BALANCED
            OnboardingStrictnessPreset.LOCKED -> StrictnessPreset.LOCKED
        }
}
