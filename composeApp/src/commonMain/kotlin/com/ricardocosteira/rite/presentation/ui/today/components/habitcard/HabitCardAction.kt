package com.ricardocosteira.rite.presentation.ui.today.components.habitcard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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

// Shared key for the trailing pill: Skip (pending) morphs into Undo (completed/skipped),
// staying in place on the right while the primary action fades out separately.
private const val TAIL_PILL_KEY = "habit-card-action-tail-pill"

@OptIn(ExperimentalSharedTransitionApi::class)
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
    modifier: Modifier = Modifier
) {
    SharedTransitionLayout(modifier = modifier) {
        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it },
            label = "habit-card-action"
        ) { currentState ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (currentState) {
                    HabitCardState.Pending, HabitCardState.PendingInProgress -> {
                        val primaryScaleModifier = Modifier.animateEnterExit(
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        )
                        when (type) {
                            HabitType.BINARY -> CheckCircle(
                                onClick = onComplete,
                                modifier = primaryScaleModifier
                            )

                            HabitType.QUANTITATIVE -> PlusChip(
                                label = stringResource(
                                    Res.string.today_action_increment_short,
                                    defaultIncrement
                                ),
                                onClick = onIncrement,
                                modifier = primaryScaleModifier
                            )
                        }
                        if (!skipLocked) {
                            SkipChip(
                                onClick = onSkip,
                                modifier = Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(TAIL_PILL_KEY),
                                    animatedVisibilityScope = this@AnimatedContent,
                                    enter = scaleIn(),
                                    exit = scaleOut()
                                )
                            )
                        }
                    }

                    HabitCardState.Completed -> UndoPill(
                        onClick = onUndo,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(TAIL_PILL_KEY),
                            animatedVisibilityScope = this@AnimatedContent,
                            enter = scaleIn(),
                            exit = scaleOut()
                        )
                    )

                    HabitCardState.Skipped -> {
                        Stamp(
                            label = stringResource(Res.string.today_stamp_skipped),
                            stateColor = RiteAppTheme.colors.onSurfaceMuted
                        )
                        UndoPill(
                            onClick = onUndo,
                            variant = UndoVariant.Skip,
                            modifier = Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(TAIL_PILL_KEY),
                                animatedVisibilityScope = this@AnimatedContent,
                                enter = scaleIn(),
                                exit = scaleOut()
                            )
                        )
                    }

                    HabitCardState.Failed -> Stamp(
                        label = stringResource(Res.string.today_stamp_missed),
                        stateColor = RiteAppTheme.colors.error
                    )

                    HabitCardState.Suspended -> Stamp(
                        label = stringResource(Res.string.today_stamp_paused),
                        stateColor = RiteAppTheme.colors.suspend
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckCircle(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = RiteAppTheme.colors
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
        contentColor = colors.onSurface,
        border = BorderStroke(1.5.dp, colors.onSurface),
        modifier = modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(Res.string.today_action_complete),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PlusChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = RiteAppTheme.colors
    Surface(
        onClick = onClick,
        shape = RiteAppTheme.shapes.pill,
        color = Color.Transparent,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.onSurface),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = RiteAppTheme.typography.mono,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun SkipChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = RiteAppTheme.colors
    Surface(
        onClick = onClick,
        shape = RiteAppTheme.shapes.pill,
        color = Color.Transparent,
        contentColor = colors.onSurfaceMuted,
        border = BorderStroke(1.dp, colors.outline),
        modifier = modifier
    ) {
        Text(
            text = stringResource(Res.string.common_skip).uppercase(),
            style = RiteAppTheme.typography.eyebrow,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

internal enum class UndoVariant {
    Primary,
    Skip
}

@Composable
private fun UndoPill(
    onClick: () -> Unit,
    variant: UndoVariant = UndoVariant.Primary,
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
    ) {
        Text(
            text = stringResource(Res.string.today_action_undo).uppercase(),
            style = RiteAppTheme.typography.eyebrow,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun Stamp(label: String, stateColor: Color) {
    val strokeColor = stateColor.copy(alpha = 0.45f)
    Box(
        modifier = Modifier
            .drawBehind {
                val corner = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                val strokeWidth = 1.dp.toPx()
                drawRoundRect(
                    color = strokeColor,
                    cornerRadius = corner,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    ),
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth)
                )
            }
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = RiteAppTheme.typography.eyebrow,
            color = stateColor
        )
    }
}
