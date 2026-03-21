package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun OnboardingStrictnessPreset.icon(): ImageVector = when (this) {
    OnboardingStrictnessPreset.FLEXIBLE -> Icons.Outlined.EditNote
    OnboardingStrictnessPreset.BALANCED -> Icons.Filled.Balance
    OnboardingStrictnessPreset.LOCKED   -> Icons.Filled.Lock
}

@Composable
fun StrictnessStep(
    selectedPreset: OnboardingStrictnessPreset,
    onPresetSelected: (OnboardingStrictnessPreset) -> Unit,
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
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
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

        OnboardingStrictnessPreset.entries.forEach { preset ->
            PresetCard(
                preset = preset,
                isSelected = preset == selectedPreset,
                onClick = { onPresetSelected(preset) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PresetCard(
    preset: OnboardingStrictnessPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(200),
        label = "presetCardBackground"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 16.dp,
        animationSpec = tween(200),
        label = "presetCardCornerRadius"
    )

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(cornerRadius)
                    ) else Modifier
                )
                .clip(RoundedCornerShape(cornerRadius))
                .then(
                    if (!isSelected) Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(cornerRadius)
                    ) else Modifier
                )
                .background(backgroundColor)
                .clickable { onClick() }
                .semantics {
                    role = Role.RadioButton
                    selected = isSelected
                    stateDescription = if (isSelected) "Selected" else "Not selected"
                }
                .padding(16.dp)
                .animateContentSize(animationSpec = tween(200))
        ) {
            if (isSelected) {
                // === SELECTED STATE ===
                Icon(
                    imageVector = preset.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = preset.label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                preset.rules.forEachIndexed { index, rule ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.07f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = rule.key,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = rule.value,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                // === COLLAPSED (UNSELECTED) STATE ===
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val iconTint = if (preset == OnboardingStrictnessPreset.LOCKED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Icon(
                        imageVector = preset.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = iconTint
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = preset.collapsedSummary,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }

        // "RECOMMENDED" pill badge — positioned above the selected card
        if (isSelected && preset.isRecommended) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "RECOMMENDED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.12.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
