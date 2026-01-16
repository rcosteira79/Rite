package com.ricardocosteira.habitlock.presentation.ui.habit

import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ReminderType
import kotlinx.datetime.LocalTime

/**
 * State for the Create/Edit Habit screen.
 */
data class HabitFormState(
    val habitId: String? = null,
    val name: String = "",
    val description: String = "",
    val type: HabitType = HabitType.BINARY,
    val targetValue: String = "",
    val unit: String = "",
    val hasReminder: Boolean = false,
    val reminderType: ReminderType = ReminderType.FIXED,
    val reminderTime: LocalTime? = null,
    val intervalMinutes: String = "60",
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isEditing: Boolean get() = habitId != null

    val isValid: Boolean get() = name.isNotBlank() &&
        (type == HabitType.BINARY || targetValue.toIntOrNull()?.let { it > 0 } == true)
}

/**
 * Events from the Create/Edit Habit screen.
 */
sealed interface HabitFormEvent {
    data object NavigateBack : HabitFormEvent
    data class ShowError(val message: String) : HabitFormEvent
}
