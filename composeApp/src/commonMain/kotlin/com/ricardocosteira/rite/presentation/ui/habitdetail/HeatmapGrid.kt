package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.util.todayIn
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private val CELL_SIZE = 12.dp
private val CELL_GAP = 2.dp
private val CELL_CORNER = 2.dp

private val DAY_LABELS: List<Pair<DayOfWeek, String>> = listOf(
    DayOfWeek.MONDAY to "M",
    DayOfWeek.TUESDAY to "",
    DayOfWeek.WEDNESDAY to "W",
    DayOfWeek.THURSDAY to "",
    DayOfWeek.FRIDAY to "F",
    DayOfWeek.SATURDAY to "",
    DayOfWeek.SUNDAY to "S"
)

@Composable
fun HeatmapGrid(heatmapData: List<HeatmapDay>, modifier: Modifier = Modifier) {
    val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val startDate: LocalDate = today.minus(DatePeriod(days = 90))

    val dataByDate: Map<LocalDate, HeatmapDay> = heatmapData.associateBy { it.date }

    val weeks: List<List<LocalDate?>> = buildWeeks(startDate, today)

    Row(modifier = modifier) {
        // Day labels column
        Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
            DAY_LABELS.forEach { (_, label) ->
                Box(
                    modifier = Modifier.size(CELL_SIZE),
                    contentAlignment = Alignment.Center
                ) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Week columns
        Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
            weeks.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                    week.forEach { date ->
                        if (date == null) {
                            Box(modifier = Modifier.size(CELL_SIZE))
                        } else {
                            HeatmapCell(day = dataByDate[date])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCell(day: HeatmapDay?, modifier: Modifier = Modifier) {
    val color = when {
        day == null -> MaterialTheme.colorScheme.surfaceContainerLow
        day.status == HabitStatus.SKIPPED -> MaterialTheme.colorScheme.outlineVariant
        day.status == HabitStatus.SUSPENDED -> MaterialTheme.colorScheme.surfaceContainerLow
        day.completionPercentage >= 1.0f -> MaterialTheme.colorScheme.primary
        day.completionPercentage >= 0.5f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        day.completionPercentage > 0f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Box(
        modifier = modifier
            .size(CELL_SIZE)
            .background(color = color, shape = RoundedCornerShape(CELL_CORNER))
    )
}

private fun buildWeeks(startDate: LocalDate, endDate: LocalDate): List<List<LocalDate?>> {
    val weeks: MutableList<List<LocalDate?>> = mutableListOf()

    // Align to start of week (Monday)
    var weekStart: LocalDate = startDate
    while (weekStart.dayOfWeek != DayOfWeek.MONDAY) {
        weekStart = weekStart.minus(DatePeriod(days = 1))
    }

    while (weekStart <= endDate) {
        val week: MutableList<LocalDate?> = mutableListOf()
        for (dayOffset in 0..6) {
            val date: LocalDate = weekStart.plus(DatePeriod(days = dayOffset))
            week.add(if (date in startDate..endDate) date else null)
        }
        weeks.add(week)
        weekStart = weekStart.plus(DatePeriod(days = 7))
    }

    return weeks
}
