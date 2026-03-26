package com.ricardocosteira.habitlock.presentation.ui.today

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_edit
import habitlock.composeapp.generated.resources.common_error_generic
import habitlock.composeapp.generated.resources.common_failed
import habitlock.composeapp.generated.resources.common_skip
import habitlock.composeapp.generated.resources.today_action_archive
import habitlock.composeapp.generated.resources.today_cd_complete
import habitlock.composeapp.generated.resources.today_cd_undo
import habitlock.composeapp.generated.resources.today_empty_state_add_habit
import habitlock.composeapp.generated.resources.today_empty_state_heading
import habitlock.composeapp.generated.resources.today_empty_state_subtext
import habitlock.composeapp.generated.resources.today_error_skip_limit_reached
import habitlock.composeapp.generated.resources.today_score
import habitlock.composeapp.generated.resources.today_section_daily_habits
import habitlock.composeapp.generated.resources.today_section_suspended_habits
import habitlock.composeapp.generated.resources.today_section_weekly_habits
import habitlock.composeapp.generated.resources.today_status_suspended
import habitlock.composeapp.generated.resources.today_streak
import habitlock.composeapp.generated.resources.today_success_action_undone
import habitlock.composeapp.generated.resources.today_success_habit_archived
import habitlock.composeapp.generated.resources.today_success_habit_completed
import habitlock.composeapp.generated.resources.today_success_habit_skipped
import habitlock.composeapp.generated.resources.today_success_progress_added
import habitlock.composeapp.generated.resources.today_timezone_changed_dismiss
import habitlock.composeapp.generated.resources.today_timezone_changed_message
import habitlock.composeapp.generated.resources.today_timezone_changed_title
import org.jetbrains.compose.resources.stringResource

private val SECTION_HEADER_LETTER_SPACING = 0.8.sp
private val ACCENT_BAR_WIDTH = 3.dp
private val ACCENT_BAR_CONTENT_START_PADDING = ACCENT_BAR_WIDTH + 16.dp // bar width + standard padding

@Composable
fun TodayScreen(
    onNavigateToHabitDetail: (String) -> Unit,
    onNavigateToCreateHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = LocalAppComponent.current.todayViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    val messageHabitCompleted = stringResource(Res.string.today_success_habit_completed)
    val messageProgressAdded = stringResource(Res.string.today_success_progress_added)
    val messageHabitSkipped = stringResource(Res.string.today_success_habit_skipped)
    val messageActionUndone = stringResource(Res.string.today_success_action_undone)
    val messageHabitArchived = stringResource(Res.string.today_success_habit_archived)
    val messageSkipLimitReached = stringResource(Res.string.today_error_skip_limit_reached)
    val messageGenericError = stringResource(Res.string.common_error_generic)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TodayEvent.NavigateToHabitDetail -> onNavigateToHabitDetail(event.instanceId)
                TodayEvent.NavigateToCreateHabit -> onNavigateToCreateHabit()
                TodayEvent.HabitCompleted -> snackbarHostState.showSnackbar(messageHabitCompleted)
                TodayEvent.ProgressAdded -> snackbarHostState.showSnackbar(messageProgressAdded)
                TodayEvent.HabitSkipped -> snackbarHostState.showSnackbar(messageHabitSkipped)
                TodayEvent.ActionUndone -> snackbarHostState.showSnackbar(messageActionUndone)
                TodayEvent.HabitArchived -> snackbarHostState.showSnackbar(messageHabitArchived)
                TodayEvent.SkipLimitReached -> snackbarHostState.showSnackbar(messageSkipLimitReached)
                is TodayEvent.ShowError -> snackbarHostState.showSnackbar(event.message ?: messageGenericError)
            }
        }
    }

    TodayScreen(
        state = state,
        onHabitClick = viewModel::navigateToHabitDetail,
        onCompleteClick = viewModel::completeHabit,
        onSkipClick = viewModel::skipHabit,
        onUndoClick = viewModel::undoHabit,
        onEditClick = onEditHabit,
        onArchiveClick = viewModel::archiveHabit,
        onAddHabitClick = viewModel::navigateToCreateHabit,
        onDismissTimezoneWarning = viewModel::dismissTimezoneWarning,
    )

    state.showQuantitativeInputFor?.let { instanceId ->
        val habit = state.habits.find { it.instanceId == instanceId }
        if (habit != null) {
            QuantitativeInputBottomSheet(
                habit = habit,
                onConfirm = { value -> viewModel.completeQuantitativeHabit(instanceId, value) },
                onDismiss = viewModel::dismissQuantitativeInput,
            )
        }
    }
}

