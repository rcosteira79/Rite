package com.ricardocosteira.habitlock.presentation.ui.onboarding

import com.ricardocosteira.habitlock.domain.models.HabitType
import kotlinx.datetime.DayOfWeek

enum class ScheduleOption { EVERY_DAY, WEEKDAYS, WEEKENDS, CUSTOM }

/**
 * State for the onboarding flow.
 */
data class OnboardingState(
    val selectedPreset: OnboardingStrictnessPreset = OnboardingStrictnessPreset.BALANCED,
    val habitName: String = "",
    val habitType: HabitType = HabitType.BINARY,
    val targetValue: String = "",
    val unit: String = "",
    val scheduleOption: ScheduleOption = ScheduleOption.EVERY_DAY,
    val customDays: Set<DayOfWeek> = emptySet(),
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
    data object EmptyHabitName : OnboardingEvent
    data object MissingTargetValue : OnboardingEvent
    data object InvalidTargetValue : OnboardingEvent
}
