package com.ricardocosteira.habitlock.presentation.ui.today

import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.today_error_skip_limit_reached
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource

sealed interface UndoOperation {
    val habitId: String
    val habitName: String

    data class Delete(override val habitId: String, override val habitName: String) : UndoOperation

    data class Archive(override val habitId: String, override val habitName: String) : UndoOperation
}

/**
 * State for the Today screen.
 */
data class TodayState(
    val habits: ImmutableList<TodayHabitUiModel> = persistentListOf(),
    val pendingDaily: ImmutableList<TodayHabitUiModel> = persistentListOf(),
    val resolvedDaily: ImmutableList<TodayHabitUiModel> = persistentListOf(),
    val pendingWeekly: ImmutableList<TodayHabitUiModel> = persistentListOf(),
    val resolvedWeekly: ImmutableList<TodayHabitUiModel> = persistentListOf(),
    val isLoading: Boolean = true,
    val showTimezoneWarning: Boolean = false,
    val previousTimezone: String? = null,
    val error: String? = null,
    val showQuantitativeInputFor: String? = null,
    val pendingCount: Int = 0,
    val dailyProgressDisplay: Int = 0,
    val dailyProgressExact: Float = 0f,
    val dailyTotal: Int = 0,
    val motivationalTitleRes: StringResource? = null,
    val strictnessPreset: StrictnessPreset? = null,
    val pendingUndo: UndoOperation? = null,
)

/**
 * Events from the Today screen.
 */
sealed interface TodayEvent {
    data class NavigateToHabitDetail(val instanceId: String) : TodayEvent

    data object NavigateToCreateHabit : TodayEvent

    sealed interface ShowSnackbar : TodayEvent {
        val messageRes: StringResource
    }

    data object SkipLimitReached : ShowSnackbar {
        override val messageRes: StringResource = Res.string.today_error_skip_limit_reached
    }

    data class ShowError(val message: String) : TodayEvent

    data class HabitArchived(val habitName: String) : TodayEvent

    data class HabitDeleted(val habitName: String) : TodayEvent

    data object UndoCompleted : TodayEvent
}
