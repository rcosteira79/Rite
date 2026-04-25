package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.ricardocosteira.rite.di.LocalAppComponent
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.navigation.AddHabitBoundsTransform
import com.ricardocosteira.rite.presentation.navigation.AddHabitContainerMs
import com.ricardocosteira.rite.presentation.navigation.AddHabitFabEnter
import com.ricardocosteira.rite.presentation.navigation.AddHabitFabExit
import com.ricardocosteira.rite.presentation.navigation.AddHabitIconKey
import com.ricardocosteira.rite.presentation.navigation.AddHabitRotationMs
import com.ricardocosteira.rite.presentation.navigation.AddHabitSharedKey
import com.ricardocosteira.rite.presentation.navigation.LocalSharedTransitionScope
import com.ricardocosteira.rite.presentation.navigation.animatedAddHabitSourceShape
import com.ricardocosteira.rite.presentation.ui.components.toolbar.DynamicCollapsingToolbar
import com.ricardocosteira.rite.presentation.ui.components.toolbar.pinnedExitUntilCollapsedToolbarSpec
import com.ricardocosteira.rite.presentation.ui.haptics.HapticController
import com.ricardocosteira.rite.presentation.ui.haptics.rememberHapticController
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import com.ricardocosteira.rite.presentation.ui.today.components.HabitCard
import com.ricardocosteira.rite.presentation.ui.today.components.QuantitativeInputBottomSheet
import com.ricardocosteira.rite.presentation.ui.today.components.SectionHeader
import com.ricardocosteira.rite.presentation.ui.today.components.SwipeableHabitCard
import com.ricardocosteira.rite.presentation.ui.today.components.TimezoneBanner
import com.ricardocosteira.rite.presentation.ui.today.components.TodayEmptyState
import com.ricardocosteira.rite.presentation.ui.today.components.TodayHeaderCollapsed
import com.ricardocosteira.rite.presentation.ui.today.components.TodayHeaderExpanded
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.today_cd_add_habit
import rite.composeapp.generated.resources.today_section_focus
import rite.composeapp.generated.resources.today_section_kept_count
import rite.composeapp.generated.resources.today_section_met_count
import rite.composeapp.generated.resources.today_section_weekly

private val BOTTOM_CLEARANCE = 80.dp
private val TOP_BREATHING_ROOM = 8.dp
private val SECTION_GAP = 16.dp

private val resolvedStatuses = setOf(
    HabitStatus.COMPLETED,
    HabitStatus.SKIPPED,
    HabitStatus.FAILED
)

