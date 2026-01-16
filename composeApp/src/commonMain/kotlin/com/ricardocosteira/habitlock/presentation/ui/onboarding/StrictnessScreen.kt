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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset

@Composable
fun StrictnessScreen(
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
            text = "How strict should HabitLock be?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "You're always in control. You can change this later.",
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
                label = "Flexible",
                description = "Gentle support with maximum forgiveness.",
                rules = listOf(
                    "Unlimited undo",
                    "Unlimited snoozes",
                    "Skips allowed without limits",
                    "Missed habits are tracked, but lightly enforced"
                ),
                isSelected = selectedPreset == StrictnessPreset.FLEXIBLE,
                onClick = { onPresetSelected(StrictnessPreset.FLEXIBLE) }
            )
            
            PresetCard(
                emoji = "🟡",
                label = "Balanced",
                description = "Structure with room for real life.",
                rules = listOf(
                    "Undo allowed for today only",
                    "Snoozes are limited",
                    "Skips are limited",
                    "Missed habits fail at the end of the day"
                ),
                isSelected = selectedPreset == StrictnessPreset.BALANCED,
                isRecommended = true,
                onClick = { onPresetSelected(StrictnessPreset.BALANCED) }
            )
            
            PresetCard(
                emoji = "🔴",
                label = "Locked",
                description = "No excuses. Full accountability.",
                rules = listOf(
                    "No undo",
                    "Snoozes are capped",
                    "Skips are capped",
                    "Missed habits always fail"
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
                Text("Continue")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = onSkip) {
                Text("Skip")
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
                        text = "(Recommended)",
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
                    text = "• $rule",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

