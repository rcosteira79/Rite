package com.ricardocosteira.rite.presentation.ui.today.habitcard

import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType

/**
 * The five visual states a habit card renders. Maps 1:1 from [HabitStatus], with
 * pending quantitative habits that already have progress breaking out into
 * [PendingInProgress] so the margin rule can render the soft-accent fill.
 */
enum class HabitCardState { Pending, PendingInProgress, Completed, Skipped, Failed, Suspended }

data class HabitCardVisuals(
    val state: HabitCardState,
    /** 0f..1f. Fraction of the margin rule that renders filled. */
    val fillFraction: Float
)

fun visualsFor(status: HabitStatus, type: HabitType, progressPercentage: Float): HabitCardVisuals {
    val clamped = progressPercentage.coerceIn(0f, 1f)
    return when (status) {
        HabitStatus.COMPLETED -> HabitCardVisuals(HabitCardState.Completed, 1f)

        HabitStatus.FAILED -> HabitCardVisuals(HabitCardState.Failed, 1f)

        HabitStatus.SKIPPED -> HabitCardVisuals(HabitCardState.Skipped, 0f)

        HabitStatus.SUSPENDED -> HabitCardVisuals(HabitCardState.Suspended, 0f)

        HabitStatus.PENDING -> {
            val inProgress = type == HabitType.QUANTITATIVE && clamped > 0f
            val state = if (inProgress) HabitCardState.PendingInProgress else HabitCardState.Pending
            HabitCardVisuals(state, if (inProgress) clamped else 0f)
        }
    }
}
