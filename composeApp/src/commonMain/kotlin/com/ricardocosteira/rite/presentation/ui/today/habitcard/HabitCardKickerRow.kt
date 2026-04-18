package com.ricardocosteira.rite.presentation.ui.today.habitcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun HabitCardKickerRow(
    state: HabitCardState,
    kicker: String,
    streakDays: Int?,
    modifier: Modifier = Modifier,
) {
    val colors = RiteAppTheme.colors
    val kickerColor: Color =
        when (state) {
            HabitCardState.Completed -> colors.primary
            HabitCardState.Failed -> colors.error
            HabitCardState.Skipped -> colors.onSurfaceSubtle
            HabitCardState.Suspended -> colors.suspend
            else -> colors.onSurfaceMuted
        }
    val streakVisible: Boolean =
        streakDays != null &&
            state != HabitCardState.Skipped &&
            state != HabitCardState.Suspended

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = kicker.uppercase(),
            style = RiteAppTheme.typography.eyebrow,
            color = kickerColor,
        )
        if (streakVisible) {
            Text(
                text = "$streakDays DAY STREAK",
                style = RiteAppTheme.typography.eyebrow,
                color = colors.onSurfaceSubtle,
            )
        }
    }
}
