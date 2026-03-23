package com.ricardocosteira.habitlock.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_daily
import habitlock.composeapp.generated.resources.common_schedule
import habitlock.composeapp.generated.resources.common_schedule_day_fri
import habitlock.composeapp.generated.resources.common_schedule_day_mon
import habitlock.composeapp.generated.resources.common_schedule_day_sat
import habitlock.composeapp.generated.resources.common_schedule_day_sun
import habitlock.composeapp.generated.resources.common_schedule_day_thu
import habitlock.composeapp.generated.resources.common_schedule_day_tue
import habitlock.composeapp.generated.resources.common_schedule_day_wed
import habitlock.composeapp.generated.resources.common_weekly
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val DayChipSize = 40.dp

private val Weekdays = setOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
)

/**
 * Schedule picker with a Daily/Weekly tab toggle and circular day chips.
 *
 * - Daily (selectedDays == all 7): chips show all filled, non-interactive.
 * - Weekly (any other selection): chips are interactive.
 *
 * Switching Daily → Weekly defaults to weekdays (Mon–Fri).
 */
@Composable
fun SchedulePicker(
    selectedDays: Set<DayOfWeek>,
    onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDaily = selectedDays.size == DayOfWeek.entries.size

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.common_schedule),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ScheduleTab(
                    text = stringResource(Res.string.common_daily),
                    isSelected = isDaily,
                    onClick = { onSelectedDaysChange(DayOfWeek.entries.toSet()) },
                )
                ScheduleTab(
                    text = stringResource(Res.string.common_weekly),
                    isSelected = !isDaily,
                    onClick = { if (isDaily) onSelectedDaysChange(Weekdays) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DayOfWeek.entries.forEach { day ->
                val isSelected = day in selectedDays
                DayChip(
                    label = stringResource(dayInitial(day)),
                    isSelected = isSelected,
                    onClick = {
                        if (!isDaily) {
                            val updated = if (isSelected) selectedDays - day else selectedDays + day
                            onSelectedDaysChange(updated)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ScheduleTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val contentColor = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    color = if (isSelected) primaryColor else Color.Transparent,
                    shape = RoundedCornerShape(1.dp),
                )
        )
    }
}

@Composable
private fun DayChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
    }
}

private fun dayInitial(day: DayOfWeek): StringResource = when (day) {
    DayOfWeek.MONDAY -> Res.string.common_schedule_day_mon
    DayOfWeek.TUESDAY -> Res.string.common_schedule_day_tue
    DayOfWeek.WEDNESDAY -> Res.string.common_schedule_day_wed
    DayOfWeek.THURSDAY -> Res.string.common_schedule_day_thu
    DayOfWeek.FRIDAY -> Res.string.common_schedule_day_fri
    DayOfWeek.SATURDAY -> Res.string.common_schedule_day_sat
    DayOfWeek.SUNDAY -> Res.string.common_schedule_day_sun
}
