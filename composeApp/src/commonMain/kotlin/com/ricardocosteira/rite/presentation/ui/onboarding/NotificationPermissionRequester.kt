package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.runtime.Composable

/**
 * Returns whether the notification permission step should be shown on this platform/API level,
 * and a lambda to request the permission.
 *
 * On Android 13+ (API 33+), [shouldShow] is true and [requestPermission] launches the
 * system POST_NOTIFICATIONS permission dialog. On all other platforms and older Android versions,
 * [shouldShow] is false.
 */
data class NotificationPermissionState(
    val shouldShow: Boolean,
    val isGranted: Boolean,
    val requestPermission: (onResult: (Boolean) -> Unit) -> Unit
)

@Composable
expect fun rememberNotificationPermissionState(): NotificationPermissionState
