package com.ricardocosteira.habitlock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import java.util.UUID

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "HabitLock",
    ) {
        App()
    }
}

@Composable
actual fun rememberDatabaseDriverFactory(): DatabaseDriverFactory {
    return remember { DatabaseDriverFactory() }
}

actual fun generateUuid(): String = UUID.randomUUID().toString()
