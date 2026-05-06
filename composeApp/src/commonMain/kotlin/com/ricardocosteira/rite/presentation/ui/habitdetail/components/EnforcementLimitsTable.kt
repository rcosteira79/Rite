package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.UndoPolicy
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import com.ricardocosteira.rite.presentation.ui.theme.RiteColorScheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_detail_enf_consecutive_of_max
import rite.composeapp.generated.resources.habit_detail_enf_consecutive_unlimited
import rite.composeapp.generated.resources.habit_detail_enf_row_consecutive
import rite.composeapp.generated.resources.habit_detail_enf_row_skips
import rite.composeapp.generated.resources.habit_detail_enf_row_snoozes
import rite.composeapp.generated.resources.habit_detail_enf_row_strictness
import rite.composeapp.generated.resources.habit_detail_enf_row_undo
import rite.composeapp.generated.resources.habit_detail_enf_skips_this_week
import rite.composeapp.generated.resources.habit_detail_enf_snoozes_unlimited
import rite.composeapp.generated.resources.habit_detail_enf_snoozes_used
import rite.composeapp.generated.resources.habit_detail_enf_strictness_balanced
import rite.composeapp.generated.resources.habit_detail_enf_strictness_custom
import rite.composeapp.generated.resources.habit_detail_enf_strictness_flexible
import rite.composeapp.generated.resources.habit_detail_enf_strictness_unwavering
import rite.composeapp.generated.resources.habit_detail_enf_undo_all_history
import rite.composeapp.generated.resources.habit_detail_enf_undo_disabled
import rite.composeapp.generated.resources.habit_detail_enf_undo_today_only

/**
 * A bordered table that summarises the enforcement limits for a single habit.
 *
 * Displays five rows — Strictness, Undo, Snoozes, Skips, and Consecutive — each showing
 * a label on the left and the current value on the right. The Consecutive row is rendered
 * in the theme's [RiteColorScheme.suspend] colour when [currentConsecutiveSkips] has
 * reached [maxConsecutiveSkips], signalling that the habit is locked.
 *
 * @param strictnessPreset The active preset, or `null` if the user has custom settings.
 * @param undoPolicy How far back undo operations are permitted.
 * @param snoozesUsedToday Number of snoozes already consumed today.
 * @param maxSnoozesPerDay Daily snooze cap, or `null` for unlimited.
 * @param skipsThisWeek Number of skips recorded in the current ISO week.
 * @param currentConsecutiveSkips Current run of consecutive skipped days.
 * @param maxConsecutiveSkips Maximum consecutive skips allowed, or `null` for unlimited.
 * @param modifier Modifier applied to the outermost [Column].
 */
@Composable
fun EnforcementLimitsTable(
    strictnessPreset: StrictnessPreset?,
    undoPolicy: UndoPolicy,
    snoozesUsedToday: Int,
    maxSnoozesPerDay: Int?,
    skipsThisWeek: Int,
    currentConsecutiveSkips: Int,
    maxConsecutiveSkips: Int?,
    modifier: Modifier = Modifier
) {
    val colors = RiteAppTheme.colors
    val ruleColor: Color = colors.outline
    val shape = RiteAppTheme.shapes.sm
    val isLocked: Boolean =
        maxConsecutiveSkips != null && currentConsecutiveSkips >= maxConsecutiveSkips

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = ruleColor, shape = shape)
            .background(color = colors.surface, shape = shape)
    ) {
        EnforcementRow(
            label = stringResource(Res.string.habit_detail_enf_row_strictness),
            value = strictnessLabel(strictnessPreset)
        )
        EnforcementDivider(ruleColor)
        EnforcementRow(
            label = stringResource(Res.string.habit_detail_enf_row_undo),
            value = undoLabel(undoPolicy)
        )
        EnforcementDivider(ruleColor)
        EnforcementRow(
            label = stringResource(Res.string.habit_detail_enf_row_snoozes),
            value = snoozesLabel(snoozesUsedToday, maxSnoozesPerDay)
        )
        EnforcementDivider(ruleColor)
        EnforcementRow(
            label = stringResource(Res.string.habit_detail_enf_row_skips),
            value = stringResource(Res.string.habit_detail_enf_skips_this_week, skipsThisWeek)
        )
        EnforcementDivider(ruleColor)
        EnforcementRow(
            label = stringResource(Res.string.habit_detail_enf_row_consecutive),
            value = consecutiveLabel(currentConsecutiveSkips, maxConsecutiveSkips),
            valueColor = if (isLocked) colors.suspend else colors.onSurface
        )
    }
}

@Composable
private fun strictnessLabel(preset: StrictnessPreset?): String = when (preset) {
    StrictnessPreset.FLEXIBLE -> stringResource(Res.string.habit_detail_enf_strictness_flexible)
    StrictnessPreset.BALANCED -> stringResource(Res.string.habit_detail_enf_strictness_balanced)
    StrictnessPreset.UNWAVERING -> stringResource(Res.string.habit_detail_enf_strictness_unwavering)
    null -> stringResource(Res.string.habit_detail_enf_strictness_custom)
}

@Composable
private fun undoLabel(policy: UndoPolicy): String = when (policy) {
    UndoPolicy.ALL_HISTORY -> stringResource(Res.string.habit_detail_enf_undo_all_history)
    UndoPolicy.TODAY_ONLY -> stringResource(Res.string.habit_detail_enf_undo_today_only)
    UndoPolicy.NONE -> stringResource(Res.string.habit_detail_enf_undo_disabled)
}

@Composable
private fun snoozesLabel(used: Int, max: Int?): String = when (max) {
    null -> stringResource(Res.string.habit_detail_enf_snoozes_unlimited)
    else -> stringResource(Res.string.habit_detail_enf_snoozes_used, used, max)
}

@Composable
private fun consecutiveLabel(current: Int, max: Int?): String = when (max) {
    null -> stringResource(Res.string.habit_detail_enf_consecutive_unlimited)
    else -> stringResource(Res.string.habit_detail_enf_consecutive_of_max, current, max)
}

@Composable
private fun EnforcementRow(
    label: String,
    value: String,
    valueColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = RiteAppTheme.typography.bodySmall,
            color = RiteAppTheme.colors.onSurfaceMuted
        )
        Text(
            text = value,
            style = RiteAppTheme.typography.titleMedium.copy(fontSize = 14.sp),
            color = valueColor ?: RiteAppTheme.colors.onSurface
        )
    }
}

@Composable
private fun EnforcementDivider(color: Color) {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}
