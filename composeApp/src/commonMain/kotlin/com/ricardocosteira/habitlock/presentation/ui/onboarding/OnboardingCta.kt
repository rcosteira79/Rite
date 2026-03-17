package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.HabitType
import kotlinx.coroutines.launch

@Composable
fun OnboardingCta(
    currentStep: Int,
    state: OnboardingState,
    onAdvance: () -> Unit,
    onContinueFromStrictness: () -> Unit,
    onCreateHabit: () -> Unit,
    onSkipFirstHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val translateY = remember { Animatable(16f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        launch { translateY.animateTo(0f, tween(200)) }
        launch { alpha.animateTo(1f, tween(200)) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .alpha(alpha.value)
            .graphicsLayer { translationY = translateY.value.dp.toPx() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isLoading = when (currentStep) {
            1 -> state.isApplyingPreset
            2 -> state.isCreatingHabit
            else -> false
        }

        val isEnabled = when (currentStep) {
            2 -> state.habitName.isNotBlank() &&
                    (state.habitType == HabitType.BINARY || state.targetValue.isNotBlank())
            else -> true
        }

        val ctaLabel = when (currentStep) {
            2 -> "Create habit"
            else -> "Continue"
        }

        val onCtaClick: () -> Unit = when (currentStep) {
            0 -> onAdvance
            1 -> onContinueFromStrictness
            2 -> onCreateHabit
            else -> onAdvance
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        } else {
            Button(
                onClick = onCtaClick,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(ctaLabel)
            }
        }

        if (currentStep == 2) {
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = onSkipFirstHabit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skip for now",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
