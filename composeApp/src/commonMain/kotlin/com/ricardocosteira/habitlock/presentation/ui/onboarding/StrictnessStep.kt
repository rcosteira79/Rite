package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset

private data class PresetInfo(
    val preset: StrictnessPreset,
    val label: String,
    val description: String,
    val dotColor: Color,
    val rules: List<String>,
    val isRecommended: Boolean = false
)

private val PRESETS = listOf(
    PresetInfo(
        preset = StrictnessPreset.FLEXIBLE,
        label = "Flexible",
        description = "Gentle support, maximum forgiveness.",
        dotColor = Color(0xFF4CAF50),
        rules = listOf(
            "Unlimited undo",
            "Unlimited snoozes",
            "Skips allowed without limits",
            "Missed habits tracked lightly"
        )
    ),
    PresetInfo(
        preset = StrictnessPreset.BALANCED,
        label = "Balanced",
        description = "Structure with room for real life.",
        dotColor = Color(0xFFFF9800),
        rules = listOf(
            "Undo allowed for today only",
            "Snoozes are limited",
            "Skips are limited",
            "Missed habits fail at end of day"
        ),
        isRecommended = true
    ),
    PresetInfo(
        preset = StrictnessPreset.LOCKED,
        label = "Locked",
        description = "No excuses. Full accountability.",
        dotColor = Color(0xFFF44336),
        rules = listOf(
            "No undo",
            "Snoozes are capped",
            "Skips are capped",
            "Missed habits always fail"
        )
    )
)

@Composable
fun StrictnessStep(
    selectedPreset: StrictnessPreset,
    onPresetSelected: (StrictnessPreset) -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "How strict\nshould it be?",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "You're always in control. Change this anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        PRESETS.forEach { info ->
            PresetCard(
                info = info,
                isSelected = info.preset == selectedPreset,
                onClick = { onPresetSelected(info.preset) },
                reduceMotion = reduceMotion
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PresetCard(
    info: PresetInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                stateDescription = if (isSelected) "Selected" else "Not selected"
            }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = info.dotColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = info.label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (info.isRecommended) {
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "Recommended",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = info.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp)
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = if (reduceMotion) EnterTransition.None else expandVertically(tween(250)) + fadeIn(tween(250)),
            exit = if (reduceMotion) ExitTransition.None else shrinkVertically(tween(200)) + fadeOut(tween(200))
        ) {
            Column(modifier = Modifier.padding(top = 10.dp, start = 16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(1.dp)
                        )
                )
                Spacer(modifier = Modifier.height(10.dp))
                info.rules.forEach { rule ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(bottom = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rule,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
