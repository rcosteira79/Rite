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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_error_generic
import habitlock.composeapp.generated.resources.ic_launcher_foreground
import habitlock.composeapp.generated.resources.today_empty_state_cta
import habitlock.composeapp.generated.resources.today_empty_state_heading
import habitlock.composeapp.generated.resources.today_empty_state_subtext
import habitlock.composeapp.generated.resources.today_error_skip_limit_reached
import habitlock.composeapp.generated.resources.today_section_focus
import habitlock.composeapp.generated.resources.today_section_this_week
import habitlock.composeapp.generated.resources.today_section_weekly
import habitlock.composeapp.generated.resources.today_success_action_undone
import habitlock.composeapp.generated.resources.today_success_habit_archived
import habitlock.composeapp.generated.resources.today_success_habit_completed
import habitlock.composeapp.generated.resources.today_success_habit_skipped
import habitlock.composeapp.generated.resources.today_success_progress_added
import habitlock.composeapp.generated.resources.today_timezone_changed_dismiss
import habitlock.composeapp.generated.resources.today_timezone_changed_message
import habitlock.composeapp.generated.resources.today_timezone_changed_title
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

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
        onComplete = viewModel::completeHabit,
        onSkip = viewModel::skipHabit,
        onUndo = viewModel::undoHabit,
        onIncrementProgress = viewModel::incrementHabitProgress,
        onCustomProgress = viewModel::showQuantitativeInput,
        onDismissTimezoneWarning = viewModel::dismissTimezoneWarning,
        onAddFirstHabit = onNavigateToCreateHabit,
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
internal fun TodayScreen(
    state: TodayState,
    onComplete: (String) -> Unit,
    onSkip: (String) -> Unit,
    onUndo: (String) -> Unit,
    onIncrementProgress: (String) -> Unit,
    onCustomProgress: (String) -> Unit,
    onDismissTimezoneWarning: () -> Unit,
    onAddFirstHabit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val isHeaderCollapsed: Boolean by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
    }

    var expandedCardId: String? by remember { mutableStateOf(null) }

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

        // Header shows on all non-loading states
        if (!state.isLoading) {
            TodayHeader(
                motivationalTitle = state.motivationalTitle,
                pendingCount = state.pendingCount,
                hasHabits = state.habits.isNotEmpty(),
                strictnessPreset = state.strictnessPreset,
                dailyResolved = state.dailyResolved,
                dailyTotal = state.dailyTotal,
                isCollapsed = isHeaderCollapsed,
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
                EmptyHabitsMessage(onAddFirstHabit = onAddFirstHabit)
            }

            else -> {
                // Partition habits into groups
                val dailyHabits: List<TodayHabitUiModel> =
                    state.habits.filter { it.isDaily && !it.isSuspended }
                val weeklyHabits: List<TodayHabitUiModel> =
                    state.habits.filter { it.isWeekly && !it.isSuspended }

                val (pendingDaily: List<TodayHabitUiModel>, resolvedDaily: List<TodayHabitUiModel>) =
                    dailyHabits.partition { !it.isCompleted && !it.isSkipped && !it.isFailed }
                val (pendingWeekly: List<TodayHabitUiModel>, resolvedWeekly: List<TodayHabitUiModel>) =
                    weeklyHabits.partition { !it.isCompleted && !it.isSkipped && !it.isFailed }

                val formattedDate: String = rememberFormattedDate()

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Top breathing room
                    item(key = "top_spacer") {
                        Spacer(modifier = Modifier.height(TOP_BREATHING_ROOM))
                    }

                    // TODAY'S FOCUS section
                    item(key = "daily_header") {
                        SectionHeader(
                            title = stringResource(Res.string.today_section_focus),
                            trailingLabel = formattedDate,
                        )
                    }

                    items(
                        items = pendingDaily,
                        key = { it.instanceId },
                    ) { habit ->
                        HabitCard(
                            habit = habit,
                            isExpanded = expandedCardId == habit.instanceId,
                            onToggleExpand = {
                                expandedCardId =
                                    if (expandedCardId == habit.instanceId) null else habit.instanceId
                            },
                            onComplete = {
                                if (habit.type == HabitType.BINARY) {
                                    onComplete(habit.instanceId)
                                } else {
                                    onIncrementProgress(habit.instanceId)
                                }
                            },
                            onSkip = { onSkip(habit.instanceId) },
                            onUndo = { onUndo(habit.instanceId) },
                            onIncrementProgress = { onIncrementProgress(habit.instanceId) },
                            onCustomProgress = { onCustomProgress(habit.instanceId) },
                        )
                    }

                    if (resolvedDaily.isNotEmpty()) {
                        item(key = "daily_divider") {
                            HorizontalDivider(
                                modifier =
                                    Modifier
                                        .alpha(DIVIDER_ALPHA)
                                        .padding(horizontal = DIVIDER_HORIZONTAL_PADDING),
                            )
                        }

                        items(
                            items = resolvedDaily,
                            key = { it.instanceId },
                        ) { habit ->
                            HabitCard(
                                habit = habit,
                                isExpanded = false,
                                onToggleExpand = {},
                                onComplete = {},
                                onSkip = {},
                                onUndo = { onUndo(habit.instanceId) },
                                onIncrementProgress = {},
                                onCustomProgress = {},
                            )
                        }
                    }

                    // WEEKLY GOALS section
                    if (weeklyHabits.isNotEmpty()) {
                        item(key = "weekly_spacer") {
                            Spacer(modifier = Modifier.height(SECTION_GAP))
                        }

                        item(key = "weekly_header") {
                            SectionHeader(
                                title = stringResource(Res.string.today_section_weekly),
                                trailingLabel = stringResource(Res.string.today_section_this_week),
                            )
                        }

                        items(
                            items = pendingWeekly,
                            key = { it.instanceId },
                        ) { habit ->
                            HabitCard(
                                habit = habit,
                                isExpanded = expandedCardId == habit.instanceId,
                                onToggleExpand = {
                                    expandedCardId =
                                        if (expandedCardId == habit.instanceId) null else habit.instanceId
                                },
                                onComplete = {
                                    if (habit.type == HabitType.BINARY) {
                                        onComplete(habit.instanceId)
                                    } else {
                                        onIncrementProgress(habit.instanceId)
                                    }
                                },
                                onSkip = { onSkip(habit.instanceId) },
                                onUndo = { onUndo(habit.instanceId) },
                                onIncrementProgress = { onIncrementProgress(habit.instanceId) },
                                onCustomProgress = { onCustomProgress(habit.instanceId) },
                            )
                        }

                        if (resolvedWeekly.isNotEmpty()) {
                            item(key = "weekly_divider") {
                                HorizontalDivider(
                                    modifier =
                                        Modifier
                                            .alpha(DIVIDER_ALPHA)
                                            .padding(horizontal = DIVIDER_HORIZONTAL_PADDING),
                                )
                            }

                            items(
                                items = resolvedWeekly,
                                key = { it.instanceId },
                            ) { habit ->
                                HabitCard(
                                    habit = habit,
                                    isExpanded = false,
                                    onToggleExpand = {},
                                    onComplete = {},
                                    onSkip = {},
                                    onUndo = { onUndo(habit.instanceId) },
                                    onIncrementProgress = {},
                                    onCustomProgress = {},
                                )
                            }
                        }
                    }

                    // Bottom clearance for nav bar
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(BOTTOM_CLEARANCE))
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberFormattedDate(): String {
    val now = remember { Clock.System.now() }
    val localDate =
        remember(now) {
            now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }

    val monthAbbreviation: String =
        remember(localDate) {
            formatMonthAbbreviation(localDate.month)
        }

    return remember(localDate) { "$monthAbbreviation ${localDate.day}" }
}

private fun formatMonthAbbreviation(month: Month): String =
    when (month) {
        Month.JANUARY -> {
            "Jan"
        }

        Month.FEBRUARY -> {
            "Feb"
        }

        Month.MARCH -> {
            "Mar"
        }

        Month.APRIL -> {
            "Apr"
        }

        Month.MAY -> {
            "May"
        }

        Month.JUNE -> {
            "Jun"
        }

        Month.JULY -> {
            "Jul"
        }

        Month.AUGUST -> {
            "Aug"
        }

        Month.SEPTEMBER -> {
            "Sep"
        }

        Month.OCTOBER -> {
            "Oct"
        }

        Month.NOVEMBER -> {
            "Nov"
        }

        Month.DECEMBER -> {
            "Dec"
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
private fun EmptyHabitsMessage(onAddFirstHabit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // App icon
        Image(
            painter = painterResource(Res.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier =
                Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Heading
        Text(
            text = stringResource(Res.string.today_empty_state_heading),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtext
        Text(
            text = stringResource(Res.string.today_empty_state_subtext),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CTA button
        Button(
            onClick = onAddFirstHabit,
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.today_empty_state_cta),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
