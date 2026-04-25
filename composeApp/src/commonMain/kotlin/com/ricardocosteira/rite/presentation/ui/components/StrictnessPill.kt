package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

enum class StrictnessPreset { Flexible, Balanced, Unwavering }

@Composable
fun StrictnessPill(
    preset: StrictnessPreset,
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    val colors = RiteAppTheme.colors
    val dotAlpha = if (animated) {
        val t = rememberInfiniteTransition(label = "strictness-dot")
        val a by t.animateFloat(
            initialValue = 0.45f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = RiteAppTheme.motion.deliberate.inWholeMilliseconds.toInt(),
                    easing = RiteAppTheme.motion.easeQuiet
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "strictness-dot-alpha"
        )
        a
    } else {
        1f
    }

    Surface(
        modifier = modifier,
        shape = RiteAppTheme.shapes.pill,
        color = colors.surfaceContainer,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(dotAlpha)
                    .background(colors.primary, CircleShape)
            )
            Text(
                text = preset.name,
                style = RiteAppTheme.typography.labelSmall
            )
        }
    }
}
