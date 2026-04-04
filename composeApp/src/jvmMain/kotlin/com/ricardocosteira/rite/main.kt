package com.ricardocosteira.rite

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.di.HabitLockAppComponent
import com.ricardocosteira.rite.di.create
import com.ricardocosteira.rite.notifications.HabitNotification
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
