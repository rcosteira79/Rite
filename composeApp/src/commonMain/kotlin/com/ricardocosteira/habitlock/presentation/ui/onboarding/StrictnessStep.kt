package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.strictness_badge_recommended
import habitlock.composeapp.generated.resources.strictness_heading
import habitlock.composeapp.generated.resources.strictness_preset_cd_not_selected
import habitlock.composeapp.generated.resources.strictness_preset_cd_selected
import habitlock.composeapp.generated.resources.strictness_subtext
import org.jetbrains.compose.resources.stringResource

private fun OnboardingStrictnessPreset.icon(): ImageVector = when (this) {
    OnboardingStrictnessPreset.FLEXIBLE -> Icons.Outlined.EditNote
    OnboardingStrictnessPreset.BALANCED -> Icons.Filled.Balance
    OnboardingStrictnessPreset.LOCKED -> Icons.Filled.Lock
}

@Composable
fun StrictnessStep(
    selectedPreset: OnboardingStrictnessPreset,
    onPresetSelected: (OnboardingStrictnessPreset) -> Unit,
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
            text = stringResource(Res.string.strictness_heading),
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
            text = stringResource(Res.string.strictness_subtext),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        OnboardingStrictnessPreset.entries.forEach { preset ->
            PresetCard(
                preset = preset,
                isSelected = preset == selectedPreset,
                onClick = { onPresetSelected(preset) },
                reduceMotion = reduceMotion
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
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = if (reduceMotion) snap() else tween(200),
        label = "presetCardBackground"
    )

    val selectedStateDescription = stringResource(Res.string.strictness_preset_cd_selected)
    val notSelectedStateDescription = stringResource(Res.string.strictness_preset_cd_not_selected)

    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 16.dp,
        animationSpec = if (reduceMotion) snap() else tween(200),
        label = "presetCardCornerRadius"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(cornerRadius)
                    )
                } else {
                    Modifier
                }
            ).clip(RoundedCornerShape(cornerRadius))
            .border(
                width = 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(cornerRadius)
            ).background(backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f))
                        )
                    )
                } else {
                    Modifier
                }
            ).animateContentSize(animationSpec = tween(200))
            .clickable { onClick() }
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                stateDescription =
                    if (isSelected) selectedStateDescription else notSelectedStateDescription
            }.padding(16.dp)
    ) {
        if (isSelected) {
            // onPrimaryContainer: Forest Discipline #FFFFFF / Stoic Night #E5E2DF
            val cardContent = MaterialTheme.colorScheme.onPrimaryContainer
            // === SELECTED STATE ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = preset.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = cardContent
                )
                if (preset.isRecommended) {
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = cardContent.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(percent = 50)
                            ).padding(horizontal = 12.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.strictness_badge_recommended),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.12.sp
                            ),
                            color = cardContent.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = preset.label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = cardContent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodySmall,
                color = cardContent.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = cardContent.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))
            preset.rules.forEachIndexed { index, rule ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = cardContent.copy(alpha = 0.07f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = rule.key,
                        style = MaterialTheme.typography.labelSmall,
                        color = cardContent.copy(alpha = 0.6f)
                    )
                    Text(
                        text = rule.value,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = cardContent
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
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
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
}
