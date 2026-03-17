package com.ricardocosteira.habitlock.presentation.ui.onboarding

import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset

/**
 * State for the onboarding flow.
 */
data class OnboardingState(
    val selectedPreset: StrictnessPreset = StrictnessPreset.BALANCED,
    val habitName: String = "",
    val habitType: HabitType = HabitType.BINARY,
    val targetValue: String = "",
    val unit: String = "",
    val isCreatingHabit: Boolean = false,
    val isApplyingPreset: Boolean = false,
    val error: String? = null
)

/**
 * Events from the onboarding flow.
 */
sealed interface OnboardingEvent {
    data object NavigateToFirstHabit : OnboardingEvent
    data object NavigateToToday : OnboardingEvent
    data class ShowError(val message: String) : OnboardingEvent
}


