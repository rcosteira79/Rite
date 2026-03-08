package com.ricardocosteira.habitlock

import androidx.compose.ui.window.ComposeUIViewController
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.di.create
import platform.Foundation.NSUUID

fun MainViewController() = ComposeUIViewController {
    val driverFactory = DatabaseDriverFactory()
    val appComponent = HabitLockAppComponent::class.create(driverFactory)
    App(appComponent = { appComponent })
}

actual fun generateUuid(): String = NSUUID().UUIDString()
