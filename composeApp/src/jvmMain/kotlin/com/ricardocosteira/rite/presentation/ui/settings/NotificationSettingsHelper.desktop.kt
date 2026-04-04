package com.ricardocosteira.rite.presentation.ui.settings

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationSettingsState(): NotificationSettingsState =
    NotificationSettingsState(
        isEnabled = true,
        openSettings = {}
    )
