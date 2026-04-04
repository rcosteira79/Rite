package com.ricardocosteira.rite.presentation.ui.onboarding

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState =
    NotificationPermissionState(
        shouldShow = false,
        requestPermission = { onResult -> onResult(true) }
    )
