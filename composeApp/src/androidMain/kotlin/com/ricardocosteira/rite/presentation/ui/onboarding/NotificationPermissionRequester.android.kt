package com.ricardocosteira.rite.presentation.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return NotificationPermissionState(
            shouldShow = false,
            requestPermission = { onResult -> onResult(true) }
        )
    }

    val callbackRef = remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        callbackRef.value?.invoke(isGranted)
        callbackRef.value = null
    }

    return remember {
        NotificationPermissionState(
            shouldShow = true,
            requestPermission = { onResult ->
                callbackRef.value = onResult
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        )
    }
}
