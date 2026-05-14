package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.habit_detail_stat_current_streak
import rite.composeapp.generated.resources.habit_detail_stat_days
import rite.composeapp.generated.resources.habit_detail_stat_habit_score
import rite.composeapp.generated.resources.habit_detail_stat_longest_streak

/**
 * Bordered 3-column stat row displayed between the action area and the tapestry on the
 * Habit Detail screen.
 *
 * Each tile renders a mono eyebrow label, a Fraunces 30sp numeric value, and a mono unit
 * suffix. The row itself carries top + bottom `outline` rules; the left two tiles carry a
 * right `outline` edge acting as a column divider.
 *
 * @param currentStreak  Number of consecutive days the habit has been completed.
 * @param longestStreak  All-time longest consecutive-day streak.
 * @param habitScore     Cumulative habit score (0–100).
 * @param modifier       Optional modifier applied to the outer [Row].
 */
@Composable
fun StatTileRow(
    currentStreak: Int,
    longestStreak: Int,
    habitScore: Int,
    modifier: Modifier = Modifier
) {
    val ruleColor = RiteAppTheme.colors.outline
    val ruleThicknessPx: Float = with(LocalDensity.current) { 1.dp.toPx() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = ruleColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, ruleThicknessPx)
                )
                drawRect(
                    color = ruleColor,
                    topLeft = Offset(0f, size.height - ruleThicknessPx),
                    size = Size(size.width, ruleThicknessPx)
                )
            }
    ) {
        StatTile(
            label = stringResource(Res.string.habit_detail_stat_current_streak),
            value = currentStreak.toString(),
            unit = stringResource(Res.string.habit_detail_stat_days),
            modifier = Modifier
                .weight(1f)
                .drawBehind {
                    drawRect(
                        color = ruleColor,
                        topLeft = Offset(size.width - ruleThicknessPx, 0f),
                        size = Size(ruleThicknessPx, size.height)
                    )
                }
        )
        StatTile(
            label = stringResource(Res.string.habit_detail_stat_longest_streak),
            value = longestStreak.toString(),
            unit = stringResource(Res.string.habit_detail_stat_days),
            modifier = Modifier
                .weight(1f)
                .drawBehind {
                    drawRect(
                        color = ruleColor,
                        topLeft = Offset(size.width - ruleThicknessPx, 0f),
                        size = Size(ruleThicknessPx, size.height)
                    )
                }
        )
        StatTile(
            label = stringResource(Res.string.habit_detail_stat_habit_score),
            value = habitScore.toString(),
            unit = "/100",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
        Text(
            text = label.uppercase(),
            style = RiteAppTheme.typography.eyebrow,
            color = RiteAppTheme.colors.onSurfaceMuted
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                append(value)
                withStyle(
                    SpanStyle(
                        fontFamily = RiteAppTheme.typography.mono.fontFamily,
                        fontSize = 11.sp,
                        color = RiteAppTheme.colors.onSurfaceMuted
                    )
                ) {
                    append(" ")
                    append(unit.uppercase())
                }
            },
            style = RiteAppTheme.typography.displaySmall.copy(
                fontSize = 30.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.6).sp
            ),
            color = RiteAppTheme.colors.onSurface
        )
    }
}
