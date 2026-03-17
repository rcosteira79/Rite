package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.presentation.ui.isReduceMotionEnabled

@Composable
fun OnboardingRoute(
    viewModel: OnboardingViewModel,
    snackbarHostState: SnackbarHostState,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var currentStep by remember { mutableIntStateOf(0) }
    val reduceMotion = isReduceMotionEnabled()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToFirstHabit -> currentStep = 2
                OnboardingEvent.NavigateToToday -> onFinished()
                is OnboardingEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    OnboardingWizard(
        state = state,
        currentStep = currentStep,
        snackbarHostState = snackbarHostState,
        onStepChange = { currentStep = it },
        onSkip = viewModel::skipToToday,
        reduceMotion = reduceMotion,
        onContinueFromStrictness = viewModel::continueFromStrictness,
        onCreateHabit = viewModel::createFirstHabit,
        onSkipFirstHabit = viewModel::skipFirstHabit,
        onPresetSelected = viewModel::selectPreset,
        onHabitNameChange = viewModel::updateHabitName,
        onHabitTypeChange = viewModel::updateHabitType,
        onTargetValueChange = viewModel::updateTargetValue,
        onUnitChange = viewModel::updateUnit,
        modifier = modifier
    )
}
