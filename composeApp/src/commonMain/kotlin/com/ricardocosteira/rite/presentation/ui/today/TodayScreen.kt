package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.rite.di.LocalAppComponent
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.ui.components.RiteDivider
import com.ricardocosteira.rite.presentation.ui.components.RiteSnackbarContent
import com.ricardocosteira.rite.presentation.ui.components.RiteSnackbarVariant
import com.ricardocosteira.rite.presentation.ui.components.RiteSnackbarVisuals
import com.ricardocosteira.rite.presentation.ui.components.toolbar.DynamicCollapsingToolbar
import com.ricardocosteira.rite.presentation.ui.components.toolbar.pinnedExitUntilCollapsedToolbarSpec
import com.ricardocosteira.rite.presentation.ui.haptics.HapticController
import com.ricardocosteira.rite.presentation.ui.haptics.rememberHapticController
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.snackbar_completed_prefix
import rite.composeapp.generated.resources.snackbar_completed_suffix_no_streak
import rite.composeapp.generated.resources.snackbar_completed_suffix_with_streak
import rite.composeapp.generated.resources.snackbar_deleted_prefix
import rite.composeapp.generated.resources.snackbar_deleted_suffix
import rite.composeapp.generated.resources.snackbar_failed_prefix
import rite.composeapp.generated.resources.snackbar_failed_subtext_limit
import rite.composeapp.generated.resources.snackbar_failed_suffix
import rite.composeapp.generated.resources.snackbar_generic_error
import rite.composeapp.generated.resources.snackbar_skipped_prefix
import rite.composeapp.generated.resources.snackbar_skipped_subtext_remaining
import rite.composeapp.generated.resources.snackbar_skipped_suffix
import rite.composeapp.generated.resources.swipe_undo
import rite.composeapp.generated.resources.today_cd_add_habit
import rite.composeapp.generated.resources.today_section_focus
import rite.composeapp.generated.resources.today_section_kept_count
import rite.composeapp.generated.resources.today_section_met_count
import rite.composeapp.generated.resources.today_section_weekly

private val BOTTOM_CLEARANCE = 80.dp
private val TOP_BREATHING_ROOM = 8.dp
private val SECTION_GAP = 16.dp

