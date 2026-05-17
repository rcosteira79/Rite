package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.BackHandler
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.first_habit_strap_label
import rite.composeapp.generated.resources.notifications_strap_label
import rite.composeapp.generated.resources.philosophy_strap_label
import rite.composeapp.generated.resources.strictness_strap_label

private const val ENTER_DURATION_MS = 300
private const val EXIT_DURATION_MS = 200

// M3 motion easing (spec §7)
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

@Composable
fun OnboardingWizard(
    state: OnboardingState,
    currentStep: OnboardingStep,
    snackbarHostState: SnackbarHostState,
    onStepChange: (OnboardingStep) -> Unit,
    onContinueFromNotificationPermission: () -> Unit,
    onEnableNotifications: () -> Unit,
    onContinueFromStrictness: () -> Unit,
    onCreateHabit: () -> Unit,
    onSkipFirstHabit: () -> Unit,
    onPresetSelected: (OnboardingStrictnessPreset) -> Unit,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onScheduleKindChange: (OnboardingScheduleKind) -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = currentStep.ordinal > 0) {
        onStepChange(OnboardingStep.entries[currentStep.ordinal - 1])
    }

    val candidateStepNames = buildList {
        add(stringResource(OnboardingStep.PHILOSOPHY.strapLabel))
        add(stringResource(OnboardingStep.STRICTNESS.strapLabel))
        add(stringResource(OnboardingStep.FIRST_HABIT.strapLabel))
        if (state.showNotificationStep) add(stringResource(OnboardingStep.NOTIFICATIONS.strapLabel))
    }.toImmutableList()

    Scaffold(modifier = modifier.fillMaxSize(), snackbarHost = {
        SnackbarHost(snackbarHostState)
    }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OnboardingStepStrap(
                step = currentStep.ordinal + 1,
                totalSteps = state.totalSteps,
                stepName = stringResource(currentStep.strapLabel),
                allStepNames = candidateStepNames,
                reduceMotion = reduceMotion,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 4.dp)
            )

            if (reduceMotion) {
                Crossfade(
                    targetState = currentStep,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    label = "onboarding_step"
                ) { step ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        StepContent(
                            step = step,
                            state = state,
                            onStepChange = onStepChange,
                            onContinueFromNotificationPermission = onContinueFromNotificationPermission,
                            onEnableNotifications = onEnableNotifications,
                            onContinueFromStrictness = onContinueFromStrictness,
                            onCreateHabit = onCreateHabit,
                            onSkipFirstHabit = onSkipFirstHabit,
                            onPresetSelected = onPresetSelected,
                            onHabitNameChange = onHabitNameChange,
                            onHabitTypeChange = onHabitTypeChange,
                            onTargetValueChange = onTargetValueChange,
                            onUnitChange = onUnitChange,
                            onScheduleKindChange = onScheduleKindChange,
                            reduceMotion = true
                        )
                    }
                }
            } else {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        val isForward = targetState.ordinal > initialState.ordinal
                        val enterSlide = slideInHorizontally(
                            tween(ENTER_DURATION_MS, easing = EmphasizedDecelerate)
                        ) {
                            if (isForward) it else -it
                        } + fadeIn(tween(ENTER_DURATION_MS))
                        val exitSlide = slideOutHorizontally(
                            tween(EXIT_DURATION_MS, easing = EmphasizedAccelerate)
                        ) {
                            if (isForward) -it else it
                        } + fadeOut(tween(EXIT_DURATION_MS))
                        enterSlide togetherWith exitSlide
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    label = "onboarding_step"
                ) { step ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        StepContent(
                            step = step,
                            state = state,
                            onStepChange = onStepChange,
                            onContinueFromNotificationPermission = onContinueFromNotificationPermission,
                            onEnableNotifications = onEnableNotifications,
                            onContinueFromStrictness = onContinueFromStrictness,
                            onCreateHabit = onCreateHabit,
                            onSkipFirstHabit = onSkipFirstHabit,
                            onPresetSelected = onPresetSelected,
                            onHabitNameChange = onHabitNameChange,
                            onHabitTypeChange = onHabitTypeChange,
                            onTargetValueChange = onTargetValueChange,
                            onUnitChange = onUnitChange,
                            onScheduleKindChange = onScheduleKindChange,
                            reduceMotion = reduceMotion
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.StepContent(
    step: OnboardingStep,
    state: OnboardingState,
    onStepChange: (OnboardingStep) -> Unit,
    onContinueFromNotificationPermission: () -> Unit,
    onEnableNotifications: () -> Unit,
    onContinueFromStrictness: () -> Unit,
    onCreateHabit: () -> Unit,
    onSkipFirstHabit: () -> Unit,
    onPresetSelected: (OnboardingStrictnessPreset) -> Unit,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onScheduleKindChange: (OnboardingScheduleKind) -> Unit,
    reduceMotion: Boolean
) {
    when (step) {
        OnboardingStep.PHILOSOPHY -> {
            PhilosophyStep(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                reduceMotion = reduceMotion
            )
            PhilosophyStepCta(
                onAdvance = { onStepChange(OnboardingStep.STRICTNESS) },
                modifier = Modifier.fillMaxWidth(),
                reduceMotion = reduceMotion
            )
        }

        OnboardingStep.STRICTNESS -> {
            StrictnessStep(
                selectedPreset = state.selectedPreset,
                onPresetSelected = onPresetSelected,
                reduceMotion = reduceMotion,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            StrictnessStepCta(
                state = state,
                onContinue = onContinueFromStrictness,
                modifier = Modifier.fillMaxWidth(),
                reduceMotion = reduceMotion
            )
        }

        OnboardingStep.FIRST_HABIT -> {
            FirstHabitStep(
                habitName = state.habitName,
                habitType = state.habitType,
                targetValue = state.targetValue,
                unit = state.unit,
                scheduleKind = state.scheduleKind,
                onHabitNameChange = onHabitNameChange,
                onHabitTypeChange = onHabitTypeChange,
                onTargetValueChange = onTargetValueChange,
                onUnitChange = onUnitChange,
                onScheduleKindChange = onScheduleKindChange,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            FirstHabitStepCta(
                state = state,
                onCreateHabit = onCreateHabit,
                onSkip = onSkipFirstHabit,
                modifier = Modifier.fillMaxWidth(),
                reduceMotion = reduceMotion
            )
        }

        OnboardingStep.NOTIFICATIONS -> {
            NotificationPermissionStep(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                reduceMotion = reduceMotion
            )
            NotificationPermissionStepCta(
                onEnableNotifications = onEnableNotifications,
                onMaybeLater = onContinueFromNotificationPermission,
                modifier = Modifier.fillMaxWidth(),
                reduceMotion = reduceMotion
            )
        }
    }
}

private val OnboardingStep.strapLabel: StringResource
    get() = when (this) {
        OnboardingStep.PHILOSOPHY -> Res.string.philosophy_strap_label
        OnboardingStep.STRICTNESS -> Res.string.strictness_strap_label
        OnboardingStep.FIRST_HABIT -> Res.string.first_habit_strap_label
        OnboardingStep.NOTIFICATIONS -> Res.string.notifications_strap_label
    }
