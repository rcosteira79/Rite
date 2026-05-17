package com.ricardocosteira.rite

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.di.RiteAppComponent
import com.ricardocosteira.rite.di.create
import com.ricardocosteira.rite.notifications.HabitNotification
import com.ricardocosteira.rite.time.JvmAppForegroundObserver
import java.util.UUID

fun main() = application {
    val driverFactory = DatabaseDriverFactory()
    val habitNotification = HabitNotification()
    val appForegroundObserver = JvmAppForegroundObserver()
    val appComponent = RiteAppComponent::class.create(
        driverFactory,
        habitNotification,
        appForegroundObserver
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "Rite",
    ) {
        App(appComponent = appComponent)
    }
}

actual fun generateUuid(): String = UUID.randomUUID().toString()
