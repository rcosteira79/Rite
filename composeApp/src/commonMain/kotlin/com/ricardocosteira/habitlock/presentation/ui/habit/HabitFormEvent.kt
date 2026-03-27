package com.ricardocosteira.habitlock.presentation.ui.habit

/**
 * Events from the Create/Edit Habit screen.
 */
sealed interface HabitFormEvent {
    data object NavigateBack : HabitFormEvent

    data object RequiredFieldsMissing : HabitFormEvent

    data object HabitNotFound : HabitFormEvent

    data class ShowError(
        val message: String?
    ) : HabitFormEvent
}
