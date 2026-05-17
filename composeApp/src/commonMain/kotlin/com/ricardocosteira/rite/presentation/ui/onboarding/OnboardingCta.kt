package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.components.RiteButton
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.first_habit_button_create
import rite.composeapp.generated.resources.first_habit_button_skip
import rite.composeapp.generated.resources.notifications_cta_enable
import rite.composeapp.generated.resources.notifications_cta_later
import rite.composeapp.generated.resources.philosophy_cta_accept
import rite.composeapp.generated.resources.strictness_cta_continue

@Composable
private fun CtaContainer(
    step: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val translateYAnim = remember { Animatable(16f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            translateYAnim.snapTo(0f)
            alphaAnim.snapTo(1f)
            return@LaunchedEffect
        }
        delay(200)
        launch { translateYAnim.animateTo(0f, tween(200)) }
        launch { alphaAnim.animateTo(1f, tween(200)) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .graphicsLayer {
                alpha = alphaAnim.value
                translationY = translateYAnim.value.dp.toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingStepIndicator(
            step = step,
            totalSteps = totalSteps,
            modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)
        )
        content()
    }
}

@Composable
internal fun PhilosophyStepCta(
    step: Int,
    totalSteps: Int,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    CtaContainer(
        step = step,
        totalSteps = totalSteps,
        modifier = modifier,
        reduceMotion = reduceMotion
    ) {
        RiteButton(onClick = onAdvance) {
            Text(stringResource(Res.string.philosophy_cta_accept))
        }
    }
}

@Composable
internal fun StrictnessStepCta(
    step: Int,
    totalSteps: Int,
    state: OnboardingState,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    CtaContainer(
        step = step,
        totalSteps = totalSteps,
        modifier = modifier,
        reduceMotion = reduceMotion
    ) {
        if (state.isApplyingPreset) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        } else {
            RiteButton(onClick = onContinue) {
                Text(stringResource(Res.string.strictness_cta_continue))
            }
        }
    }
}

@Composable
internal fun FirstHabitStepCta(
    step: Int,
    totalSteps: Int,
    state: OnboardingState,
    onCreateHabit: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    val isEnabled = state.habitName.isNotBlank() &&
        (state.habitType == HabitType.BINARY || state.targetValue.isNotBlank()) &&
        state.selectedDays.isNotEmpty()

    CtaContainer(
        step = step,
        totalSteps = totalSteps,
        modifier = modifier,
        reduceMotion = reduceMotion
    ) {
        if (state.isCreatingHabit) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        } else {
            RiteButton(onClick = onCreateHabit, enabled = isEnabled) {
                Text(stringResource(Res.string.first_habit_button_create))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.first_habit_button_skip),
                style = RiteAppTheme.typography.labelLarge,
                color = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun NotificationPermissionStepCta(
    step: Int,
    totalSteps: Int,
    onEnableNotifications: () -> Unit,
    onMaybeLater: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    CtaContainer(
        step = step,
        totalSteps = totalSteps,
        modifier = modifier,
        reduceMotion = reduceMotion
    ) {
        RiteButton(onClick = onEnableNotifications) {
            Text(stringResource(Res.string.notifications_cta_enable))
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = onMaybeLater, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.notifications_cta_later),
                style = RiteAppTheme.typography.labelLarge,
                color = RiteAppTheme.colors.onSurfaceVariant
            )
        }
    }
}
