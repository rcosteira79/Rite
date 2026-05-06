package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ricardocosteira.rite.di.LocalAppComponent
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.components.ProgressRing
import com.ricardocosteira.rite.presentation.ui.habitdetail.components.CategoryEyebrow
import com.ricardocosteira.rite.presentation.ui.habitdetail.components.EnforcementLimitsTable
import com.ricardocosteira.rite.presentation.ui.habitdetail.components.HabitDetailAction
import com.ricardocosteira.rite.presentation.ui.habitdetail.components.StatTileRow
import com.ricardocosteira.rite.presentation.ui.habitdetail.components.Tapestry
import com.ricardocosteira.rite.presentation.ui.habitdetail.components.formatWeekRange
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import com.ricardocosteira.rite.presentation.ui.today.components.QuantitativeInputBottomSheet
import com.ricardocosteira.rite.util.todayIn
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_cd_back
import rite.composeapp.generated.resources.habit_detail_cd_edit
import rite.composeapp.generated.resources.habit_form_cd_archive
import rite.composeapp.generated.resources.habit_form_cd_delete
import rite.composeapp.generated.resources.habit_form_delete_dialog_body
import rite.composeapp.generated.resources.habit_form_delete_dialog_cancel
import rite.composeapp.generated.resources.habit_form_delete_dialog_confirm
import rite.composeapp.generated.resources.habit_form_delete_dialog_title

@Composable
fun HabitDetailScreen(
    instanceId: String,
    onNavigateBack: () -> Unit,
    onEditHabit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val createViewModel = LocalAppComponent.current.createHabitDetailViewModel
    val viewModel: HabitDetailViewModel = viewModel { createViewModel(instanceId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HabitDetailEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    HabitDetailScreen(
        state = state,
        onBackClick = onNavigateBack,
        onComplete = viewModel::completeBinary,
        onIncrementProgress = viewModel::incrementProgress,
        onCustomProgress = viewModel::showCustomInput,
        onSkip = viewModel::skip,
        onUndo = viewModel::undo,
        onUndoIncrement = viewModel::undoIncrement,
        onEditHabit = { state.habit?.habitId?.let(onEditHabit) },
        onArchiveHabit = viewModel::archiveHabit,
        onDeleteHabit = { isDeleteDialogVisible = true },
        modifier = modifier
    )

    if (isDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogVisible = false },
            title = { Text(stringResource(Res.string.habit_form_delete_dialog_title)) },
            text = { Text(stringResource(Res.string.habit_form_delete_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteDialogVisible = false
                        viewModel.deleteHabit()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = RiteAppTheme.colors.error
                    )
                ) { Text(stringResource(Res.string.habit_form_delete_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteDialogVisible = false }) {
                    Text(stringResource(Res.string.habit_form_delete_dialog_cancel))
                }
            }
        )
    }

    val customInputHabit = state.habit
    if (state.showCustomInput && customInputHabit != null) {
        QuantitativeInputBottomSheet(
            name = customInputHabit.name,
            completedValue = customInputHabit.completedValue,
            targetValue = customInputHabit.targetValue,
            unit = customInputHabit.unit,
            defaultIncrement = customInputHabit.defaultIncrement,
            onConfirm = { value ->
                viewModel.addCustomProgress(value)
                viewModel.dismissCustomInput()
            },
            onDismiss = viewModel::dismissCustomInput
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitDetailScreen(
    state: HabitDetailState,
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onUndoIncrement: () -> Unit,
    onEditHabit: () -> Unit,
    onArchiveHabit: () -> Unit,
    onDeleteHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState: ScrollState = rememberScrollState()
    val isScrolled: Boolean by remember { derivedStateOf { scrollState.value > 0 } }
    val filledColor: Color = RiteAppTheme.colors.surfaceContainerHighest
    val targetColor: Color = if (isScrolled) filledColor else filledColor.copy(alpha = 0f)
    val iconContainerColor: Color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 200),
        label = "iconContainerColor"
    )

    Scaffold(
        modifier = modifier,
        containerColor = RiteAppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = iconContainerColor
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onEditHabit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = iconContainerColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(
                                Res.string.habit_detail_cd_edit
                            ),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onArchiveHabit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = iconContainerColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = stringResource(
                                Res.string.habit_form_cd_archive
                            ),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteHabit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = iconContainerColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(
                                Res.string.habit_form_cd_delete
                            ),
                            tint = RiteAppTheme.colors.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading || state.habit == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val habit = state.habit
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                CategoryEyebrow(type = habit.type)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = habit.name,
                    style = RiteAppTheme.typography.displaySmall.copy(
                        fontSize = 34.sp,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.68).sp
                    ),
                    fontWeight = FontWeight.Normal,
                    color = RiteAppTheme.colors.onSurface
                )
                habit.description?.let { note ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = note,
                        style = RiteAppTheme.typography.titleLarge.copy(
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        ),
                        color = RiteAppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.widthIn(max = 300.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ProgressRing(
                        progress = habit.progressPercentage.coerceIn(0f, 1f),
                        capLabel = null
                    )
                }
                Spacer(Modifier.height(20.dp))

                HabitDetailAction(
                    type = habit.type,
                    status = habit.status,
                    currentProgress = habit.currentProgress,
                    unit = habit.unit,
                    isSkipLocked = habit.isSkipLocked,
                    onComplete = onComplete,
                    onIncrementProgress = onIncrementProgress,
                    onCustomProgress = onCustomProgress,
                    onSkip = onSkip,
                    onUndo = onUndo,
                    onUndoIncrement = onUndoIncrement
                )
                Spacer(Modifier.height(24.dp))

                StatTileRow(
                    currentStreak = habit.currentStreak,
                    longestStreak = habit.longestStreak,
                    habitScore = habit.habitScore
                )
                Spacer(Modifier.height(20.dp))

                val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
                val startDate = remember(today) { today.minus(DatePeriod(days = 90)) }
                val weekRangeLabel = remember(today, startDate) {
                    formatWeekRange(from = startDate, to = today)
                }

                Tapestry(
                    heatmapData = state.heatmapData,
                    weekRangeLabel = weekRangeLabel
                )
                Spacer(Modifier.height(24.dp))

                EnforcementLimitsTable(
                    strictnessPreset = habit.strictnessPreset,
                    undoPolicy = habit.undoPolicy,
                    snoozesUsedToday = habit.snoozesUsedToday,
                    maxSnoozesPerDay = habit.maxSnoozesPerDay,
                    skipsThisWeek = habit.skipsThisWeek,
                    currentConsecutiveSkips = habit.currentConsecutiveSkips,
                    maxConsecutiveSkips = habit.maxConsecutiveSkips
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
