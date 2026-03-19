package com.ricardocosteira.habitlock.presentation.ui.today

import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import com.ricardocosteira.habitlock.presentation.ui.UiText

/**
 * State for the Today screen.
 */
data class TodayState(
    val habits: List<TodayHabitUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val showTimezoneWarning: Boolean = false,
    val previousTimezone: String? = null,
    val error: String? = null,
    val showQuantitativeInputFor: String? = null,
    val pendingCount: Int = 0,
    val dailyResolved: Int = 0,
    val dailyTotal: Int = 0,
    val weeklyResolved: Int = 0,
    val weeklyTotal: Int = 0
)

/**
 * Events from the Today screen.
 */
sealed interface TodayEvent {
    data class NavigateToHabitDetail(val instanceId: String) : TodayEvent
    data object NavigateToCreateHabit : TodayEvent
    data class ShowError(val message: UiText) : TodayEvent
    data class ShowSuccess(val message: UiText) : TodayEvent
}
