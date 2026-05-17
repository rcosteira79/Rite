package com.ricardocosteira.rite.presentation.ui.onboarding

import com.ricardocosteira.rite.domain.models.HabitType

enum class OnboardingScheduleKind { DAILY, WEEKLY }

/** State for the onboarding flow. */
data class OnboardingState(
    val selectedPreset: OnboardingStrictnessPreset = OnboardingStrictnessPreset.BALANCED,
    val habitName: String = "",
    val habitType: HabitType = HabitType.BINARY,
    val targetValue: String = "",
    val unit: String = "",
    val scheduleKind: OnboardingScheduleKind = OnboardingScheduleKind.DAILY,
    val isCreatingHabit: Boolean = false,
    val isApplyingPreset: Boolean = false,
    val error: String? = null,
    val currentStep: Int = 0,
    val showNotificationStep: Boolean = false
) {
    val totalSteps: Int get() = if (showNotificationStep) 4 else 3

    val firstHabitStepIndex: Int get() = 2

    /** Only valid when [showNotificationStep] is true. */
    val notificationStepIndex: Int get() = 3
}

/** Events from the onboarding flow. */
sealed interface OnboardingEvent {
    data object NavigateToToday : OnboardingEvent

    data object EmptyHabitName : OnboardingEvent

    data object MissingTargetValue : OnboardingEvent

    data object InvalidTargetValue : OnboardingEvent
}
