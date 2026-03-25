package com.ricardocosteira.habitlock.presentation.ui.habit

import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ReminderType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import kotlinx.datetime.DayOfWeek
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
    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val selectedDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val quota: String = "1",
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

    val isValid: Boolean get() {
        val nameValid = name.isNotBlank()
        val typeValid = type == HabitType.BINARY || targetValue.toIntOrNull()?.let { it > 0 } == true
        val quotaValid = quota.toIntOrNull()?.let { it > 0 } == true
        val daysValid = scheduleType == ScheduleType.DAILY || selectedDays.isNotEmpty()

        return nameValid && typeValid && quotaValid && daysValid
    }
}

/**
 * Events from the Create/Edit Habit screen.
 */
sealed interface HabitFormEvent {
    data object NavigateBack : HabitFormEvent

    data object RequiredFieldsMissing : HabitFormEvent

    data class ShowError(
        val message: String?
    ) : HabitFormEvent
}
