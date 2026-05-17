package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

private const val CROSSFADE_MS = 220
private const val BAR_COLOR_MS = 320

/**
 * Combined step header: `[STEP NAME mono] ━━━━━━━ [N / total mono]`.
 *
 * Both side slots reserve fixed width by stacking invisible reference Texts for
 * every possible label, so the bar's start/end positions stay put as the step
 * name and counter change during navigation.
 */
@Composable
fun OnboardingStepStrap(
    step: Int,
    totalSteps: Int,
    stepName: String,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    allStepNames: List<String> = listOf(stepName)
) {
    val nameStyle = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 2.2.sp)
    val countStyle = RiteAppTheme.typography.labelSmall.copy(
        letterSpacing = 1.8.sp,
        fontFeatureSettings = "tnum"
    )
    val nameSpec = if (reduceMotion) snap<Float>() else tween<Float>(CROSSFADE_MS)
    val barSpec = if (reduceMotion) snap<Color>() else tween<Color>(BAR_COLOR_MS)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FixedSlot(
            candidates = allStepNames.map { it.uppercase() },
            style = nameStyle,
            alignment = Alignment.CenterStart
        ) {
            Crossfade(
                targetState = stepName,
                animationSpec = nameSpec,
                label = "stepName"
            ) { name ->
                Text(
                    text = name.uppercase(),
                    style = nameStyle,
                    color = RiteAppTheme.colors.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(totalSteps) { index ->
                val target = if (index < step) {
                    RiteAppTheme.colors.onSurface
                } else {
                    RiteAppTheme.colors.outline
                }
                val color by animateColorAsState(
                    targetValue = target,
                    animationSpec = barSpec,
                    label = "bar$index"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(color)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Crossfade(
                targetState = step,
                animationSpec = nameSpec,
                label = "stepDigit"
            ) { stepNum ->
                Text(
                    text = stepNum.toString(),
                    style = countStyle,
                    color = RiteAppTheme.colors.onSurfaceVariant
                )
            }
            Text(
                text = " / $totalSteps",
                style = countStyle,
                color = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FixedSlot(
    candidates: List<String>,
    style: TextStyle,
    alignment: Alignment,
    content: @Composable () -> Unit
) {
    Box(contentAlignment = alignment) {
        candidates.forEach { candidate ->
            Text(
                text = candidate,
                style = style,
                color = Color.Transparent,
                modifier = Modifier.clearAndSetSemantics { }
            )
        }
        content()
    }
}
