package com.ricardocosteira.rite.presentation.ui.today.components.habitcard

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

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

/**
 * Eyebrow / kicker label tint for a given card state. Co-located with the
 * state itself so per-state visual mappings live next to the type that
 * defines them, instead of being scattered in `when` blocks at call sites.
 */
@Composable
fun HabitCardState.kickerColor(): Color {
    val colors = RiteAppTheme.colors
    return when (this) {
        HabitCardState.Completed -> colors.primary

        HabitCardState.Failed -> colors.error

        HabitCardState.Skipped -> colors.onSurfaceSubtle

        HabitCardState.Suspended -> colors.suspend

        HabitCardState.Pending,
        HabitCardState.PendingInProgress -> colors.onSurfaceMuted
    }
}

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
