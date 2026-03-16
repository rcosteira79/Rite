package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel

private val SECTION_HEADER_LETTER_SPACING = 0.8.sp
private val ACCENT_BAR_WIDTH = 3.dp
private val ACCENT_BAR_CONTENT_START_PADDING = ACCENT_BAR_WIDTH + 16.dp // bar width + standard padding
private const val PILL_CORNER_PERCENT = 50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    state: TodayState,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHabitClick: (String) -> Unit,
    onCompleteClick: (String) -> Unit,
    onSkipClick: (String) -> Unit,
    onUndoClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onArchiveClick: (String) -> Unit,
    onAddHabitClick: () -> Unit,
    onDismissTimezoneWarning: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (!state.isLoading && (state.dailyTotal > 0 || state.weeklyTotal > 0)) {
                            val subtitleText: String = if (state.pendingCount > 0) {
                                "${state.pendingCount} ${if (state.pendingCount == 1) "habit" else "habits"} to go"
                            } else {
                                "All done for today 🎉"
                            }
                            val subtitleColor: Color = if (state.pendingCount > 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            }
                            Text(
                                text = subtitleText,
                                style = MaterialTheme.typography.labelMedium,
                                color = subtitleColor
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onCalendarClick) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHabitClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Timezone warning banner
            if (state.showTimezoneWarning) {
                TimezoneWarningBanner(
                    previousTimezone = state.previousTimezone,
                    onDismiss = onDismissTimezoneWarning
                )
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.habits.isEmpty() -> {
                    EmptyHabitsMessage(onAddHabitClick = onAddHabitClick)
                }
                else -> {
                    // Group habits by cadence and status
                    val dailyHabits = state.habits.filter { it.isDaily && !it.isSuspended }
                    val weeklyHabits = state.habits.filter { it.isWeekly && !it.isSuspended }
                    val suspendedHabits = state.habits.filter { it.isSuspended }

                    if (state.dailyTotal > 0 || state.weeklyTotal > 0) {
                        ProgressRingRow(
                            dailyCompleted = state.dailyCompleted,
                            dailyTotal = state.dailyTotal,
                            weeklyCompleted = state.weeklyCompleted,
                            weeklyTotal = state.weeklyTotal
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Daily Habits Section
                        if (dailyHabits.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "DAILY HABITS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = SECTION_HEADER_LETTER_SPACING,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${state.dailyCompleted} / ${state.dailyTotal}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            items(
                                items = dailyHabits,
                                key = { it.instanceId }
                            ) { habit ->
                                HabitCard(
                                    habit = habit,
                                    onClick = { onHabitClick(habit.instanceId) },
                                    onCompleteClick = { onCompleteClick(habit.instanceId) },
                                    onSkipClick = { onSkipClick(habit.instanceId) },
                                    onUndoClick = { onUndoClick(habit.instanceId) },
                                    onEditClick = { onEditClick(habit.habitId) },
                                    onArchiveClick = { onArchiveClick(habit.habitId) }
                                )
                            }
                        }

                        // Weekly Habits Section
                        if (weeklyHabits.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "WEEKLY HABITS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = SECTION_HEADER_LETTER_SPACING,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "${state.weeklyCompleted} / ${state.weeklyTotal}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            items(
                                items = weeklyHabits,
                                key = { it.instanceId }
                            ) { habit ->
                                HabitCard(
                                    habit = habit,
                                    onClick = { onHabitClick(habit.instanceId) },
                                    onCompleteClick = { onCompleteClick(habit.instanceId) },
                                    onSkipClick = { onSkipClick(habit.instanceId) },
                                    onUndoClick = { onUndoClick(habit.instanceId) },
                                    onEditClick = { onEditClick(habit.habitId) },
                                    onArchiveClick = { onArchiveClick(habit.habitId) }
                                )
                            }
                        }

                        // Suspended Habits Section
                        if (suspendedHabits.isNotEmpty()) {
                            item {
                                Text(
                                    text = "SUSPENDED HABITS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = SECTION_HEADER_LETTER_SPACING,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(
                                items = suspendedHabits,
                                key = { it.instanceId }
                            ) { habit ->
                                HabitCard(
                                    habit = habit,
                                    onClick = { onHabitClick(habit.instanceId) },
                                    onCompleteClick = { onCompleteClick(habit.instanceId) },
                                    onSkipClick = { onSkipClick(habit.instanceId) },
                                    onUndoClick = { onUndoClick(habit.instanceId) },
                                    onEditClick = { onEditClick(habit.habitId) },
                                    onArchiveClick = { onArchiveClick(habit.habitId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimezoneWarningBanner(
    previousTimezone: String?,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Timezone changed",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Your timezone has changed from $previousTimezone. Past data remains unchanged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun EmptyHabitsMessage(onAddHabitClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No habits for today",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your first habit to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onAddHabitClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Habit")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitCard(
    habit: TodayHabitUiModel,
    onClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onSkipClick: () -> Unit,
    onUndoClick: () -> Unit,
    onEditClick: () -> Unit,
    onArchiveClick: () -> Unit
) {
    var showMenu: Boolean by remember { mutableStateOf(false) }

    val cardColor: Color = when (habit.status) {
        HabitStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
        HabitStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceVariant
        HabitStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
        HabitStatus.SUSPENDED -> MaterialTheme.colorScheme.secondaryContainer
        HabitStatus.PENDING -> MaterialTheme.colorScheme.surface
    }

    val isResolved: Boolean = habit.status == HabitStatus.COMPLETED
        || habit.status == HabitStatus.SKIPPED
        || habit.status == HabitStatus.FAILED

    val accentColor: Color = when {
        habit.isSuspended -> MaterialTheme.colorScheme.surfaceVariant
        habit.isDaily && habit.isPending -> MaterialTheme.colorScheme.primary
        habit.isDaily -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        habit.isWeekly && habit.isPending -> MaterialTheme.colorScheme.secondary
        habit.isWeekly -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    // Resolved cards (completed/skipped/failed) are visually dimmed but remain interactive
    // so users can access the undo action via long-press or the card tap.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isResolved) Modifier.alpha(0.65f) else Modifier)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box {
            // Accent bar — rendered first (behind content)
            Box(
                modifier = Modifier
                    .matchParentSize() // fills Box height without crashing in LazyColumn (unlike fillMaxHeight)
                    .width(ACCENT_BAR_WIDTH)
                    .align(Alignment.TopStart)
                    .background(accentColor)
            )

            Column(
                modifier = Modifier.padding(start = ACCENT_BAR_CONTENT_START_PADDING, top = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else null,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (habit.description != null) {
                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Streak and Score info
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (habit.currentStreak > 0) {
                                Text(
                                    text = "🔥 ${habit.currentStreak} day streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "📊 Score: ${habit.scoreText}",
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    habit.scorePercentage >= 100 -> MaterialTheme.colorScheme.primary
                                    habit.scorePercentage >= 75 -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Action buttons
                    when (habit.status) {
                        HabitStatus.PENDING -> {
                            Row {
                                if (!habit.isSkipLocked) {
                                    TextButton(onClick = onSkipClick) {
                                        Text("Skip")
                                    }
                                }
                                OutlinedButton(
                                    onClick = onCompleteClick,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Complete")
                                }
                            }
                        }
                        HabitStatus.COMPLETED, HabitStatus.SKIPPED -> {
                            IconButton(onClick = onUndoClick) {
                                Icon(
                                    Icons.Default.Undo,
                                    contentDescription = "Undo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HabitStatus.SUSPENDED -> {
                            Text(
                                text = "Suspended",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        HabitStatus.FAILED -> {
                            Text(
                                text = "Failed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Progress bar for quantitative habits
                if (habit.type == HabitType.QUANTITATIVE && habit.targetValue != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { habit.progressPercentage },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = habit.progressText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Context menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        onEditClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Archive") },
                    onClick = {
                        showMenu = false
                        onArchiveClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun ProgressRingRow(
    dailyCompleted: Int,
    dailyTotal: Int,
    weeklyCompleted: Int,
    weeklyTotal: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (dailyTotal > 0) {
            RingChip(
                completed = dailyCompleted,
                total = dailyTotal,
                label = "Daily",
                incompleteColor = MaterialTheme.colorScheme.primary,
                modifier = if (weeklyTotal > 0) Modifier.weight(1f) else Modifier
            )
        }
        if (weeklyTotal > 0) {
            RingChip(
                completed = weeklyCompleted,
                total = weeklyTotal,
                label = "Weekly",
                incompleteColor = MaterialTheme.colorScheme.secondary,
                modifier = if (dailyTotal > 0) Modifier.weight(1f) else Modifier
            )
        }
    }
}

@Composable
private fun RingChip(
    completed: Int,
    total: Int,
    label: String,
    incompleteColor: Color,
    modifier: Modifier = Modifier
) {
    val isComplete: Boolean = completed == total
    val ringColor: Color = if (isComplete) MaterialTheme.colorScheme.tertiary else incompleteColor
    val trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
    val sweepAngle: Float = if (total > 0) 360f * completed / total else 0f

    Surface(
        shape = RoundedCornerShape(PILL_CORNER_PERCENT),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(28.dp)) {
                val strokeWidthPx: Float = 5.dp.toPx()
                val radius: Float = (size.minDimension - strokeWidthPx) / 2f
                val topLeft = Offset(
                    x = center.x - radius,
                    y = center.y - radius
                )
                val arcSize = Size(radius * 2, radius * 2)

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
            Column {
                Text(
                    text = "$completed / $total",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
