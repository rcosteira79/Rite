package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import com.ricardocosteira.rite.presentation.ui.today.habitcard.HabitCardAction
import com.ricardocosteira.rite.presentation.ui.today.habitcard.HabitCardKickerRow
import com.ricardocosteira.rite.presentation.ui.today.habitcard.HabitCardState
import com.ricardocosteira.rite.presentation.ui.today.habitcard.MarginRule
import com.ricardocosteira.rite.presentation.ui.today.habitcard.visualsFor

private val CARD_VERTICAL_PADDING = 14.dp
private val CARD_LEFT_PADDING = 22.dp
private val CARD_RIGHT_PADDING = 16.dp
private val BODY_COLUMN_GAP = 4.dp
private val RULE_RIGHT_GAP = 14.dp

@Composable
fun HabitCard(
    habit: TodayHabitUiModel,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    onIncrementProgress: () -> Unit,
    onCustomProgress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visuals =
        visualsFor(
            status = habit.status,
            type = habit.type,
            progressPercentage = habit.progressPercentage,
        )

    Surface(
        onClick = onClick,
        shape = RiteAppTheme.shapes.sm,
        color = Color.Transparent,
        contentColor = RiteAppTheme.colors.onSurface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    start = CARD_LEFT_PADDING,
                    end = CARD_RIGHT_PADDING,
                    top = CARD_VERTICAL_PADDING,
                    bottom = CARD_VERTICAL_PADDING,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MarginRule(
                state = visuals.state,
                fillFraction = visuals.fillFraction,
                modifier =
                    Modifier
                        .width(5.dp)
                        .height(48.dp),
            )
            Spacer(modifier = Modifier.width(RULE_RIGHT_GAP))
            HabitCardBody(
                habit = habit,
                state = visuals.state,
                modifier = Modifier.weight(1f),
            )
            HabitCardAction(
                state = visuals.state,
                type = habit.type,
                defaultIncrement = habit.defaultIncrement,
                skipLocked = habit.isSkipLocked,
                onComplete = {
                    if (habit.type == HabitType.BINARY) onComplete() else onIncrementProgress()
                },
                onIncrement = onIncrementProgress,
                onSkip = onSkip,
                onUndo = onUndo,
                modifier = Modifier,
            )
        }
    }
}

@Composable
private fun HabitCardBody(
    habit: TodayHabitUiModel,
    state: HabitCardState,
    modifier: Modifier = Modifier,
) {
    val colors = RiteAppTheme.colors
    val nameColor: Color =
        when (state) {
            HabitCardState.Completed -> colors.primary
            HabitCardState.Failed -> colors.error
            HabitCardState.Skipped -> colors.onSurfaceMuted
            HabitCardState.Suspended -> colors.suspend
            else -> colors.onSurface
        }
    val nameDecoration: TextDecoration =
        if (state == HabitCardState.Completed) TextDecoration.LineThrough else TextDecoration.None
    val nameStyle =
        RiteAppTheme.typography.titleLarge.copy(
            fontStyle = if (state ==
                HabitCardState.Suspended
            ) {
                FontStyle.Italic
            } else {
                FontStyle.Normal
            },
        )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(BODY_COLUMN_GAP),
    ) {
        HabitCardKickerRow(
            state = state,
            kicker = todayHabitKicker(habit, state),
            streakDays = habit.currentStreak,
        )
        Text(
            text = habit.name,
            style = nameStyle,
            color = nameColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = nameDecoration,
        )
        val sub: String? = habitCardSubText(habit, state)
        if (sub != null) {
            Text(
                text = sub,
                style = RiteAppTheme.typography.mono,
                color = colors.onSurfaceSubtle,
            )
        }
    }
}

internal fun todayHabitKicker(habit: TodayHabitUiModel, state: HabitCardState): String {
    val unit: String = habit.unit ?: ""
    val cur: Int = habit.completedValue ?: 0
    val target: Int = habit.targetValue ?: 0
    return when {
        habit.type == HabitType.QUANTITATIVE &&
            (state == HabitCardState.Pending || state == HabitCardState.PendingInProgress) ->
            listOf("$cur", "of", "$target", unit).filter { it.isNotEmpty() }.joinToString(" ")

        habit.type == HabitType.QUANTITATIVE && habit.cadence == ScheduleType.FLEXIBLE_WEEKLY ->
            listOfNotNull("Any day", "$target $unit per week".takeIf { unit.isNotEmpty() })
                .joinToString(" · ")
                .ifBlank { "Weekly" }

        habit.type == HabitType.QUANTITATIVE ->
            listOfNotNull("Daily", "$target $unit".takeIf { unit.isNotEmpty() || target > 0 })
                .joinToString(" · ")

        habit.cadence == ScheduleType.FLEXIBLE_WEEKLY ->
            listOfNotNull("Any day", habit.description).joinToString(" · ")

        habit.cadence == ScheduleType.WEEKLY ->
            listOfNotNull("Fixed days", habit.description).joinToString(" · ")

        // BINARY DAILY
        else -> listOfNotNull("Daily", habit.description).joinToString(" · ")
    }
}

internal fun habitCardSubText(habit: TodayHabitUiModel, state: HabitCardState): String? =
    when (state) {
        HabitCardState.Completed -> habit.completedAtText?.let { "Completed $it" }
        HabitCardState.Skipped -> habit.completedAtText?.let { "Skipped $it" }
        else -> null
    }
