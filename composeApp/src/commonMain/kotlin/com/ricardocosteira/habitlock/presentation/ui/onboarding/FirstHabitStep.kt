package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.HabitType
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_placeholder_habit_name
import habitlock.composeapp.generated.resources.common_quantitative
import habitlock.composeapp.generated.resources.first_habit_heading
import habitlock.composeapp.generated.resources.first_habit_label_name
import habitlock.composeapp.generated.resources.first_habit_label_target_value
import habitlock.composeapp.generated.resources.first_habit_label_unit
import habitlock.composeapp.generated.resources.first_habit_placeholder_unit
import habitlock.composeapp.generated.resources.first_habit_schedule_custom
import habitlock.composeapp.generated.resources.first_habit_schedule_day_fri
import habitlock.composeapp.generated.resources.first_habit_schedule_day_mon
import habitlock.composeapp.generated.resources.first_habit_schedule_day_sat
import habitlock.composeapp.generated.resources.first_habit_schedule_day_sun
import habitlock.composeapp.generated.resources.first_habit_schedule_day_thu
import habitlock.composeapp.generated.resources.first_habit_schedule_day_tue
import habitlock.composeapp.generated.resources.first_habit_schedule_day_wed
import habitlock.composeapp.generated.resources.first_habit_schedule_every_day
import habitlock.composeapp.generated.resources.first_habit_schedule_label
import habitlock.composeapp.generated.resources.first_habit_schedule_weekdays
import habitlock.composeapp.generated.resources.first_habit_schedule_weekends
import habitlock.composeapp.generated.resources.first_habit_subtext
import habitlock.composeapp.generated.resources.first_habit_type_binary
import habitlock.composeapp.generated.resources.first_habit_type_binary_description
import habitlock.composeapp.generated.resources.first_habit_type_quantitative_description
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirstHabitStep(
    habitName: String,
    habitType: HabitType,
    targetValue: String,
    unit: String,
    scheduleOption: ScheduleOption,
    customDays: Set<DayOfWeek>,
    onHabitNameChange: (String) -> Unit,
    onHabitTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onScheduleOptionChange: (ScheduleOption) -> Unit,
    onCustomDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.first_habit_heading),
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
            text = stringResource(Res.string.first_habit_subtext),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = habitName,
            onValueChange = onHabitNameChange,
            label = { Text(stringResource(Res.string.first_habit_label_name)) },
            placeholder = { Text(stringResource(Res.string.common_placeholder_habit_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Type cards
        HabitTypeCard(
            label = stringResource(Res.string.first_habit_type_binary),
            description = stringResource(Res.string.first_habit_type_binary_description),
            isSelected = habitType == HabitType.BINARY,
            onClick = {
                onHabitTypeChange(HabitType.BINARY)
                onTargetValueChange("")
                onUnitChange("")
            },
            expandedContent = null
        )

        Spacer(modifier = Modifier.height(8.dp))

        HabitTypeCard(
            label = stringResource(Res.string.common_quantitative),
            description = stringResource(Res.string.first_habit_type_quantitative_description),
            isSelected = habitType == HabitType.QUANTITATIVE,
            onClick = { onHabitTypeChange(HabitType.QUANTITATIVE) },
            expandedContent = {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = onTargetValueChange,
                        label = { Text(stringResource(Res.string.first_habit_label_target_value)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = onUnitChange,
                        label = { Text(stringResource(Res.string.first_habit_label_unit)) },
                        placeholder = { Text(stringResource(Res.string.first_habit_placeholder_unit)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Schedule section
        Text(
            text = stringResource(Res.string.first_habit_schedule_label).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = scheduleOption == ScheduleOption.EVERY_DAY,
                onClick = { onScheduleOptionChange(ScheduleOption.EVERY_DAY) },
                label = { Text(stringResource(Res.string.first_habit_schedule_every_day)) }
            )
            FilterChip(
                selected = scheduleOption == ScheduleOption.WEEKDAYS,
                onClick = { onScheduleOptionChange(ScheduleOption.WEEKDAYS) },
                label = { Text(stringResource(Res.string.first_habit_schedule_weekdays)) }
            )
            FilterChip(
                selected = scheduleOption == ScheduleOption.WEEKENDS,
                onClick = { onScheduleOptionChange(ScheduleOption.WEEKENDS) },
                label = { Text(stringResource(Res.string.first_habit_schedule_weekends)) }
            )
            FilterChip(
                selected = scheduleOption == ScheduleOption.CUSTOM,
                onClick = { onScheduleOptionChange(ScheduleOption.CUSTOM) },
                label = { Text(stringResource(Res.string.first_habit_schedule_custom)) }
            )
        }

        AnimatedVisibility(
            visible = scheduleOption == ScheduleOption.CUSTOM,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DayOfWeek.entries.forEach { day ->
                    val label = stringResource(dayLabel(day))
                    FilterChip(
                        selected = day in customDays,
                        onClick = {
                            val updatedDays = if (day in customDays) {
                                customDays - day
                            } else {
                                customDays + day
                            }
                            onCustomDaysChange(updatedDays)
                        },
                        label = { Text(label) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HabitTypeCard(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    expandedContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (expandedContent != null) {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    expandedContent()
                }
            }
        }
    }
}

private fun dayLabel(day: DayOfWeek): StringResource = when (day) {
    DayOfWeek.MONDAY -> Res.string.first_habit_schedule_day_mon
    DayOfWeek.TUESDAY -> Res.string.first_habit_schedule_day_tue
    DayOfWeek.WEDNESDAY -> Res.string.first_habit_schedule_day_wed
    DayOfWeek.THURSDAY -> Res.string.first_habit_schedule_day_thu
    DayOfWeek.FRIDAY -> Res.string.first_habit_schedule_day_fri
    DayOfWeek.SATURDAY -> Res.string.first_habit_schedule_day_sat
    DayOfWeek.SUNDAY -> Res.string.first_habit_schedule_day_sun
}
