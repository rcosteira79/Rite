package com.ricardocosteira.habitlock.presentation.ui.onboarding

import com.ricardocosteira.habitlock.domain.models.HabitType
import kotlinx.datetime.DayOfWeek

/** State for the onboarding flow. */
data class OnboardingState(
  val selectedPreset: OnboardingStrictnessPreset = OnboardingStrictnessPreset.BALANCED,
  val habitName: String = "",
  val habitType: HabitType = HabitType.BINARY,
  val targetValue: String = "",
  val unit: String = "",
  val selectedDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
  val isCreatingHabit: Boolean = false,
  val isApplyingPreset: Boolean = false,
  val error: String? = null,
)

/** Events from the onboarding flow. */
sealed interface OnboardingEvent {
  data object NavigateToFirstHabit : OnboardingEvent

  data object NavigateToToday : OnboardingEvent

  data object EmptyHabitName : OnboardingEvent

  data object MissingTargetValue : OnboardingEvent

  data object InvalidTargetValue : OnboardingEvent
}
