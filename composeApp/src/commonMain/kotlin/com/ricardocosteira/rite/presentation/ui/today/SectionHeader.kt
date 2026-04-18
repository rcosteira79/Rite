package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun SectionHeader(title: String, trailingLabel: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = RiteAppTheme.typography.eyebrow,
            color = RiteAppTheme.colors.onSurfaceMuted
        )
        Text(
            text = trailingLabel.uppercase(),
            style = RiteAppTheme.typography.mono,
            color = RiteAppTheme.colors.onSurfaceSubtle
        )
    }
}
