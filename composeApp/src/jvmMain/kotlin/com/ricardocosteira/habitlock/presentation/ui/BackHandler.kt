package com.ricardocosteira.habitlock.presentation.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop has no hardware back button; no-op here.
}
