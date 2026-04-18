package com.ricardocosteira.rite.presentation.ui.today.habitcard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_skip
import rite.composeapp.generated.resources.today_action_complete
import rite.composeapp.generated.resources.today_action_increment_short
import rite.composeapp.generated.resources.today_action_undo
import rite.composeapp.generated.resources.today_stamp_missed
import rite.composeapp.generated.resources.today_stamp_paused
import rite.composeapp.generated.resources.today_stamp_skipped

@Composable
fun HabitCardAction(
    state: HabitCardState,
    type: HabitType,
    defaultIncrement: Int,
    skipLocked: Boolean,
    onComplete: () -> Unit,
    onIncrement: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when (state) {
            HabitCardState.Pending, HabitCardState.PendingInProgress -> {
                when (type) {
                    HabitType.BINARY -> CheckCircle(onClick = onComplete)

                    HabitType.QUANTITATIVE ->
                        PlusChip(
                            label =
                                stringResource(
                                    Res.string.today_action_increment_short,
                                    defaultIncrement,
                                ),
                            onClick = onIncrement,
                        )
                }
                if (!skipLocked) {
                    SkipChip(onClick = onSkip)
                }
            }

            HabitCardState.Completed -> UndoPill(onClick = onUndo)

            HabitCardState.Skipped -> {
                Stamp(
                    label = stringResource(Res.string.today_stamp_skipped),
                    stateColor = RiteAppTheme.colors.onSurfaceMuted,
                )
                UndoPill(onClick = onUndo, variant = UndoVariant.Skip)
            }

            HabitCardState.Failed ->
                Stamp(
                    label = stringResource(Res.string.today_stamp_missed),
                    stateColor = RiteAppTheme.colors.error,
                )

            HabitCardState.Suspended ->
                Stamp(
                    label = stringResource(Res.string.today_stamp_paused),
                    stateColor = RiteAppTheme.colors.suspend,
                )
        }
    }
}

@Composable
private fun CheckCircle(onClick: () -> Unit) {
    val colors = RiteAppTheme.colors
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
        contentColor = colors.onSurface,
        border = BorderStroke(1.5.dp, colors.onSurface),
        modifier = Modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(Res.string.today_action_complete),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun PlusChip(label: String, onClick: () -> Unit,) {
    val colors = RiteAppTheme.colors
    Surface(
        onClick = onClick,
        shape = RiteAppTheme.shapes.pill,
        color = Color.Transparent,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.onSurface),
    ) {
        Text(
            text = label,
            style = RiteAppTheme.typography.mono,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
        )
    }
}

@Composable
private fun SkipChip(onClick: () -> Unit) {
    val colors = RiteAppTheme.colors
    Surface(
        onClick = onClick,
        shape = RiteAppTheme.shapes.pill,
        color = Color.Transparent,
        contentColor = colors.onSurfaceMuted,
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Text(
            text = stringResource(Res.string.common_skip).uppercase(),
            style = RiteAppTheme.typography.eyebrow,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

internal enum class UndoVariant {
    Primary,
    Skip,
}

@Composable
private fun UndoPill(onClick: () -> Unit, variant: UndoVariant = UndoVariant.Primary,) {
    val colors = RiteAppTheme.colors
    val fg: Color =
        when (variant) {
            UndoVariant.Primary -> colors.primary
            UndoVariant.Skip -> colors.onSurfaceMuted
        }
    val border: Color =
        when (variant) {
            UndoVariant.Primary -> colors.primary.copy(alpha = 0.6f)
            UndoVariant.Skip -> colors.outline
        }
    Surface(
        onClick = onClick,
        shape = RiteAppTheme.shapes.pill,
        color = Color.Transparent,
        contentColor = fg,
        border = BorderStroke(1.dp, border),
    ) {
        Text(
            text = stringResource(Res.string.today_action_undo).uppercase(),
            style = RiteAppTheme.typography.eyebrow,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun Stamp(label: String, stateColor: Color,) {
    Surface(
        shape = RiteAppTheme.shapes.xs,
        color = Color.Transparent,
        contentColor = stateColor,
        border = BorderStroke(1.dp, stateColor.copy(alpha = 0.45f)),
    ) {
        Text(
            text = label.uppercase(),
            style = RiteAppTheme.typography.eyebrow,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
        )
    }
}
