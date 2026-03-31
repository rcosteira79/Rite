package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.presentation.ui.isReduceMotionEnabled
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.first_habit_error_empty_name
import habitlock.composeapp.generated.resources.first_habit_error_invalid_target_value
import habitlock.composeapp.generated.resources.first_habit_error_missing_target_value
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingRoute(
    viewModel: OnboardingViewModel,
    snackbarHostState: SnackbarHostState,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val reduceMotion = isReduceMotionEnabled()

    val messageEmptyName = stringResource(Res.string.first_habit_error_empty_name)
    val messageMissingTarget = stringResource(Res.string.first_habit_error_missing_target_value)
    val messageInvalidTarget = stringResource(Res.string.first_habit_error_invalid_target_value)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToToday -> onFinished()

                OnboardingEvent.EmptyHabitName -> snackbarHostState.showSnackbar(messageEmptyName)

                OnboardingEvent.MissingTargetValue ->
                    snackbarHostState.showSnackbar(
                        messageMissingTarget
                    )

                OnboardingEvent.InvalidTargetValue ->
                    snackbarHostState.showSnackbar(
                        messageInvalidTarget
                    )
            }
        }
    }

    OnboardingWizard(
        state = state,
        currentStep = state.currentStep,
        snackbarHostState = snackbarHostState,
        onStepChange = viewModel::setCurrentStep,
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
        onSelectedDaysChange = viewModel::updateSelectedDays,
        modifier = modifier
    )
}
