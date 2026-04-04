package com.ricardocosteira.rite.presentation.ui.settings

import com.ricardocosteira.rite.domain.models.UndoPolicy
import kotlinx.datetime.LocalTime

/**
 * State for the Settings screen.
 */
data class SettingsState(
    val undoPolicy: UndoPolicy = UndoPolicy.TODAY_ONLY,
    val maxSnoozeDurationMinutes: Int = 30,
    val maxSnoozesPerHabitPerDay: Int? = 3,
    val maxConsecutiveSkips: Int? = 2,
    val dailySummaryTime: LocalTime? = null,
    val currentTimezone: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)
