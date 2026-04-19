package com.ricardocosteira.rite.presentation.ui.today.components.habitcard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun HabitCardKickerRow(state: HabitCardState, kicker: String, modifier: Modifier = Modifier,) {
    val colors = RiteAppTheme.colors
    val kickerColor: Color = when (state) {
        HabitCardState.Completed -> colors.primary
        HabitCardState.Failed -> colors.error
        HabitCardState.Skipped -> colors.onSurfaceSubtle
        HabitCardState.Suspended -> colors.suspend
        else -> colors.onSurfaceMuted
    }

    Text(
        text = kicker.uppercase(),
        style = RiteAppTheme.typography.eyebrow,
        color = kickerColor,
        modifier = modifier.fillMaxWidth()
    )
}
