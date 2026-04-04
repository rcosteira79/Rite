package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_schedule_day_fri
import rite.composeapp.generated.resources.common_schedule_day_mon
import rite.composeapp.generated.resources.common_schedule_day_sat
import rite.composeapp.generated.resources.common_schedule_day_sun
import rite.composeapp.generated.resources.common_schedule_day_thu
import rite.composeapp.generated.resources.common_schedule_day_tue
import rite.composeapp.generated.resources.common_schedule_day_wed
import rite.composeapp.generated.resources.common_schedule_every_day
import rite.composeapp.generated.resources.common_schedule_weekdays
import rite.composeapp.generated.resources.common_schedule_weekend
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val DayChipSize = 40.dp

private enum class SchedulePreset(val days: Set<DayOfWeek>) {
    EVERY_DAY(DayOfWeek.entries.toSet()),
    WEEKDAYS(
        setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    ),
    WEEKEND(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
}

private fun SchedulePreset.labelRes(): StringResource = when (this) {
    SchedulePreset.EVERY_DAY -> Res.string.common_schedule_every_day
    SchedulePreset.WEEKDAYS -> Res.string.common_schedule_weekdays
    SchedulePreset.WEEKEND -> Res.string.common_schedule_weekend
}

private val DayOfWeek.labelRes: StringResource
    get() = when (this) {
        DayOfWeek.MONDAY -> Res.string.common_schedule_day_mon
        DayOfWeek.TUESDAY -> Res.string.common_schedule_day_tue
        DayOfWeek.WEDNESDAY -> Res.string.common_schedule_day_wed
        DayOfWeek.THURSDAY -> Res.string.common_schedule_day_thu
        DayOfWeek.FRIDAY -> Res.string.common_schedule_day_fri
        DayOfWeek.SATURDAY -> Res.string.common_schedule_day_sat
        DayOfWeek.SUNDAY -> Res.string.common_schedule_day_sun
    }

/**
 * Schedule picker with three preset pills (Every day / Weekdays / Weekend) and
 * circular day chips. Pressing a pill selects the corresponding days. Any manual
 * chip adjustment that doesn't match a preset leaves all pills un-highlighted.
 */
@Composable
fun SchedulePicker(
    selectedDays: Set<DayOfWeek>,
    onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier
) {
    val activePreset = SchedulePreset.entries.find { it.days == selectedDays }

    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SchedulePreset.entries.forEach { preset ->
                PresetPill(
                    text = stringResource(preset.labelRes()),
                    isSelected = preset == activePreset,
                    onClick = { onSelectedDaysChange(preset.days) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DayOfWeek.entries.forEach { day ->
                val isSelected = day in selectedDays
                DayChip(
                    label = stringResource(day.labelRes),
                    isSelected = isSelected,
                    onClick = {
                        val updated = if (isSelected) selectedDays - day else selectedDays + day
                        onSelectedDaysChange(updated)
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ).padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
private fun DayChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(DayChipSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
