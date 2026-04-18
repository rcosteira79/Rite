package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun RitePill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    bordered: Boolean = false
) {
    val colors = RiteAppTheme.colors
    val bg = if (containerColor.isUnspecified()) colors.surfaceContainer else containerColor
    val fg = if (contentColor.isUnspecified()) colors.onSurface else contentColor

    Surface(
        modifier = modifier,
        shape = RiteAppTheme.shapes.pill,
        color = bg,
        contentColor = fg,
        border = if (bordered) BorderStroke(1.dp, colors.outline) else null
    ) {
        Text(
            text = text,
            style = RiteAppTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun Color.isUnspecified(): Boolean = this == Color.Unspecified
