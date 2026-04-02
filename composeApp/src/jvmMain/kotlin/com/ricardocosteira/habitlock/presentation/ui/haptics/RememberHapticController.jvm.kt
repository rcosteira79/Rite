package com.ricardocosteira.habitlock.presentation.ui.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberHapticController(): HapticController = remember { HapticController() }
