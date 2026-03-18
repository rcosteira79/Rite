package com.ricardocosteira.habitlock.presentation.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.presentation.models.CalendarDayUiModel
import com.ricardocosteira.habitlock.presentation.models.DayClassification
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.calendar_cd_next_month
import habitlock.composeapp.generated.resources.calendar_cd_previous_month
import habitlock.composeapp.generated.resources.calendar_day_fri
import habitlock.composeapp.generated.resources.calendar_day_mon
import habitlock.composeapp.generated.resources.calendar_day_sat
import habitlock.composeapp.generated.resources.calendar_day_sun
import habitlock.composeapp.generated.resources.calendar_day_thu
import habitlock.composeapp.generated.resources.calendar_day_tue
import habitlock.composeapp.generated.resources.calendar_day_wed
import habitlock.composeapp.generated.resources.calendar_legend_best_effort
import habitlock.composeapp.generated.resources.calendar_legend_future
import habitlock.composeapp.generated.resources.calendar_legend_partial
import habitlock.composeapp.generated.resources.calendar_legend_perfect
import habitlock.composeapp.generated.resources.calendar_legend_rough_day
import habitlock.composeapp.generated.resources.calendar_stats_days_tracked
import habitlock.composeapp.generated.resources.calendar_stats_perfect_days
import habitlock.composeapp.generated.resources.calendar_title
import habitlock.composeapp.generated.resources.common_cd_back
import habitlock.composeapp.generated.resources.common_failed
import org.jetbrains.compose.resources.stringResource

@Composable
fun CalendarScreen(onBackClick: () -> Unit) {
    val viewModel = LocalAppComponent.current.calendarViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    CalendarScreen(
        state = state,
        onBackClick = onBackClick,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onDayClick = { viewModel.selectDay(it.date) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarScreen(
    state: CalendarState,
    onBackClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (CalendarDayUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.calendar_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_cd_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Stats summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.perfectDaysCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(Res.string.calendar_stats_perfect_days),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.totalDaysTracked}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(Res.string.calendar_stats_days_tracked),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(Res.string.calendar_cd_previous_month))
                }

                Text(
                    text = "${state.currentMonth.name.lowercase().replaceFirstChar { it.uppercase() }} ${state.currentYear}",
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(Res.string.calendar_cd_next_month))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day of week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(stringResource(Res.string.calendar_day_mon), stringResource(Res.string.calendar_day_tue), stringResource(Res.string.calendar_day_wed), stringResource(Res.string.calendar_day_thu), stringResource(Res.string.calendar_day_fri), stringResource(Res.string.calendar_day_sat), stringResource(Res.string.calendar_day_sun)).forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Calendar grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Add empty cells for days before month starts
                    val firstDayOfWeek = state.days.firstOrNull()?.date?.dayOfWeek?.ordinal ?: 0
                    items(firstDayOfWeek) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    }

                    items(state.days) { day ->
                        CalendarDayCell(
                            day = day,
                            onClick = { onDayClick(day) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = MaterialTheme.colorScheme.primary, label = stringResource(Res.string.calendar_legend_perfect))
                    LegendItem(color = MaterialTheme.colorScheme.tertiary, label = stringResource(Res.string.calendar_legend_best_effort))
                    LegendItem(color = MaterialTheme.colorScheme.secondary, label = stringResource(Res.string.calendar_legend_partial))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), label = stringResource(Res.string.calendar_legend_rough_day))
                    LegendItem(color = MaterialTheme.colorScheme.error, label = stringResource(Res.string.common_failed))
                    LegendItem(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), label = stringResource(Res.string.calendar_legend_future))
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayUiModel,
    onClick: () -> Unit
) {
    val backgroundColor = when (day.classification) {
        DayClassification.PERFECT -> MaterialTheme.colorScheme.primary
        DayClassification.BEST_EFFORT -> MaterialTheme.colorScheme.tertiary
        DayClassification.PARTIAL -> MaterialTheme.colorScheme.secondary
        DayClassification.ROUGH_DAY -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        DayClassification.FAILED -> MaterialTheme.colorScheme.error
        DayClassification.FUTURE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        DayClassification.NONE -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when (day.classification) {
        DayClassification.PERFECT -> MaterialTheme.colorScheme.onPrimary
        DayClassification.BEST_EFFORT -> MaterialTheme.colorScheme.onTertiary
        DayClassification.PARTIAL -> MaterialTheme.colorScheme.onSecondary
        DayClassification.ROUGH_DAY -> MaterialTheme.colorScheme.onError
        DayClassification.FAILED -> MaterialTheme.colorScheme.onError
        DayClassification.FUTURE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        DayClassification.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val isClickable = day.classification !in listOf(DayClassification.NONE, DayClassification.FUTURE)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = isClickable, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