@Composable
private fun TodayScreen(
    state: TodayState,
    onHabitClick: (String) -> Unit,
    onCompleteClick: (String) -> Unit,
    onSkipClick: (String) -> Unit,
    onUndoClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onArchiveClick: (String) -> Unit,
    onAddHabitClick: () -> Unit,
    onDismissTimezoneWarning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val isHeaderCollapsed: Boolean by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        // Timezone warning banner
        if (state.showTimezoneWarning) {
            TimezoneWarningBanner(
                previousTimezone = state.previousTimezone,
                onDismiss = onDismissTimezoneWarning,
            )
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
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

                TodayHeader(
                    motivationalTitle = state.motivationalTitle,
                    pendingCount = state.pendingCount,
                    strictnessPreset = state.strictnessPreset,
                    dailyResolved = state.dailyResolved,
                    dailyTotal = state.dailyTotal,
                    isCollapsed = isHeaderCollapsed,
                )

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Daily Habits Section
                    if (dailyHabits.isNotEmpty()) {
                        item {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = stringResource(Res.string.today_section_daily_habits),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = SECTION_HEADER_LETTER_SPACING,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = "${state.dailyResolved} / ${state.dailyTotal}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        items(
                            items = dailyHabits,
                            key = { it.instanceId },
                        ) { habit ->
                            HabitCard(
                                habit = habit,
                                onClick = { onHabitClick(habit.instanceId) },
                                onCompleteClick = { onCompleteClick(habit.instanceId) },
                                onSkipClick = { onSkipClick(habit.instanceId) },
                                onUndoClick = { onUndoClick(habit.instanceId) },
                                onEditClick = { onEditClick(habit.habitId) },
                                onArchiveClick = { onArchiveClick(habit.habitId) },
                            )
                        }
                    }

                    // Weekly Habits Section
                    if (weeklyHabits.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(Res.string.today_section_weekly_habits),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = SECTION_HEADER_LETTER_SPACING,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                        items(
                            items = weeklyHabits,
                            key = { it.instanceId },
                        ) { habit ->
                            HabitCard(
                                habit = habit,
                                onClick = { onHabitClick(habit.instanceId) },
                                onCompleteClick = { onCompleteClick(habit.instanceId) },
                                onSkipClick = { onSkipClick(habit.instanceId) },
                                onUndoClick = { onUndoClick(habit.instanceId) },
                                onEditClick = { onEditClick(habit.habitId) },
                                onArchiveClick = { onArchiveClick(habit.habitId) },
                            )
                        }
                    }

                    // Suspended Habits Section
                    if (suspendedHabits.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(Res.string.today_section_suspended_habits),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = SECTION_HEADER_LETTER_SPACING,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                        items(
                            items = suspendedHabits,
                            key = { it.instanceId },
                        ) { habit ->
                            HabitCard(
                                habit = habit,
                                onClick = { onHabitClick(habit.instanceId) },
                                onCompleteClick = { onCompleteClick(habit.instanceId) },
                                onSkipClick = { onSkipClick(habit.instanceId) },
                                onUndoClick = { onUndoClick(habit.instanceId) },
                                onEditClick = { onEditClick(habit.habitId) },
                                onArchiveClick = { onArchiveClick(habit.habitId) },
                            )
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
    onDismiss: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.today_timezone_changed_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = stringResource(Res.string.today_timezone_changed_message, previousTimezone ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.today_timezone_changed_dismiss))
            }
        }
    }
}

@Composable
private fun EmptyHabitsMessage(onAddHabitClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.today_empty_state_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.today_empty_state_subtext),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onAddHabitClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.today_empty_state_add_habit))
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
    onArchiveClick: () -> Unit,
) {
    var showMenu: Boolean by remember { mutableStateOf(false) }

    val cardColor: Color =
        when (habit.status) {
            HabitStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
            HabitStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceVariant
            HabitStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
            HabitStatus.SUSPENDED -> MaterialTheme.colorScheme.secondaryContainer
            HabitStatus.PENDING -> MaterialTheme.colorScheme.surface
        }

    val isResolved: Boolean =
        habit.status == HabitStatus.COMPLETED ||
            habit.status == HabitStatus.SKIPPED ||
            habit.status == HabitStatus.FAILED

    val accentColor: Color =
        when {
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
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (isResolved) Modifier.alpha(0.65f) else Modifier)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true },
                ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
    ) {
        Box {
            // Accent bar — rendered first (behind content)
            Box(
                modifier =
                    Modifier
                        .matchParentSize() // fills Box height without crashing in LazyColumn (unlike fillMaxHeight)
                        .width(ACCENT_BAR_WIDTH)
                        .align(Alignment.TopStart)
                        .background(accentColor),
            )

            Column(
                modifier = Modifier.padding(start = ACCENT_BAR_CONTENT_START_PADDING, top = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else null,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        if (habit.description != null) {
                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        // Streak and Score info
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (habit.currentStreak > 0) {
                                Text(
                                    text = stringResource(Res.string.today_streak, habit.currentStreak),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            Text(
                                text = stringResource(Res.string.today_score, habit.scoreText),
                                style = MaterialTheme.typography.labelSmall,
                                color =
                                    when {
                                        habit.scorePercentage >= 100 -> MaterialTheme.colorScheme.primary
                                        habit.scorePercentage >= 75 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
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
                                        Text(stringResource(Res.string.common_skip))
                                    }
                                }
                                OutlinedButton(
                                    onClick = onCompleteClick,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = stringResource(Res.string.today_cd_complete))
                                }
                            }
                        }

                        HabitStatus.COMPLETED, HabitStatus.SKIPPED -> {
                            IconButton(onClick = onUndoClick) {
                                Icon(
                                    Icons.Default.Undo,
                                    contentDescription = stringResource(Res.string.today_cd_undo),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        HabitStatus.SUSPENDED -> {
                            Text(
                                text = stringResource(Res.string.today_status_suspended),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }

                        HabitStatus.FAILED -> {
                            Text(
                                text = stringResource(Res.string.common_failed),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Context menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.common_edit)) },
                    onClick = {
                        showMenu = false
                        onEditClick()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.today_action_archive)) },
                    onClick = {
                        showMenu = false
                        onArchiveClick()
                    },
                )
            }
        }
    }
}
