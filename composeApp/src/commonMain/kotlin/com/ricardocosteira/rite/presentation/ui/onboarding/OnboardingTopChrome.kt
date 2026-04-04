package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_cd_onboarding_step
import habitlock.composeapp.generated.resources.common_skip
import org.jetbrains.compose.resources.stringResource

private const val TOTAL_STEPS = 3
private const val DONE_DOT_ALPHA = 0.45f

@Composable
fun OnboardingTopChrome(currentStep: Int, onSkip: () -> Unit, modifier: Modifier = Modifier) {
    val stepDescription =
        stringResource(Res.string.common_cd_onboarding_step, currentStep + 1, TOTAL_STEPS)

    Row(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProgressDots(
            currentStep = currentStep,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = stepDescription
                }
        )

        TextButton(onClick = onSkip) {
            Text(
                text = stringResource(Res.string.common_skip),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressDots(currentStep: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(TOTAL_STEPS) { index ->
            StepDot(
                state = when {
                    index < currentStep -> DotState.Done
                    index == currentStep -> DotState.Active
                    else -> DotState.Inactive
                }
            )
        }
    }
}

private enum class DotState { Active, Done, Inactive }

@Composable
private fun StepDot(state: DotState, modifier: Modifier = Modifier) {
    val targetWidth = if (state == DotState.Active) 20.dp else 6.dp
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dot_width"
    )

    val targetAlpha = if (state == DotState.Inactive) {
        1f
    } else if (state == DotState.Done) {
        DONE_DOT_ALPHA
    } else {
        1f
    }
    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        label = "dot_alpha"
    )

    val color = if (state == DotState.Inactive) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .width(animatedWidth)
            .height(6.dp)
            .alpha(animatedAlpha)
            .background(color = color, shape = RoundedCornerShape(3.dp))
    )
}
