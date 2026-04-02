package com.ricardocosteira.habitlock.presentation.ui.habit

import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import kotlinx.datetime.DayOfWeek

sealed interface HabitFormUiAction {
    data class NameChanged(val name: String) : HabitFormUiAction

    data class DescriptionChanged(val description: String) : HabitFormUiAction

    data class TypeChanged(val type: HabitType) : HabitFormUiAction

    data class TargetValueChanged(val value: String) : HabitFormUiAction

    data class UnitChanged(val unit: String) : HabitFormUiAction

    data class ScheduleTypeChanged(val scheduleType: ScheduleType) : HabitFormUiAction

    data class SelectedDaysChanged(val days: Set<DayOfWeek>) : HabitFormUiAction

    data class QuotaChanged(val quota: String) : HabitFormUiAction

    data class HasReminderChanged(val hasReminder: Boolean) : HabitFormUiAction

    data class ReminderTimeChanged(val hour: Int, val minute: Int) : HabitFormUiAction

    data object SaveClicked : HabitFormUiAction

    data object DeleteClicked : HabitFormUiAction

    data object ArchiveClicked : HabitFormUiAction

    data object DiscardDraftClicked : HabitFormUiAction

    data object DiscardChangesClicked : HabitFormUiAction

    data class IsTrackingEnabledChanged(val isEnabled: Boolean) : HabitFormUiAction

    data object NotificationSettingsClicked : HabitFormUiAction
}
