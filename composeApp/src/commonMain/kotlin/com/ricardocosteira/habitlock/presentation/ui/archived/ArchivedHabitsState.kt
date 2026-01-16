package com.ricardocosteira.habitlock.presentation.ui.archived

import com.ricardocosteira.habitlock.domain.models.Habit

/**
 * State for the Archived Habits screen.
 */
data class ArchivedHabitsState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
