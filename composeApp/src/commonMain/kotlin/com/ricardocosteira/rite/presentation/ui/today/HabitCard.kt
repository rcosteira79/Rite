package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontStyle
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
private val CARD_LEFT_PADDING = 6.dp
private val CARD_RIGHT_PADDING = 16.dp
private val BODY_COLUMN_GAP = 4.dp
private val RULE_RIGHT_GAP = 11.dp

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
    val colors = RiteAppTheme.colors
    val motion = RiteAppTheme.motion
    val targetBackground: Color = when (visuals.state) {
        HabitCardState.Pending, HabitCardState.PendingInProgress -> colors.surface
        HabitCardState.Completed -> colors.primaryContainer.copy(alpha = 0.55f)
        HabitCardState.Failed -> colors.errorContainer.copy(alpha = 0.5f)
        HabitCardState.Suspended -> colors.suspend.copy(alpha = 0.1f)
        HabitCardState.Skipped -> Color.Transparent
    }
    val animatedBackground by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = tween(
            durationMillis = motion.deliberate.inWholeMilliseconds.toInt(),
            easing = motion.easeQuiet
        ),
        label = "habit-card-bg"
    )

    Surface(
        onClick = onClick,
        shape = RiteAppTheme.shapes.sm,
        color = animatedBackground,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .height(IntrinsicSize.Min)
                    .padding(
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
                modifier = Modifier
                    .width(5.dp)
                    .heightIn(min = 48.dp),
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
    val motion = RiteAppTheme.motion
    val targetNameColor: Color =
        when (state) {
            HabitCardState.Completed -> colors.primary
            HabitCardState.Failed -> colors.error
            HabitCardState.Skipped -> colors.onSurfaceMuted
            HabitCardState.Suspended -> colors.suspend
            else -> colors.onSurface
        }
    val animatedNameColor by animateColorAsState(
        targetValue = targetNameColor,
        animationSpec = tween(
            durationMillis = motion.deliberate.inWholeMilliseconds.toInt(),
            easing = motion.easeQuiet
        ),
        label = "habit-card-name-color"
    )
    val strikethroughProgress by animateFloatAsState(
        targetValue = if (state == HabitCardState.Completed) 1f else 0f,
        animationSpec = tween(
            durationMillis = motion.standard.inWholeMilliseconds.toInt(),
            delayMillis = motion.deliberate.inWholeMilliseconds.toInt(),
            easing = motion.easeQuiet
        ),
        label = "habit-card-strikethrough"
    )
    val nameStyle =
        RiteAppTheme.typography.titleLarge.copy(
            fontStyle = if (state ==
                HabitCardState.Suspended
            ) {
                FontStyle.Italic
            } else {
                FontStyle.Normal
            }
        )
    val strikeColor = animatedNameColor.copy(alpha = 0.5f)
    var nameLayout: TextLayoutResult? by remember { mutableStateOf(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(BODY_COLUMN_GAP),
    ) {
        HabitCardKickerRow(
            state = state,
            kicker = todayHabitKicker(habit, state),
        )
        Text(
            text = habit.name,
            style = nameStyle,
            color = animatedNameColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { nameLayout = it },
            modifier = Modifier.drawWithContent {
                drawContent()
                if (strikethroughProgress > 0f) {
                    val layout = nameLayout
                    val y = if (layout != null) {
                        // Center on the x-height of lowercase letters (~24% of font
                        // size above the baseline for Fraunces), so the line doesn't
                        // ride high over the body of the word.
                        layout.firstBaseline - nameStyle.fontSize.toPx() * 0.24f
                    } else {
                        size.height / 2f
                    }
                    drawLine(
                        color = strikeColor,
                        start = Offset(0f, y),
                        end = Offset(size.width * strikethroughProgress, y),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
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