@Composable
fun TodayScreen(
    onNavigateToHabitDetail: (String) -> Unit,
    onNavigateToCreateHabit: () -> Unit,
    onEditHabit: (String) -> Unit
) {
    val viewModel = LocalAppComponent.current.todayViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is TodayNavEvent.ToHabitDetail -> onNavigateToHabitDetail(event.instanceId)
                TodayNavEvent.ToCreateHabit -> onNavigateToCreateHabit()
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
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
                    val dailyProgressFraction = if (state.dailyTotal > 0) {
                        state.dailyProgressExact / state.dailyTotal.toFloat()
                    } else {
                        0f
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        DynamicCollapsingToolbar(
                            toolbarSpec = toolbarSpec,
                            backgroundColor = RiteAppTheme.colors.background,
                            centerContent = false,
                            collapsedElevation = 0.dp
                        ) { scrollProgress ->
                            TodayHeaderExpanded(
                                saluteKey = state.motivationalTitleRes,
                                pendingCount = state.pendingCount,
                                dailyTotal = state.dailyTotal,
                                hasHabits = state.habits.isNotEmpty(),
                                dailyProgressFraction = dailyProgressFraction,
                                strictnessPreset = state.strictnessPreset,
                                modifier = Modifier.graphicsLayer {
                                    alpha = (1f - scrollProgress).coerceIn(0f, 1f)
                                }
                            )
                        }
                        val collapseFraction = toolbarSpec.state.collapsedFraction
                        if (collapseFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .graphicsLayer { alpha = collapseFraction }
                            ) {
                                TodayHeaderCollapsed(
                                    saluteKey = state.motivationalTitleRes,
                                    pendingCount = state.pendingCount,
                                    dailyTotal = state.dailyTotal,
                                    dailyProgressFraction = dailyProgressFraction,
                                    strictnessPreset = state.strictnessPreset
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.habits.isNotEmpty()) {
                val sharedScope = LocalSharedTransitionScope.current
                val animatedScope = LocalNavAnimatedContentScope.current
                with(sharedScope) {
                    val sourceShape = animatedScope.animatedAddHabitSourceShape()
                    FloatingActionButton(
                        onClick = onAddFirstHabit,
                        shape = sourceShape,
                        containerColor = RiteAppTheme.colors.onSurface,
                        contentColor = RiteAppTheme.colors.surface,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp
                        ),
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(AddHabitSharedKey),
                            animatedVisibilityScope = animatedScope,
                            enter = AddHabitFabEnter,
                            exit = AddHabitFabExit,
                            boundsTransform = AddHabitBoundsTransform,
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                            clipInOverlayDuringTransition = OverlayClip(sourceShape)
                        )
                    ) {
                        // Forward nav (Visible → PostExit): rotate + colour-shift
                        // during the first 60% so the form content can fade in during
                        // the last 40%. Back nav (PreEnter → Visible): delay by 40%
                        // so the form content fades out first, then the icon rotates
                        // back.
                        val iconRotationSpec: FiniteAnimationSpec<Float> =
                            if (animatedScope.transition.targetState ==
                                EnterExitState.Visible
                            ) {
                                tween(
                                    durationMillis = AddHabitRotationMs,
                                    delayMillis = AddHabitContainerMs,
                                    easing = FastOutSlowInEasing
                                )
                            } else {
                                tween(
                                    durationMillis = AddHabitRotationMs,
                                    easing = FastOutSlowInEasing
                                )
                            }
                        val iconRotation: Float by animatedScope.transition.animateFloat(
                            transitionSpec = { iconRotationSpec },
                            label = "fab-icon-rotation"
                        ) { state ->
                            if (state == EnterExitState.Visible) 0f else 45f
                        }
                        // Color only transitions during the container morph phase
                        // — forward: 40–100% (expansion). Back: 0–60% (contraction).
                        val iconColorSpec: FiniteAnimationSpec<Color> =
                            if (animatedScope.transition.targetState ==
                                EnterExitState.Visible
                            ) {
                                // Back nav: color shifts with contraction (0–60%).
                                tween(
                                    durationMillis = AddHabitContainerMs,
                                    easing = FastOutSlowInEasing
                                )
                            } else {
                                // Forward nav: color shifts with expansion (40–100%).
                                tween(
                                    durationMillis = AddHabitContainerMs,
                                    delayMillis = AddHabitRotationMs,
                                    easing = FastOutSlowInEasing
                                )
                            }
                        val iconColor: Color by animatedScope.transition.animateColor(
                            transitionSpec = { iconColorSpec },
                            label = "fab-icon-color"
                        ) { state ->
                            if (state == EnterExitState.Visible) {
                                RiteAppTheme.colors.surface
                            } else {
                                RiteAppTheme.colors.onSurface
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.today_cd_add_habit),
                            tint = iconColor,
                            modifier = Modifier
                                // sharedBounds must be *outside* (earlier in chain) so the
                                // overlay captures the graphicsLayer rotation applied to
                                // the inner content. With graphicsLayer outside, the
                                // overlay renders the untransformed Icon and only the
                                // in-place render shows the rotation.
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                        AddHabitIconKey
                                    ),
                                    animatedVisibilityScope = animatedScope,
                                    boundsTransform = AddHabitBoundsTransform,
                                    zIndexInOverlay = 1f
                                )
                                .size(22.dp)
                                .graphicsLayer { rotationZ = iconRotation }
                        )
                    }
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
                val dailyTotal = state.daily.size
                val dailyKept = state.daily.count { it.status in resolvedStatuses }
                val dailyTrailing = stringResource(
                    Res.string.today_section_kept_count,
                    dailyKept,
                    dailyTotal
                )
                val weeklyTotal = state.weekly.size
                val weeklyMet = state.weekly.count { it.status in resolvedStatuses }
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
                        habits = state.daily,
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
                    if (state.weekly.isNotEmpty()) {
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
                            habits = state.weekly,
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
    habits: List<TodayHabitUiModel>,
    hapticController: HapticController,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onComplete: (String) -> Unit,
    onSkip: (String) -> Unit,
    onUndo: (String) -> Unit,
    onUndoLastIncrement: (String) -> Unit,
    onIncrementProgress: (String) -> Unit,
    onCustomProgress: (String) -> Unit
) {
    items(
        items = habits,
        key = { it.instanceId },
        contentType = { "habit-card" }
    ) { habit ->
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
}