private sealed interface HabitFeedItem {
    data class Card(val model: TodayHabitUiModel) : HabitFeedItem
    data class Divider(val id: String) : HabitFeedItem
}

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

                is TodayEvent.HabitCompleted -> {
                    val suffix = event.newStreak?.let {
                        getString(Res.string.snackbar_completed_suffix_with_streak, it)
                    } ?: getString(Res.string.snackbar_completed_suffix_no_streak)
                    snackbarHostState.showSnackbar(
                        RiteSnackbarVisuals(
                            variant = RiteSnackbarVariant.Completed,
                            content = RiteSnackbarContent(
                                prefix = getString(Res.string.snackbar_completed_prefix),
                                emphasized = event.habitName,
                                suffix = suffix
                            )
                        )
                    )
                }

                is TodayEvent.HabitSkipped -> {
                    val subtext = event.skipsRemaining?.let {
                        val pluralS = if (it == 1) "" else "s"
                        getString(Res.string.snackbar_skipped_subtext_remaining, it, pluralS)
                    }
                    snackbarHostState.showSnackbar(
                        RiteSnackbarVisuals(
                            variant = RiteSnackbarVariant.Skipped,
                            content = RiteSnackbarContent(
                                prefix = getString(Res.string.snackbar_skipped_prefix),
                                emphasized = event.habitName,
                                suffix = getString(Res.string.snackbar_skipped_suffix),
                                subtext = subtext
                            )
                        )
                    )
                }

                is TodayEvent.SkipLimitReached -> {
                    snackbarHostState.showSnackbar(
                        RiteSnackbarVisuals(
                            variant = RiteSnackbarVariant.Failed,
                            content = RiteSnackbarContent(
                                prefix = getString(Res.string.snackbar_failed_prefix),
                                emphasized = event.habitName,
                                suffix = getString(Res.string.snackbar_failed_suffix),
                                subtext = getString(Res.string.snackbar_failed_subtext_limit)
                            )
                        )
                    )
                }

                is TodayEvent.HabitDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        RiteSnackbarVisuals(
                            variant = RiteSnackbarVariant.Skipped,
                            content = RiteSnackbarContent(
                                prefix = getString(Res.string.snackbar_deleted_prefix),
                                emphasized = event.habitName,
                                suffix = getString(Res.string.snackbar_deleted_suffix)
                            ),
                            actionLabel = getString(Res.string.swipe_undo),
                            duration = SnackbarDuration.Long
                        )
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    }
                }

                is TodayEvent.ShowError -> snackbarHostState.showSnackbar(
                    RiteSnackbarVisuals(
                        variant = RiteSnackbarVariant.Failed,
                        content = RiteSnackbarContent(
                            prefix = "",
                            emphasized =
                                event.message ?: getString(Res.string.snackbar_generic_error),
                            suffix = ""
                        )
                    )
                )

                is TodayEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.visuals)

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
        onEdit = onEditHabit,
        onDelete = viewModel::deleteHabit,
        onDismissTimezoneWarning = viewModel::dismissTimezoneWarning,
        onAddFirstHabit = onNavigateToCreateHabit,
        onNavigateToDetail = viewModel::navigateToHabitDetail
    )

    state.showQuantitativeInputFor?.let { instanceId ->
        val habit = state.habits.find { it.instanceId == instanceId }
        if (habit != null) {
            QuantitativeInputBottomSheet(
                name = habit.name,
                completedValue = habit.completedValue,
                targetValue = habit.targetValue,
                unit = habit.unit,
                defaultIncrement = habit.defaultIncrement,
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
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismissTimezoneWarning: () -> Unit,
    onAddFirstHabit: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val toolbarSpec = pinnedExitUntilCollapsedToolbarSpec(
        collapsedToolbarHeight = 72.dp
    )
    val hapticController = rememberHapticController()

    Scaffold(
        topBar = {
            Column {
                if (state.showTimezoneWarning) {
                    TimezoneBanner(
                        previousTimezone = state.previousTimezone,
                        onDismiss = onDismissTimezoneWarning
                    )
                }
                if (!state.isLoading) {
                    DynamicCollapsingToolbar(
                        toolbarSpec = toolbarSpec,
                        backgroundColor = RiteAppTheme.colors.background,
                        centerContent = false,
                        collapsedElevation = 0.dp
                    ) { scrollProgress ->
                        TodayHeader(
                            saluteKey = state.motivationalTitleRes,
                            pendingCount = state.pendingCount,
                            dailyTotal = state.dailyTotal,
                            hasHabits = state.habits.isNotEmpty(),
                            dailyProgressFraction = if (state.dailyTotal > 0) {
                                state.dailyProgressExact / state.dailyTotal.toFloat()
                            } else {
                                0f
                            },
                            strictnessPreset = state.strictnessPreset,
                            isCollapsed = scrollProgress > 0.5f
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.habits.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onAddFirstHabit,
                    shape = CircleShape,
                    containerColor = RiteAppTheme.colors.onSurface,
                    contentColor = RiteAppTheme.colors.surface,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 1.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.today_cd_add_habit),
                        modifier = Modifier.size(22.dp)
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
                TodayEmptyState(onAddFirstHabit = onAddFirstHabit)
            }

            else -> {
                val dailyTotal = state.pendingDaily.size + state.resolvedDaily.size
                val dailyKept = state.resolvedDaily.size
                val dailyTrailing = stringResource(
                    Res.string.today_section_kept_count,
                    dailyKept,
                    dailyTotal
                )
                val weeklyTotal = state.pendingWeekly.size + state.resolvedWeekly.size
                val weeklyMet = state.resolvedWeekly.size
                val weeklyTrailing = stringResource(
                    Res.string.today_section_met_count,
                    weeklyMet,
                    weeklyTotal
                )

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
                            trailingLabel = dailyTrailing
                        )
                    }

                    habitFeed(
                        pending = state.pendingDaily,
                        resolved = state.resolvedDaily,
                        dividerKey = "daily_divider",
                        hapticController = hapticController,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onNavigateToDetail = onNavigateToDetail,
                        onComplete = onComplete,
                        onSkip = onSkip,
                        onUndo = onUndo,
                        onUndoLastIncrement = onUndoLastIncrement,
                        onIncrementProgress = onIncrementProgress,
                        onCustomProgress = onCustomProgress
                    )

                    // WEEKLY GOALS section
                    if (state.pendingWeekly.isNotEmpty() || state.resolvedWeekly.isNotEmpty()) {
                        item(key = "weekly_spacer") {
                            Spacer(modifier = Modifier.height(SECTION_GAP))
                        }

                        item(key = "weekly_header") {
                            SectionHeader(
                                title = stringResource(Res.string.today_section_weekly),
                                trailingLabel = weeklyTrailing
                            )
                        }

                        habitFeed(
                            pending = state.pendingWeekly,
                            resolved = state.resolvedWeekly,
                            dividerKey = "weekly_divider",
                            hapticController = hapticController,
                            onEdit = onEdit,
                            onDelete = onDelete,
                            onNavigateToDetail = onNavigateToDetail,
                            onComplete = onComplete,
                            onSkip = onSkip,
                            onUndo = onUndo,
                            onUndoLastIncrement = onUndoLastIncrement,
                            onIncrementProgress = onIncrementProgress,
                            onCustomProgress = onCustomProgress
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.habitFeed(
    pending: List<TodayHabitUiModel>,
    resolved: List<TodayHabitUiModel>,
    dividerKey: String,
    hapticController: HapticController,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onComplete: (String) -> Unit,
    onSkip: (String) -> Unit,
    onUndo: (String) -> Unit,
    onUndoLastIncrement: (String) -> Unit,
    onIncrementProgress: (String) -> Unit,
    onCustomProgress: (String) -> Unit,
) {
    val feed: List<HabitFeedItem> = buildList {
        pending.forEach { add(HabitFeedItem.Card(it)) }
        if (pending.isNotEmpty() && resolved.isNotEmpty()) {
            add(HabitFeedItem.Divider(dividerKey))
        }
        resolved.forEach { add(HabitFeedItem.Card(it)) }
    }
    items(
        items = feed,
        key = { item ->
            when (item) {
                is HabitFeedItem.Card -> item.model.instanceId
                is HabitFeedItem.Divider -> item.id
            }
        },
        contentType = { item ->
            when (item) {
                is HabitFeedItem.Card -> "habit-card"
                is HabitFeedItem.Divider -> "divider"
            }
        }
    ) { item ->
        when (item) {
            is HabitFeedItem.Card -> {
                val habit: TodayHabitUiModel = item.model
                SwipeableHabitCard(
                    onEdit = { onEdit(habit.habitId) },
                    onDelete = { onDelete(habit.habitId) },
                    hapticController = hapticController,
                    modifier = Modifier.animateItem()
                ) {
                    HabitCard(
                        habit = habit,
                        onClick = { onNavigateToDetail(habit.instanceId) },
                        onComplete = {
                            if (habit.type == HabitType.BINARY) {
                                onComplete(habit.instanceId)
                            } else {
                                onIncrementProgress(habit.instanceId)
                            }
                        },
                        onSkip = { onSkip(habit.instanceId) },
                        onUndo = {
                            if (habit.type == HabitType.QUANTITATIVE && habit.isCompleted) {
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

            is HabitFeedItem.Divider -> {
                RiteDivider(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = RiteAppTheme.spacing.gap4)
                )
            }
        }
    }
}
