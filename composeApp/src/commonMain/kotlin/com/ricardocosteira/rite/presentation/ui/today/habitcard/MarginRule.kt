package com.ricardocosteira.rite.presentation.ui.today.habitcard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
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
fun MarginRule(state: HabitCardState, fillFraction: Float, modifier: Modifier = Modifier,) {
    val colors = RiteAppTheme.colors
    val dashed = state == HabitCardState.Skipped || state == HabitCardState.Suspended

    val trackColor: Color =
        when (state) {
            HabitCardState.Pending, HabitCardState.PendingInProgress -> colors.surfaceVariant

            HabitCardState.Completed ->
                Color(
                    red = lerpChannel(colors.surfaceVariant.red, colors.primary.red, 0.15f),
                    green = lerpChannel(colors.surfaceVariant.green, colors.primary.green, 0.15f),
                    blue = lerpChannel(colors.surfaceVariant.blue, colors.primary.blue, 0.15f),
                    alpha = 1f,
                )

            HabitCardState.Failed ->
                Color(
                    red = lerpChannel(colors.surfaceVariant.red, colors.error.red, 0.12f),
                    green = lerpChannel(colors.surfaceVariant.green, colors.error.green, 0.12f),
                    blue = lerpChannel(colors.surfaceVariant.blue, colors.error.blue, 0.12f),
                    alpha = 1f,
                )

            HabitCardState.Skipped, HabitCardState.Suspended -> Color.Transparent
        }

    val fillColor: Color =
        when (state) {
            HabitCardState.Pending -> colors.onSurface
            HabitCardState.PendingInProgress -> colors.primary
            HabitCardState.Completed -> colors.primary
            HabitCardState.Failed -> colors.error
            HabitCardState.Skipped, HabitCardState.Suspended -> Color.Transparent
        }

    val dashColor: Color =
        when (state) {
            HabitCardState.Skipped -> colors.onSurfaceSubtle
            HabitCardState.Suspended -> colors.suspend
            else -> Color.Transparent
        }

    val effectiveFill = if (state == HabitCardState.Failed) 1f else fillFraction.coerceIn(0f, 1f)

    Canvas(modifier = modifier.fillMaxHeight()) {
        val widthPx = (if (dashed) DASHED_WIDTH else RULE_WIDTH).toPx()
        val h = size.height

        if (dashed) {
            drawLine(
                color = dashColor,
                start = Offset(widthPx / 2f, 0f),
                end = Offset(widthPx / 2f, h),
                strokeWidth = widthPx,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON, DASH_OFF), 0f),
            )
        } else {
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, 0f),
                size = Size(widthPx, h),
                cornerRadius = CornerRadius(CORNER_RADIUS_PX, CORNER_RADIUS_PX),
            )
            if (effectiveFill > 0f) {
                val fillHeight = h * effectiveFill
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(0f, h - fillHeight),
                    size = Size(widthPx, fillHeight),
                    cornerRadius = CornerRadius(CORNER_RADIUS_PX, CORNER_RADIUS_PX),
                )
            }
        }
    }
}

private fun lerpChannel(a: Float, b: Float, t: Float): Float = a + (b - a) * t
