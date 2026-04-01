package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.presentation.ui.components.toolbar.DynamicCollapsingToolbar
import com.ricardocosteira.habitlock.presentation.ui.components.toolbar.pinnedExitUntilCollapsedToolbarSpec
import com.ricardocosteira.habitlock.presentation.ui.haptics.HapticController
import com.ricardocosteira.habitlock.presentation.ui.haptics.rememberHapticController
import com.ricardocosteira.habitlock.util.formatMonthAbbreviation
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.habit_lock_logo
import habitlock.composeapp.generated.resources.swipe_habit_archived
import habitlock.composeapp.generated.resources.swipe_habit_deleted
import habitlock.composeapp.generated.resources.swipe_undo
import habitlock.composeapp.generated.resources.today_cd_add_habit
import habitlock.composeapp.generated.resources.today_empty_state_cta
import habitlock.composeapp.generated.resources.today_empty_state_heading
import habitlock.composeapp.generated.resources.today_empty_state_subtext
import habitlock.composeapp.generated.resources.today_section_focus
import habitlock.composeapp.generated.resources.today_section_this_week
import habitlock.composeapp.generated.resources.today_section_weekly
import habitlock.composeapp.generated.resources.today_timezone_changed_dismiss
import habitlock.composeapp.generated.resources.today_timezone_changed_message
import habitlock.composeapp.generated.resources.today_timezone_changed_title
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val DIVIDER_ALPHA = 0.3f
private val DIVIDER_HORIZONTAL_PADDING = 16.dp
private val BOTTOM_CLEARANCE = 80.dp
private val TOP_BREATHING_ROOM = 8.dp
private val SECTION_GAP = 16.dp

