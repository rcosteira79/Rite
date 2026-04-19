package com.ricardocosteira.rite.presentation.ui.today

import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.ui.components.RiteSnackbarVisuals
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource

data class PendingDelete(val habitId: String, val habitName: String)

/**
 * State for the Today screen.
 */
data class TodayState(
    val habits: ImmutableList<TodayHabitUiModel> = persistentListOf(),
    val daily: ImmutableList<TodayHabitUiModel> = persistentListOf(),
    val weekly: ImmutableList<TodayHabitUiModel> = persistentListOf(),
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
    val pendingDelete: PendingDelete? = null
)

/**
 * Nav events are processed synchronously so they never queue behind a
 * long-running snackbar.
 */
sealed interface TodayNavEvent {
    data class ToHabitDetail(val instanceId: String) : TodayNavEvent

    data object ToCreateHabit : TodayNavEvent
}

/**
 * Feedback events are snackbar-bound. The Today screen collects these on a
 * separate coroutine and preempts the current snackbar so only the latest
 * feedback is visible.
 */
sealed interface TodayFeedbackEvent {
    data class ShowSnackbar(val visuals: RiteSnackbarVisuals) : TodayFeedbackEvent

    data class HabitDeleted(val habitName: String) : TodayFeedbackEvent

    data class SkipLimitReached(val habitName: String) : TodayFeedbackEvent

    data class ShowError(val message: String?) : TodayFeedbackEvent

    data object UndoCompleted : TodayFeedbackEvent
}
