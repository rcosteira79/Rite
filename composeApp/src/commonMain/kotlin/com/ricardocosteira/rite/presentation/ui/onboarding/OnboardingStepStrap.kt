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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

private const val CROSSFADE_MS = 220
private const val BAR_COLOR_MS = 320

/**
 * Combined step header: `[STEP NAME mono] ━━━━━━━ [N / total mono]`.
 *
 * Lives at the wizard level (not inside each step composable) so it persists
 * across step transitions. The step name and the `N / total` text cross-fade;
 * each bar's color animates between filled and unfilled, so the progress fills
 * or empties in place when navigating forward or back.
 */
@Composable
fun OnboardingStepStrap(
    step: Int,
    totalSteps: Int,
    stepName: String,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    val nameSpec = if (reduceMotion) snap<Float>() else tween<Float>(CROSSFADE_MS)
    val barSpec = if (reduceMotion) snap<Color>() else tween<Color>(BAR_COLOR_MS)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Crossfade(targetState = stepName, animationSpec = nameSpec, label = "stepName") { name ->
            Text(
                text = name.uppercase(),
                style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 2.2.sp),
                color = RiteAppTheme.colors.onSurfaceVariant
            )
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

        Crossfade(
            targetState = "$step / $totalSteps",
            animationSpec = nameSpec,
            label = "stepCount"
        ) { count ->
            Text(
                text = count,
                style = RiteAppTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                color = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}
