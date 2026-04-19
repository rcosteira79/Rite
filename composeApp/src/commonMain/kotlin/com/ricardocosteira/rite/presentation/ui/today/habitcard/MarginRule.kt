package com.ricardocosteira.rite.presentation.ui.today.habitcard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

private val RULE_WIDTH = 5.dp
private val DASHED_WIDTH = 1.dp
private const val CORNER_RADIUS_PX = 1f
private const val DASH_ON = 3f
private const val DASH_OFF = 3f

@Composable
fun MarginRule(state: HabitCardState, fillFraction: Float, modifier: Modifier = Modifier) {
    val colors = RiteAppTheme.colors
    val motion = RiteAppTheme.motion
    val animSpec = tween<Float>(
        durationMillis = motion.deliberate.inWholeMilliseconds.toInt(),
        easing = motion.easeQuiet,
    )

    val dashedTarget = if (state == HabitCardState.Skipped || state == HabitCardState.Suspended) {
        1f
    } else {
        0f
    }
    val dashedT by animateFloatAsState(
        targetValue = dashedTarget,
        animationSpec = animSpec,
        label = "margin-rule-dashed",
    )

    val fillColorTarget: Color = when (state) {
        HabitCardState.Pending -> colors.onSurface

        HabitCardState.PendingInProgress, HabitCardState.Completed -> colors.primary

        HabitCardState.Failed -> colors.error

        // Solid is hidden via alpha in dashed states, so keep the target stable
        // to avoid a meaningless color interpolation.
        HabitCardState.Skipped, HabitCardState.Suspended -> colors.primary
    }
    val fillColor by animateColorAsState(
        targetValue = fillColorTarget,
        animationSpec = tween(
            durationMillis = motion.deliberate.inWholeMilliseconds.toInt(),
            easing = motion.easeQuiet,
        ),
        label = "margin-rule-fill-color",
    )

    val dashColorTarget: Color = when (state) {
        HabitCardState.Suspended -> colors.suspend
        else -> colors.onSurfaceSubtle
    }
    val dashColor by animateColorAsState(
        targetValue = dashColorTarget,
        animationSpec = tween(
            durationMillis = motion.deliberate.inWholeMilliseconds.toInt(),
            easing = motion.easeQuiet,
        ),
        label = "margin-rule-dash-color",
    )

    val fillTarget = if (state == HabitCardState.Failed) 1f else fillFraction.coerceIn(0f, 1f)
    val animatedFill by animateFloatAsState(
        targetValue = fillTarget,
        animationSpec = animSpec,
        label = "margin-rule-fill",
    )

    val trackColor = colors.outline

    Canvas(modifier = modifier.fillMaxHeight()) {
        val ruleWidthPx = RULE_WIDTH.toPx()
        val dashedWidthPx = DASHED_WIDTH.toPx()
        val h = size.height
        val solidAlpha = 1f - dashedT

        if (solidAlpha > 0f) {
            drawRoundRect(
                color = trackColor.copy(alpha = solidAlpha),
                topLeft = Offset(0f, 0f),
                size = Size(ruleWidthPx, h),
                cornerRadius = CornerRadius(CORNER_RADIUS_PX, CORNER_RADIUS_PX),
            )
            if (animatedFill > 0f) {
                val fillHeight = h * animatedFill
                drawRoundRect(
                    color = fillColor.copy(alpha = solidAlpha),
                    topLeft = Offset(0f, h - fillHeight),
                    size = Size(ruleWidthPx, fillHeight),
                    cornerRadius = CornerRadius(CORNER_RADIUS_PX, CORNER_RADIUS_PX),
                )
            }
        }

        if (dashedT > 0f) {
            drawLine(
                color = dashColor.copy(alpha = dashedT),
                start = Offset(dashedWidthPx / 2f, 0f),
                end = Offset(dashedWidthPx / 2f, h),
                strokeWidth = dashedWidthPx,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON, DASH_OFF), 0f),
            )
        }
    }
}
