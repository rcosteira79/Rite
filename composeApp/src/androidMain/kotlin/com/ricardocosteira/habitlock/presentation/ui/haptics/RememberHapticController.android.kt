package com.ricardocosteira.habitlock.presentation.ui.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberHapticController(): HapticController {
    val context = LocalContext.current
    return remember(context) { HapticController(context) }
}
