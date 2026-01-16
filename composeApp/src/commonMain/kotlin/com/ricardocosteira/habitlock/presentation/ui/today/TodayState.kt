package com.ricardocosteira.habitlock.presentation.ui.today

import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel

/**
 * State for the Today screen.
 */
data class TodayState(
    val habits: List<TodayHabitUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val showTimezoneWarning: Boolean = false,
    val previousTimezone: String? = null,
    val error: String? = null,
    val showQuantitativeInputFor: String? = null
)

/**
 * Events from the Today screen.
 */
sealed interface TodayEvent {
    data class NavigateToHabitDetail(val instanceId: String) : TodayEvent
    data object NavigateToCreateHabit : TodayEvent
    data class ShowError(val message: String) : TodayEvent
    data class ShowSuccess(val message: String) : TodayEvent
}


