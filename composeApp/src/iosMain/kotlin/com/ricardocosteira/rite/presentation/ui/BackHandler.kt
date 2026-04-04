package com.ricardocosteira.rite.presentation.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS handles back navigation via swipe gestures; no-op here.
}