@Composable
fun TodayScreen(
    onNavigateToHabitDetail: (String) -> Unit,
    onNavigateToCreateHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.todayViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event: TodayEvent ->
            when (event) {
                is TodayEvent.NavigateToHabitDetail -> onNavigateToHabitDetail(event.instanceId)

                TodayEvent.NavigateToCreateHabit -> onNavigateToCreateHabit()

                is TodayEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(
                        getString(event.messageRes)
                    )

                is TodayEvent.ShowError -> snackbarHostState.showSnackbar(event.message)

                is TodayEvent.HabitArchived -> {
                    val result = snackbarHostState.showSnackbar(
                        message = getString(Res.string.swipe_habit_archived),
                        actionLabel = getString(Res.string.swipe_undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoArchive()
                    }
                }

                is TodayEvent.HabitDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = getString(Res.string.swipe_habit_deleted),
                        actionLabel = getString(Res.string.swipe_undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    }
                }

                TodayEvent.UndoCompleted -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }

    TodayScreen(
        state = state,
        onComplete = viewModel::completeHabit,
        onSkip = viewModel::skipHabit,
        onUndo = viewModel::undoHabit,
        onUndoLastIncrement = viewModel::undoLastIncrement,
        onIncrementProgress = viewModel::incrementHabitProgress,
        onCustomProgress = viewModel::showQuantitativeInput,
        onArchive = viewModel::archiveHabitWithUndo,
        onEdit = { habitId: String -> onEditHabit(habitId) },
        onDelete = viewModel::deleteHabit,
        onDismissTimezoneWarning = viewModel::dismissTimezoneWarning,
        onAddFirstHabit = onNavigateToCreateHabit
    )

    state.showQuantitativeInputFor?.let { instanceId ->
        val habit = state.habits.find { it.instanceId == instanceId }
        if (habit != null) {
            QuantitativeInputBottomSheet(
                habit = habit,
                onConfirm = { value -> viewModel.completeQuantitativeHabit(instanceId, value) },
                onDismiss = viewModel::dismissQuantitativeInput
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodayScreen(
    state: TodayState,
    onComplete: (String) -> Unit,
    onSkip: (String) -> Unit,
    onUndo: (String) -> Unit,
    onUndoLastIncrement: (String) -> Unit,
    onIncrementProgress: (String) -> Unit,
    onCustomProgress: (String) -> Unit,
    onArchive: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismissTimezoneWarning: () -> Unit,
    onAddFirstHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val toolbarSpec = pinnedExitUntilCollapsedToolbarSpec()
    val hapticController: HapticController = rememberHapticController()

    var expandedCardIds: Set<String> by rememberSaveable { mutableStateOf(emptySet()) }

    Scaffold(
        topBar = {
            Column {
                if (state.showTimezoneWarning) {
                    TimezoneWarningBanner(
                        previousTimezone = state.previousTimezone,
                        onDismiss = onDismissTimezoneWarning
                    )
                }
                if (!state.isLoading) {
                    DynamicCollapsingToolbar(
                        toolbarSpec = toolbarSpec,
                        backgroundColor = MaterialTheme.colorScheme.background,
                        centerContent = false,
                        collapsedElevation = 0.dp
                    ) { scrollProgress ->
                        TodayHeader(
                            motivationalTitle = state.motivationalTitleRes?.let {
                                stringResource(it)
                            } ?: "",
                            pendingCount = state.pendingCount,
                            hasHabits = state.habits.isNotEmpty(),
                            strictnessPreset = state.strictnessPreset,
                            dailyProgressDisplay = state.dailyProgressDisplay,
                            dailyProgressExact = state.dailyProgressExact,
                            dailyTotal = state.dailyTotal,
                            isCollapsed = scrollProgress > 0.5f
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.habits.isNotEmpty()) {
                FloatingActionButton(onClick = onAddFirstHabit) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.today_cd_add_habit)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { scaffoldPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.habits.isEmpty() -> {
                EmptyHabitsMessage(onAddFirstHabit = onAddFirstHabit)
            }

            else -> {
                val formattedDate: String = rememberFormattedDate()

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(toolbarSpec.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = scaffoldPadding.calculateTopPadding() + TOP_BREATHING_ROOM,
                        bottom = scaffoldPadding.calculateBottomPadding() + BOTTOM_CLEARANCE
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // TODAY'S FOCUS section
                    item(key = "daily_header") {
                        SectionHeader(
                            title = stringResource(Res.string.today_section_focus),
                            trailingLabel = formattedDate
                        )
                    }

                    items(
                        items = state.pendingDaily,
                        key = { it.instanceId }
                    ) { habit ->
                        SwipeableHabitCard(
                            onArchive = { onArchive(habit.habitId) },
                            onEdit = { onEdit(habit.habitId) },
                            onDelete = { onDelete(habit.habitId) },
                            hapticController = hapticController,
                            modifier = Modifier.animateItem()
                        ) {
                            HabitCard(
                                habit = habit,
                                isExpanded = habit.instanceId in expandedCardIds,
                                onToggleExpand = {
                                    expandedCardIds = if (habit.instanceId in expandedCardIds) {
                                        expandedCardIds - habit.instanceId
                                    } else {
                                        expandedCardIds + habit.instanceId
                                    }
                                },
                                onComplete = {
                                    if (habit.type == HabitType.BINARY) {
                                        onComplete(habit.instanceId)
                                    } else {
                                        onIncrementProgress(habit.instanceId)
                                    }
                                },
                                onSkip = { onSkip(habit.instanceId) },
                                onUndo = {
                                    if (habit.type == HabitType.QUANTITATIVE &&
                                        habit.status == HabitStatus.PENDING
                                    ) {
                                        onUndoLastIncrement(habit.instanceId)
                                    } else {
                                        onUndo(habit.instanceId)
                                    }
                                },
                                onIncrementProgress = { onIncrementProgress(habit.instanceId) },
                                onCustomProgress = { onCustomProgress(habit.instanceId) }
                            )
                        }
                    }

                    if (state.resolvedDaily.isNotEmpty()) {
                        item(key = "daily_divider") {
                            HorizontalDivider(
                                modifier = Modifier
                                    .alpha(DIVIDER_ALPHA)
                                    .padding(horizontal = DIVIDER_HORIZONTAL_PADDING)
                            )
                        }

                        items(
                            items = state.resolvedDaily,
                            key = { it.instanceId }
                        ) { habit ->
                            SwipeableHabitCard(
                                onArchive = { onArchive(habit.habitId) },
                                onEdit = { onEdit(habit.habitId) },
                                onDelete = { onDelete(habit.habitId) },
                                hapticController = hapticController,
                                modifier = Modifier.animateItem()
                            ) {
                                HabitCard(
                                    habit = habit,
                                    isExpanded = false,
                                    onToggleExpand = {},
                                    onComplete = {},
                                    onSkip = {},
                                    onUndo = { onUndo(habit.instanceId) },
                                    onIncrementProgress = {},
                                    onCustomProgress = {}
                                )
                            }
                        }
                    }

                    // WEEKLY GOALS section
                    if ((state.pendingWeekly.isNotEmpty() || state.resolvedWeekly.isNotEmpty())) {
                        item(key = "weekly_spacer") {
                            Spacer(modifier = Modifier.height(SECTION_GAP))
                        }

                        item(key = "weekly_header") {
                            SectionHeader(
                                title = stringResource(Res.string.today_section_weekly),
                                trailingLabel = stringResource(Res.string.today_section_this_week)
                            )
                        }

                        items(
                            items = state.pendingWeekly,
                            key = { it.instanceId }
                        ) { habit ->
                            SwipeableHabitCard(
                                onArchive = { onArchive(habit.habitId) },
                                onEdit = { onEdit(habit.habitId) },
                                onDelete = { onDelete(habit.habitId) },
                                hapticController = hapticController,
                                modifier = Modifier.animateItem()
                            ) {
                                HabitCard(
                                    habit = habit,
                                    isExpanded = habit.instanceId in expandedCardIds,
                                    onToggleExpand = {
                                        expandedCardIds = if (habit.instanceId in expandedCardIds) {
                                            expandedCardIds - habit.instanceId
                                        } else {
                                            expandedCardIds + habit.instanceId
                                        }
                                    },
                                    onComplete = {
                                        if (habit.type == HabitType.BINARY) {
                                            onComplete(habit.instanceId)
                                        } else {
                                            onIncrementProgress(habit.instanceId)
                                        }
                                    },
                                    onSkip = { onSkip(habit.instanceId) },
                                    onUndo = {
                                        if (habit.type == HabitType.QUANTITATIVE &&
                                            habit.status == HabitStatus.PENDING
                                        ) {
                                            onUndoLastIncrement(habit.instanceId)
                                        } else {
                                            onUndo(habit.instanceId)
                                        }
                                    },
                                    onIncrementProgress = { onIncrementProgress(habit.instanceId) },
                                    onCustomProgress = { onCustomProgress(habit.instanceId) }
                                )
                            }
                        }

                        if (state.resolvedWeekly.isNotEmpty()) {
                            item(key = "weekly_divider") {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .alpha(DIVIDER_ALPHA)
                                        .padding(horizontal = DIVIDER_HORIZONTAL_PADDING)
                                )
                            }

                            items(
                                items = state.resolvedWeekly,
                                key = { it.instanceId }
                            ) { habit ->
                                SwipeableHabitCard(
                                    onArchive = { onArchive(habit.habitId) },
                                    onEdit = { onEdit(habit.habitId) },
                                    onDelete = { onDelete(habit.habitId) },
                                    hapticController = hapticController,
                                    modifier = Modifier.animateItem()
                                ) {
                                    HabitCard(
                                        habit = habit,
                                        isExpanded = false,
                                        onToggleExpand = {},
                                        onComplete = {},
                                        onSkip = {},
                                        onUndo = { onUndo(habit.instanceId) },
                                        onIncrementProgress = {},
                                        onCustomProgress = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberFormattedDate(): String {
    val now = remember { Clock.System.now() }
    val localDate = remember(now) {
        now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    val monthAbbreviation: String = remember(localDate) {
        formatMonthAbbreviation(localDate.month)
    }

    return remember(localDate) { "$monthAbbreviation ${localDate.day}" }
}

@Composable
private fun TimezoneWarningBanner(previousTimezone: String?, onDismiss: () -> Unit) {
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
                    text = stringResource(Res.string.today_timezone_changed_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = stringResource(
                        Res.string.today_timezone_changed_message,
                        previousTimezone ?: ""
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.today_timezone_changed_dismiss))
            }
        }
    }
}

@Composable
private fun EmptyHabitsMessage(onAddFirstHabit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App icon
        Image(
            painter = painterResource(Res.drawable.habit_lock_logo),
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Heading
        Text(
            text = stringResource(Res.string.today_empty_state_heading),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtext
        Text(
            text = stringResource(Res.string.today_empty_state_subtext),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CTA button
        Button(
            onClick = onAddFirstHabit,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.today_empty_state_cta),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
