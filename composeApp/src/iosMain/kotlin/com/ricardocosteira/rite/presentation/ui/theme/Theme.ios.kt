package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun HabitLockTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    HabitLockThemeFallback(darkTheme = darkTheme, content = content)
}

