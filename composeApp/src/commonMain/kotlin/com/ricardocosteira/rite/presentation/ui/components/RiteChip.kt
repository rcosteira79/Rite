package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun RiteLetterChip(
    letter: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RiteAppTheme.colors
    Surface(
        modifier = modifier.size(32.dp),
        shape = RiteAppTheme.shapes.sm,
        color = if (selected) colors.onSurface else colors.surfaceContainer,
        contentColor = if (selected) colors.surface else colors.onSurface,
        border = BorderStroke(1.dp, colors.outline),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = letter, style = RiteAppTheme.typography.labelLarge)
        }
    }
}

@Composable
fun RiteShortcutChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RiteAppTheme.colors
    Surface(
        modifier = modifier,
        shape = RiteAppTheme.shapes.sm,
        color = if (selected) colors.onSurface else colors.surfaceContainer,
        contentColor = if (selected) colors.surface else colors.onSurface,
        border = BorderStroke(1.dp, colors.outline),
        onClick = onClick
    ) {
        Text(
            text = text,
            style = RiteAppTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
