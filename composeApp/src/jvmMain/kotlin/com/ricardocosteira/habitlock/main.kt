package com.ricardocosteira.habitlock

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.di.create
import com.ricardocosteira.habitlock.notifications.HabitNotification
import java.util.UUID

fun main() = application {
    val driverFactory = DatabaseDriverFactory()
    val habitNotification = HabitNotification()
    val appComponent = HabitLockAppComponent::class.create(driverFactory, habitNotification)
    Window(
        onCloseRequest = ::exitApplication,
        title = "HabitLock",
    ) {
        App(appComponent = appComponent)
    }
}

actual fun generateUuid(): String = UUID.randomUUID().toString()
