package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun RiteTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    RiteThemeFallback(darkTheme = darkTheme, content = content)
}

