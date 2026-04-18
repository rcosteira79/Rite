package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun RiteDivider(modifier: Modifier = Modifier, strong: Boolean = false) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth().height(1.dp),
        thickness = 1.dp,
        color = if (strong) RiteAppTheme.colors.outline else RiteAppTheme.colors.outlineVariant
    )
}
