package com.ricardocosteira.rite.presentation.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
actual fun rememberNotificationSettingsState(): NotificationSettingsState {
    val context = LocalContext.current
    val isEnabled = remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    LifecycleResumeEffect(Unit) {
        isEnabled.value = NotificationManagerCompat.from(context).areNotificationsEnabled()
        onPauseOrDispose {}
    }

    return NotificationSettingsState(
        isEnabled = isEnabled.value,
        openSettings = {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        }
    )
}
