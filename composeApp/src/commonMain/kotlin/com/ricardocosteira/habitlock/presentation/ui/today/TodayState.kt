package com.ricardocosteira.habitlock.presentation.ui.today

import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.today_error_skip_limit_reached
import habitlock.composeapp.generated.resources.today_success_action_undone
import habitlock.composeapp.generated.resources.today_success_habit_archived
import habitlock.composeapp.generated.resources.today_success_habit_completed
import habitlock.composeapp.generated.resources.today_success_habit_skipped
import habitlock.composeapp.generated.resources.today_success_progress_added
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource

/**
 * State for the Today screen.
 */
data class TodayState(
    val habits: ImmutableList<TodayHabitUiModel> = persistentListOf(),
    val isLoading: Boolean = true,
    val showTimezoneWarning: Boolean = false,
    val previousTimezone: String? = null,
    val error: String? = null,
    val showQuantitativeInputFor: String? = null,
    val pendingCount: Int = 0,
    val dailyResolved: Int = 0,
    val dailyTotal: Int = 0,
    val motivationalTitle: String = "",
    val strictnessPreset: StrictnessPreset? = null,
)

/**
 * Events from the Today screen.
 */
sealed interface TodayEvent {
    data class NavigateToHabitDetail(
        val instanceId: String,
    ) : TodayEvent

    data object NavigateToCreateHabit : TodayEvent

    sealed interface ShowSnackbar : TodayEvent {
        val messageRes: StringResource
    }

    data object HabitCompleted : ShowSnackbar {
        override val messageRes: StringResource = Res.string.today_success_habit_completed
    }

    data object ProgressAdded : ShowSnackbar {
        override val messageRes: StringResource = Res.string.today_success_progress_added
    }

    data object HabitSkipped : ShowSnackbar {
        override val messageRes: StringResource = Res.string.today_success_habit_skipped
    }

    data object ActionUndone : ShowSnackbar {
        override val messageRes: StringResource = Res.string.today_success_action_undone
    }

    data object HabitArchived : ShowSnackbar {
        override val messageRes: StringResource = Res.string.today_success_habit_archived
    }

    data object SkipLimitReached : ShowSnackbar {
        override val messageRes: StringResource = Res.string.today_error_skip_limit_reached
    }

    data class ShowError(
        val message: String,
    ) : TodayEvent
}
