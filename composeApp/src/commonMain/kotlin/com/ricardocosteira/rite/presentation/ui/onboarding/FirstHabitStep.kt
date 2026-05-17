package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.first_habit_heading_accent
import rite.composeapp.generated.resources.first_habit_heading_first
import rite.composeapp.generated.resources.first_habit_label_name
import rite.composeapp.generated.resources.first_habit_label_schedule
import rite.composeapp.generated.resources.first_habit_label_target_value
import rite.composeapp.generated.resources.first_habit_label_type
import rite.composeapp.generated.resources.first_habit_label_unit
import rite.composeapp.generated.resources.first_habit_placeholder_unit
import rite.composeapp.generated.resources.first_habit_schedule_daily
import rite.composeapp.generated.resources.first_habit_schedule_weekly
import rite.composeapp.generated.resources.first_habit_strap_label
import rite.composeapp.generated.resources.first_habit_subtext
import rite.composeapp.generated.resources.first_habit_type_binary
import rite.composeapp.generated.resources.first_habit_type_binary_description
import rite.composeapp.generated.resources.first_habit_type_quantitative
import rite.composeapp.generated.resources.first_habit_type_quantitative_description

@Composable
fun FirstHabitStep(
    habitName: String,
    habitType: HabitType,
    targetValue: String,
    unit: String,
    scheduleKind: OnboardingScheduleKind,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onScheduleKindChange: (OnboardingScheduleKind) -> Unit,
    modifier: Modifier = Modifier
) {
    val isQuantitative = habitType == HabitType.QUANTITATIVE

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        OnboardingStepStrap(
            step = 3,
            totalSteps = 4,
            stepName = stringResource(Res.string.first_habit_strap_label)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = headingAnnotated(),
            style = RiteAppTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Normal),
            color = RiteAppTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(Res.string.first_habit_subtext),
            style = RiteAppTheme.typography.bodyMedium,
            color = RiteAppTheme.colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        FieldGroup(label = stringResource(Res.string.first_habit_label_name)) {
            TextField(
                value = habitName,
                onValueChange = onHabitNameChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = RiteAppTheme.colors.surface,
                    unfocusedContainerColor = RiteAppTheme.colors.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        FieldGroup(label = stringResource(Res.string.first_habit_label_type)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeCard(
                    label = stringResource(Res.string.first_habit_type_binary),
                    description = stringResource(Res.string.first_habit_type_binary_description),
                    isSelected = !isQuantitative,
                    onClick = { onHabitTypeChange(HabitType.BINARY) },
                    modifier = Modifier.weight(1f)
                )
                TypeCard(
                    label = stringResource(Res.string.first_habit_type_quantitative),
                    description = stringResource(
                        Res.string.first_habit_type_quantitative_description
                    ),
                    isSelected = isQuantitative,
                    onClick = { onHabitTypeChange(HabitType.QUANTITATIVE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        AnimatedVisibility(
            visible = isQuantitative,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldGroup(
                        label = stringResource(Res.string.first_habit_label_target_value),
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = targetValue,
                            onValueChange = onTargetValueChange,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = RiteAppTheme.colors.surface,
                                unfocusedContainerColor = RiteAppTheme.colors.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    FieldGroup(
                        label = stringResource(Res.string.first_habit_label_unit),
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = unit,
                            onValueChange = onUnitChange,
                            placeholder = {
                                Text(stringResource(Res.string.first_habit_placeholder_unit))
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = RiteAppTheme.colors.surface,
                                unfocusedContainerColor = RiteAppTheme.colors.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        FieldGroup(label = stringResource(Res.string.first_habit_label_schedule)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScheduleChip(
                    label = stringResource(Res.string.first_habit_schedule_daily),
                    isSelected = scheduleKind == OnboardingScheduleKind.DAILY,
                    onClick = { onScheduleKindChange(OnboardingScheduleKind.DAILY) }
                )
                ScheduleChip(
                    label = stringResource(Res.string.first_habit_schedule_weekly),
                    isSelected = scheduleKind == OnboardingScheduleKind.WEEKLY,
                    onClick = { onScheduleKindChange(OnboardingScheduleKind.WEEKLY) }
                )
            }
        }
    }
}

@Composable
private fun headingAnnotated(): AnnotatedString = buildAnnotatedString {
    append(stringResource(Res.string.first_habit_heading_first))
    append(" ")
    withStyle(
        SpanStyle(fontStyle = FontStyle.Italic, color = RiteAppTheme.colors.onSurfaceVariant)
    ) {
        append(stringResource(Res.string.first_habit_heading_accent))
    }
}

@Composable
private fun FieldGroup(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = RiteAppTheme.typography.labelSmall,
            color = RiteAppTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}

@Composable
private fun TypeCard(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .background(if (isSelected) RiteAppTheme.colors.surface else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = RiteAppTheme.typography.titleSmall,
            color = RiteAppTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            style = RiteAppTheme.typography.bodySmall,
            color = RiteAppTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
private fun ScheduleChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(
                width = 1.dp,
                color = if (isSelected) RiteAppTheme.colors.onSurface else RiteAppTheme.colors.outline,
                shape = RoundedCornerShape(50)
            )
            .background(if (isSelected) RiteAppTheme.colors.onSurface else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = RiteAppTheme.typography.labelLarge,
            color = if (isSelected) RiteAppTheme.colors.surface else RiteAppTheme.colors.onSurface
        )
    }
}
