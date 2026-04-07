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
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
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
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_cd_onboarding_step
import rite.composeapp.generated.resources.common_skip

private const val DONE_DOT_ALPHA = 0.45f

@Composable
fun OnboardingTopChrome(
    currentStep: Int,
    totalSteps: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stepDescription =
        stringResource(Res.string.common_cd_onboarding_step, currentStep + 1, totalSteps)

    Row(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProgressDots(
            currentStep = currentStep,
            totalSteps = totalSteps,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = stepDescription
                }
        )

        TextButton(onClick = onSkip) {
            Text(
                text = stringResource(Res.string.common_skip),
                style = RiteAppTheme.typography.labelLarge,
                color = RiteAppTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressDots(currentStep: Int, totalSteps: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
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
        RiteAppTheme.colorScheme.surfaceVariant
    } else {
        RiteAppTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .width(animatedWidth)
            .height(6.dp)
            .alpha(animatedAlpha)
            .background(color = color, shape = RoundedCornerShape(3.dp))
    )
}
