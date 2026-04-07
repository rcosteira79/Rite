package com.ricardocosteira.rite.presentation.ui.settings

import androidx.compose.runtime.Composable

data class NotificationSettingsState(val isEnabled: Boolean, val openSettings: () -> Unit)

/**
 * Returns the current notification enabled state and a lambda to open system notification settings.
 * On Android, queries NotificationManager and refreshes on lifecycle resume.
 * On other platforms, always returns enabled with a no-op settings launcher.
 */
@Composable
expect fun rememberNotificationSettingsState(): NotificationSettingsState
