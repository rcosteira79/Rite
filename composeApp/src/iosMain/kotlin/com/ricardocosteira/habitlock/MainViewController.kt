package com.ricardocosteira.habitlock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import platform.Foundation.NSUUID

fun MainViewController() = ComposeUIViewController { App() }

@Composable
actual fun rememberDatabaseDriverFactory(): DatabaseDriverFactory {
    return remember { DatabaseDriverFactory() }
}

actual fun generateUuid(): String = NSUUID().UUIDString()
