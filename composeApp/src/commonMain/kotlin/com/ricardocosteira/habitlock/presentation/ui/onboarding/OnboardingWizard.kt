package com.ricardocosteira.habitlock.presentation.ui.onboarding

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ricardocosteira.habitlock.presentation.ui.BackHandler
import kotlinx.datetime.DayOfWeek

private const val ENTER_DURATION_MS = 300
private const val EXIT_DURATION_MS = 200

// M3 motion easing (spec §7)
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

@Composable
fun OnboardingWizard(
    state: OnboardingState,
    currentStep: Int,
    snackbarHostState: SnackbarHostState,
    onStepChange: (Int) -> Unit,
    onSkip: () -> Unit,
    onContinueFromStrictness: () -> Unit,
    onCreateHabit: () -> Unit,
    onSkipFirstHabit: () -> Unit,
    onPresetSelected: (OnboardingStrictnessPreset) -> Unit,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (com.ricardocosteira.habitlock.domain.models.HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onScheduleOptionChange: (ScheduleOption) -> Unit,
    onCustomDaysChange: (Set<DayOfWeek>) -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = currentStep > 0) {
        onStepChange(currentStep - 1)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OnboardingTopChrome(
                currentStep = currentStep,
                onSkip = onSkip,
                modifier = Modifier.fillMaxWidth()
            )

            if (reduceMotion) {
                Crossfade(
                    targetState = currentStep,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    label = "onboarding_step"
                ) { step ->
                    when (step) {
                        0 -> PhilosophyStep(modifier = Modifier.fillMaxSize(), reduceMotion = true)
                        1 -> StrictnessStep(
                            selectedPreset = state.selectedPreset,
                            onPresetSelected = onPresetSelected,
                            reduceMotion = reduceMotion,
                            modifier = Modifier.fillMaxSize()
                        )
                        2 -> FirstHabitStep(
                            habitName = state.habitName,
                            habitType = state.habitType,
                            targetValue = state.targetValue,
                            unit = state.unit,
                            scheduleOption = state.scheduleOption,
                            customDays = state.customDays,
                            onHabitNameChange = onHabitNameChange,
                            onHabitTypeChange = onHabitTypeChange,
                            onTargetValueChange = onTargetValueChange,
                            onUnitChange = onUnitChange,
                            onScheduleOptionChange = onScheduleOptionChange,
                            onCustomDaysChange = onCustomDaysChange,
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> Unit
                    }
                }
            } else {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        val isForward = targetState > initialState
                        val enterSlide = slideInHorizontally(
                            tween(ENTER_DURATION_MS, easing = EmphasizedDecelerate)
                        ) { if (isForward) it else -it } + fadeIn(tween(ENTER_DURATION_MS))
                        val exitSlide = slideOutHorizontally(
                            tween(EXIT_DURATION_MS, easing = EmphasizedAccelerate)
                        ) { if (isForward) -it else it } + fadeOut(tween(EXIT_DURATION_MS))
                        enterSlide togetherWith exitSlide
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = "onboarding_step"
                ) { step ->
                    when (step) {
                        0 -> PhilosophyStep(modifier = Modifier.fillMaxSize(), reduceMotion = reduceMotion)
                        1 -> StrictnessStep(
                            selectedPreset = state.selectedPreset,
                            onPresetSelected = onPresetSelected,
                            reduceMotion = reduceMotion,
                            modifier = Modifier.fillMaxSize()
                        )
                        2 -> FirstHabitStep(
                            habitName = state.habitName,
                            habitType = state.habitType,
                            targetValue = state.targetValue,
                            unit = state.unit,
                            scheduleOption = state.scheduleOption,
                            customDays = state.customDays,
                            onHabitNameChange = onHabitNameChange,
                            onHabitTypeChange = onHabitTypeChange,
                            onTargetValueChange = onTargetValueChange,
                            onUnitChange = onUnitChange,
                            onScheduleOptionChange = onScheduleOptionChange,
                            onCustomDaysChange = onCustomDaysChange,
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> Unit
                    }
                }
            }

            when (currentStep) {
                0 -> PhilosophyStepCta(
                    onAdvance = { onStepChange(currentStep + 1) },
                    modifier = Modifier.fillMaxWidth(),
                    reduceMotion = reduceMotion
                )
                1 -> StrictnessStepCta(
                    state = state,
                    onContinue = onContinueFromStrictness,
                    modifier = Modifier.fillMaxWidth(),
                    reduceMotion = reduceMotion
                )
                2 -> FirstHabitStepCta(
                    state = state,
                    onCreateHabit = onCreateHabit,
                    onSkip = onSkipFirstHabit,
                    modifier = Modifier.fillMaxWidth(),
                    reduceMotion = reduceMotion
                )
            }
        }
    }
}
