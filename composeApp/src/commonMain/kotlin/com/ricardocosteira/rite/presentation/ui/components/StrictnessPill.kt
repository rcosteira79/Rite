package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

enum class StrictnessPreset { Flexible, Balanced, Unwavering }

private const val PULSE_PERIOD_MS = 2400
private val DOT_DIAMETER = 6.dp
private val HALO_MAX_RADIUS = 6.dp
private const val HALO_PEAK_ALPHA = 0.6f

@Composable
fun StrictnessPill(
    preset: StrictnessPreset,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    showCap: Boolean = true
) {
    val colors = RiteAppTheme.colors
    val typography = RiteAppTheme.typography
    val motion = RiteAppTheme.motion

    val pulseT: Float = if (animated) {
        val transition = rememberInfiniteTransition(label = "strictness-pulse")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = PULSE_PERIOD_MS,
                    easing = motion.easeQuiet
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "strictness-pulse-t"
        )
        value
    } else {
        0f
    }

    Surface(
        modifier = modifier,
        shape = RiteAppTheme.shapes.pill,
        color = colors.surface,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(
                start = 10.dp,
                end = 12.dp,
                top = 5.dp,
                bottom = 5.dp
            )
        ) {
            PulsingDot(
                dotColor = colors.primary,
                pulseT = pulseT
            )
            if (showCap) {
                Text(
                    text = "Strictness".uppercase(),
                    style = typography.mono.copy(
                        fontSize = 10.sp,
                        letterSpacing = 1.4.sp,
                        color = colors.onSurfaceSubtle
                    )
                )
            }
            Text(
                text = preset.name,
                style = typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    letterSpacing = 0.sp
                )
            )
        }
    }
}

@Composable
private fun PulsingDot(dotColor: Color, pulseT: Float) {
    Box(
        modifier = Modifier
            .size(DOT_DIAMETER)
            .drawBehind {
                val center = Offset(size.width / 2f, size.height / 2f)
                val dotRadius = size.minDimension / 2f
                if (pulseT > 0f) {
                    val haloRadius = dotRadius + HALO_MAX_RADIUS.toPx() * pulseT
                    val haloAlpha = HALO_PEAK_ALPHA * (1f - pulseT)
                    drawCircle(
                        color = dotColor.copy(alpha = haloAlpha),
                        center = center,
                        radius = haloRadius
                    )
                }
                drawCircle(
                    color = dotColor,
                    center = center,
                    radius = dotRadius
                )
            }
    )
}
