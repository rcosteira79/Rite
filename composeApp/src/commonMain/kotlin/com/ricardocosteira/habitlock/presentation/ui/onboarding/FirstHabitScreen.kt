package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.domain.models.HabitType
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_placeholder_habit_name
import habitlock.composeapp.generated.resources.common_placeholder_target_value
import habitlock.composeapp.generated.resources.common_quantitative
import habitlock.composeapp.generated.resources.first_habit_button_create
import habitlock.composeapp.generated.resources.first_habit_button_skip
import habitlock.composeapp.generated.resources.first_habit_heading
import habitlock.composeapp.generated.resources.first_habit_label_name
import habitlock.composeapp.generated.resources.first_habit_label_target_value
import habitlock.composeapp.generated.resources.first_habit_label_type
import habitlock.composeapp.generated.resources.first_habit_label_unit
import habitlock.composeapp.generated.resources.first_habit_placeholder_unit
import habitlock.composeapp.generated.resources.first_habit_subtext
import habitlock.composeapp.generated.resources.first_habit_type_binary
import org.jetbrains.compose.resources.stringResource

@Composable
fun FirstHabitScreen(
    onNavigateToToday: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.onboardingViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToStrictness -> Unit  // not reachable from FirstHabitScreen
                OnboardingEvent.NavigateToFirstHabit -> Unit  // not reachable from FirstHabitScreen
                OnboardingEvent.NavigateToToday -> onNavigateToToday()
                is OnboardingEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    FirstHabitScreen(
        habitName = state.habitName,
        habitType = state.habitType,
        targetValue = state.targetValue,
        unit = state.unit,
        isLoading = state.isCreatingHabit,
        onHabitNameChange = viewModel::updateHabitName,
        onHabitTypeChange = viewModel::updateHabitType,
        onTargetValueChange = viewModel::updateTargetValue,
        onUnitChange = viewModel::updateUnit,
        onCreateHabit = viewModel::createFirstHabit,
        onSkip = viewModel::skipFirstHabit
    )
}

@Composable
private fun FirstHabitScreen(
    habitName: String,
    habitType: HabitType,
    targetValue: String,
    unit: String,
    isLoading: Boolean,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onCreateHabit: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.first_habit_heading),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.first_habit_subtext),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = habitName,
            onValueChange = onHabitNameChange,
            label = { Text(stringResource(Res.string.first_habit_label_name)) },
            placeholder = { Text(stringResource(Res.string.common_placeholder_habit_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.first_habit_label_type),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = habitType == HabitType.BINARY,
                onClick = { onHabitTypeChange(HabitType.BINARY) },
                label = { Text(stringResource(Res.string.first_habit_type_binary)) }
            )
            FilterChip(
                selected = habitType == HabitType.QUANTITATIVE,
                onClick = { onHabitTypeChange(HabitType.QUANTITATIVE) },
                label = { Text(stringResource(Res.string.common_quantitative)) }
            )
        }

        if (habitType == HabitType.QUANTITATIVE) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = targetValue,
                onValueChange = onTargetValueChange,
                label = { Text(stringResource(Res.string.first_habit_label_target_value)) },
                placeholder = { Text(stringResource(Res.string.common_placeholder_target_value)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = unit,
                onValueChange = onUnitChange,
                label = { Text(stringResource(Res.string.first_habit_label_unit)) },
                placeholder = { Text(stringResource(Res.string.first_habit_placeholder_unit)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onCreateHabit,
                modifier = Modifier.fillMaxWidth(),
                enabled = habitName.isNotBlank() &&
                    (habitType == HabitType.BINARY || targetValue.isNotBlank())
            ) {
                Text(stringResource(Res.string.first_habit_button_create))
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onSkip) {
                Text(stringResource(Res.string.first_habit_button_skip))
            }
        }
    }
}

