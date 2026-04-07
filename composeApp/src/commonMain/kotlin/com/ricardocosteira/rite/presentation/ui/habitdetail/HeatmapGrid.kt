package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import com.ricardocosteira.rite.util.todayIn
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_detail_heatmap_best_effort
import rite.composeapp.generated.resources.habit_detail_heatmap_failed
import rite.composeapp.generated.resources.habit_detail_heatmap_partial
import rite.composeapp.generated.resources.habit_detail_heatmap_perfect
import rite.composeapp.generated.resources.habit_detail_heatmap_skipped

private val CELL_GAP = 2.dp
private val CELL_CORNER = 2.dp
private val DAY_LABEL_WIDTH = 16.dp
private val DAY_LABEL_SPACING = 4.dp
private val LEGEND_CELL_SIZE = 10.dp

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
    val weekCount: Int = weeks.size

    BoxWithConstraints(modifier = modifier) {
        val availableWidth: Dp = maxWidth
        val labelSpace: Dp = DAY_LABEL_WIDTH + DAY_LABEL_SPACING
        val totalGaps: Dp = CELL_GAP * (weekCount - 1)
        val cellSize: Dp = (availableWidth - labelSpace - totalGaps) / weekCount

        Column {
            Row {
                // Day labels column
                Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                    DAY_LABELS.forEach { (_, label) ->
                        Box(
                            modifier = Modifier.size(width = DAY_LABEL_WIDTH, height = cellSize),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (label.isNotEmpty()) {
                                Text(
                                    text = label,
                                    style = RiteAppTheme.typography.labelSmall,
                                    color = RiteAppTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(DAY_LABEL_SPACING))

                // Week columns
                Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                    weeks.forEach { week ->
                        Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                            week.forEach { date ->
                                if (date == null) {
                                    Box(modifier = Modifier.size(cellSize))
                                } else {
                                    HeatmapCell(
                                        day = dataByDate[date],
                                        size = cellSize
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            HeatmapLegend(cellSize = LEGEND_CELL_SIZE)
        }
    }
}

@Composable
private fun HeatmapCell(day: HeatmapDay?, size: Dp, modifier: Modifier = Modifier) {
    val colorScheme = RiteAppTheme.colorScheme
    val color = when {
        day == null -> colorScheme.noData
        day.status == HabitStatus.FAILED -> colorScheme.failed
        day.status == HabitStatus.SKIPPED -> colorScheme.skipped
        day.status == HabitStatus.SUSPENDED -> colorScheme.noData
        day.completionPercentage >= 1.0f -> colorScheme.perfect
        day.completionPercentage >= 0.5f -> colorScheme.bestEffort
        day.completionPercentage > 0f -> colorScheme.partial
        else -> colorScheme.noData
    }

    Box(
        modifier = modifier
            .size(size)
            .background(color = color, shape = RoundedCornerShape(CELL_CORNER))
    )
}

@Composable
private fun HeatmapLegend(cellSize: Dp, modifier: Modifier = Modifier) {
    val colorScheme = RiteAppTheme.colorScheme
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(
            color = colorScheme.perfect,
            label = stringResource(Res.string.habit_detail_heatmap_perfect),
            size = cellSize
        )
        LegendItem(
            color = colorScheme.bestEffort,
            label = stringResource(Res.string.habit_detail_heatmap_best_effort),
            size = cellSize
        )
        LegendItem(
            color = colorScheme.partial,
            label = stringResource(Res.string.habit_detail_heatmap_partial),
            size = cellSize
        )
        LegendItem(
            color = colorScheme.failed,
            label = stringResource(Res.string.habit_detail_heatmap_failed),
            size = cellSize
        )
        LegendItem(
            color = colorScheme.skipped,
            label = stringResource(Res.string.habit_detail_heatmap_skipped),
            size = cellSize
        )
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(color = color, shape = RoundedCornerShape(CELL_CORNER))
        )
        Text(
            text = label,
            style = RiteAppTheme.typography.labelSmall,
            color = RiteAppTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun buildWeeks(startDate: LocalDate, endDate: LocalDate): List<List<LocalDate?>> {
    val weeks: MutableList<List<LocalDate?>> = mutableListOf()

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
