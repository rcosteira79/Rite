package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.strictness_badge_recommended
import rite.composeapp.generated.resources.strictness_heading_accent
import rite.composeapp.generated.resources.strictness_heading_first
import rite.composeapp.generated.resources.strictness_heading_tail
import rite.composeapp.generated.resources.strictness_preset_cd_not_selected
import rite.composeapp.generated.resources.strictness_preset_cd_selected
import rite.composeapp.generated.resources.strictness_subtext

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            text = headingAnnotated(),
            style = RiteAppTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Normal),
            color = RiteAppTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(Res.string.strictness_subtext),
            style = RiteAppTheme.typography.bodyMedium,
            color = RiteAppTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(22.dp))

        OnboardingStrictnessPreset.entries.forEach { preset ->
            PresetAccordionCard(
                preset = preset,
                isOpen = preset == selectedPreset,
                onClick = { onPresetSelected(preset) },
                reduceMotion = reduceMotion
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun headingAnnotated(): AnnotatedString = buildAnnotatedString {
    append(stringResource(Res.string.strictness_heading_first))
    append(" ")
    withStyle(
        SpanStyle(fontStyle = FontStyle.Italic, color = RiteAppTheme.colors.onSurfaceVariant)
    ) {
        append(stringResource(Res.string.strictness_heading_accent))
    }
    append(" ")
    append(stringResource(Res.string.strictness_heading_tail))
}

@Composable
private fun PresetAccordionCard(
    preset: OnboardingStrictnessPreset,
    isOpen: Boolean,
    onClick: () -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val selectedDesc = stringResource(Res.string.strictness_preset_cd_selected)
    val notSelectedDesc = stringResource(Res.string.strictness_preset_cd_not_selected)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = if (isOpen) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .background(if (isOpen) RiteAppTheme.colors.surface else RiteAppTheme.colors.background)
            .clickable { onClick() }
            .semantics {
                role = Role.RadioButton
                selected = isOpen
                stateDescription = if (isOpen) selectedDesc else notSelectedDesc
            }
            .padding(horizontal = 18.dp, vertical = if (isOpen) 18.dp else 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioDot(isOpen = isOpen)
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = preset.label,
                style = RiteAppTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    letterSpacing = (-0.1).sp
                ),
                color = RiteAppTheme.colors.onSurface
            )
            if (preset.isRecommended) {
                Spacer(modifier = Modifier.size(8.dp))
                RecommendedBadge()
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isOpen) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = RiteAppTheme.colors.outline,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(
            visible = isOpen,
            enter =
                expandVertically(tween(if (reduceMotion) 0 else 180)) +
                    fadeIn(tween(if (reduceMotion) 0 else 180)),
            exit =
                shrinkVertically(tween(if (reduceMotion) 0 else 180)) +
                    fadeOut(tween(if (reduceMotion) 0 else 180))
        ) {
            Column(modifier = Modifier.padding(start = 28.dp, top = 12.dp)) {
                Text(
                    text = preset.description,
                    style = RiteAppTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = RiteAppTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                preset.rules.forEach { rule ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 9.dp)
                                .size(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(RiteAppTheme.colors.onSurface)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = rule,
                            style = RiteAppTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = RiteAppTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RadioDot(isOpen: Boolean) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(50))
            .border(
                width = 1.5.dp,
                color = if (isOpen) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.outline,
                shape = RoundedCornerShape(50)
            )
            .background(
                if (isOpen) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.background
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isOpen) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(RiteAppTheme.colors.background)
            )
        }
    }
}

@Composable
private fun RecommendedBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(RiteAppTheme.colors.onSurface)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = stringResource(Res.string.strictness_badge_recommended),
            style = RiteAppTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = RiteAppTheme.colors.background
        )
    }
}
