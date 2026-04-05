package com.ricardocosteira.rite.presentation.ui.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.ricardocosteira.rite.findActivity

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return NotificationPermissionState(
            shouldShow = false,
            isGranted = true,
            requestPermission = { onResult -> onResult(true) }
        )
    }

    val context = LocalContext.current
    val callbackRef = remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    val hasRequestedOnce = remember { mutableStateOf(false) }
    val isGranted = remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    LifecycleResumeEffect(Unit) {
        isGranted.value = NotificationManagerCompat.from(context).areNotificationsEnabled()
        onPauseOrDispose {}
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        hasRequestedOnce.value = true
        isGranted.value = granted
        callbackRef.value?.invoke(granted)
        callbackRef.value = null
    }

    return NotificationPermissionState(
        shouldShow = true,
        isGranted = isGranted.value,
        requestPermission = { onResult ->
            if (isGranted.value) {
                onResult(true)
                return@NotificationPermissionState
            }

            val activity = context.findActivity()
            val canShowDialog: Boolean = activity == null ||
                !hasRequestedOnce.value ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )

            if (canShowDialog) {
                callbackRef.value = onResult
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Permanently denied — open system settings instead
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
        }
    )
}
