package com.ricardocosteira.rite.presentation.ui.archived

import com.ricardocosteira.rite.domain.models.Habit

/**
 * State for the Archived Habits screen.
 */
data class ArchivedHabitsState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
