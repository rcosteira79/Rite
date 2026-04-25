package com.ricardocosteira.rite.presentation.ui.today.components.habitcard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun HabitCardKickerRow(state: HabitCardState, kicker: String, modifier: Modifier = Modifier) {
    Text(
        text = kicker.uppercase(),
        style = RiteAppTheme.typography.eyebrow,
        color = state.kickerColor(),
        modifier = modifier.fillMaxWidth()
    )
}
