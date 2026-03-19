package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_continue
import habitlock.composeapp.generated.resources.common_skip
import habitlock.composeapp.generated.resources.strictness_badge_recommended
import habitlock.composeapp.generated.resources.strictness_balanced_description
import habitlock.composeapp.generated.resources.strictness_balanced_label
import habitlock.composeapp.generated.resources.strictness_balanced_rule_1
import habitlock.composeapp.generated.resources.strictness_balanced_rule_2
import habitlock.composeapp.generated.resources.strictness_balanced_rule_3
import habitlock.composeapp.generated.resources.strictness_balanced_rule_4
import habitlock.composeapp.generated.resources.strictness_flexible_description
import habitlock.composeapp.generated.resources.strictness_flexible_label
import habitlock.composeapp.generated.resources.strictness_flexible_rule_1
import habitlock.composeapp.generated.resources.strictness_flexible_rule_2
import habitlock.composeapp.generated.resources.strictness_flexible_rule_3
import habitlock.composeapp.generated.resources.strictness_flexible_rule_4
import habitlock.composeapp.generated.resources.strictness_heading
import habitlock.composeapp.generated.resources.strictness_locked_description
import habitlock.composeapp.generated.resources.strictness_locked_label
import habitlock.composeapp.generated.resources.strictness_locked_rule_1
import habitlock.composeapp.generated.resources.strictness_locked_rule_2
import habitlock.composeapp.generated.resources.strictness_locked_rule_3
import habitlock.composeapp.generated.resources.strictness_locked_rule_4
import habitlock.composeapp.generated.resources.strictness_subtext
import org.jetbrains.compose.resources.stringResource

@Composable
fun StrictnessScreen(
    onNavigateToFirstHabit: () -> Unit,
    onNavigateToToday: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.onboardingViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToStrictness -> Unit  // not reachable from StrictnessScreen
                OnboardingEvent.NavigateToFirstHabit -> onNavigateToFirstHabit()
                OnboardingEvent.NavigateToToday -> onNavigateToToday()
                OnboardingEvent.EmptyHabitName -> Unit     // not reachable from StrictnessScreen
                OnboardingEvent.MissingTargetValue -> Unit // not reachable from StrictnessScreen
                OnboardingEvent.InvalidTargetValue -> Unit // not reachable from StrictnessScreen
            }
        }
    }

    StrictnessScreen(
        selectedPreset = state.selectedPreset,
        isLoading = state.isApplyingPreset,
        onPresetSelected = viewModel::selectPreset,
        onContinue = viewModel::continueFromStrictness,
        onSkip = viewModel::skipToToday
    )
}

@Composable
private fun StrictnessScreen(
    selectedPreset: StrictnessPreset,
    isLoading: Boolean,
    onPresetSelected: (StrictnessPreset) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(Res.string.strictness_heading),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(Res.string.strictness_subtext),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PresetCard(
                emoji = "🟢",
                label = stringResource(Res.string.strictness_flexible_label),
                description = stringResource(Res.string.strictness_flexible_description),
                rules = listOf(
                    stringResource(Res.string.strictness_flexible_rule_1),
                    stringResource(Res.string.strictness_flexible_rule_2),
                    stringResource(Res.string.strictness_flexible_rule_3),
                    stringResource(Res.string.strictness_flexible_rule_4)
                ),
                isSelected = selectedPreset == StrictnessPreset.FLEXIBLE,
                onClick = { onPresetSelected(StrictnessPreset.FLEXIBLE) }
            )
            
            PresetCard(
                emoji = "🟡",
                label = stringResource(Res.string.strictness_balanced_label),
                description = stringResource(Res.string.strictness_balanced_description),
                rules = listOf(
                    stringResource(Res.string.strictness_balanced_rule_1),
                    stringResource(Res.string.strictness_balanced_rule_2),
                    stringResource(Res.string.strictness_balanced_rule_3),
                    stringResource(Res.string.strictness_balanced_rule_4)
                ),
                isSelected = selectedPreset == StrictnessPreset.BALANCED,
                isRecommended = true,
                onClick = { onPresetSelected(StrictnessPreset.BALANCED) }
            )
            
            PresetCard(
                emoji = "🔴",
                label = stringResource(Res.string.strictness_locked_label),
                description = stringResource(Res.string.strictness_locked_description),
                rules = listOf(
                    stringResource(Res.string.strictness_locked_rule_1),
                    stringResource(Res.string.strictness_locked_rule_2),
                    stringResource(Res.string.strictness_locked_rule_3),
                    stringResource(Res.string.strictness_locked_rule_4)
                ),
                isSelected = selectedPreset == StrictnessPreset.LOCKED,
                onClick = { onPresetSelected(StrictnessPreset.LOCKED) }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.common_continue))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = onSkip) {
                Text(stringResource(Res.string.common_skip))
            }
        }
    }
}

@Composable
private fun PresetCard(
    emoji: String,
    label: String,
    description: String,
    rules: List<String>,
    isSelected: Boolean,
    isRecommended: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isRecommended) {
                    Text(
                        text = stringResource(Res.string.strictness_badge_recommended),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            rules.forEach { rule ->
                Text(
                    text = rule,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

