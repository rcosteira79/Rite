package com.ricardocosteira.habitlock

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.di.create
import platform.Foundation.NSUUID

fun MainViewController() = ComposeUIViewController {
    val appComponent = remember { HabitLockAppComponent::class.create(DatabaseDriverFactory()) }
    App(appComponent = appComponent)
}

actual fun generateUuid(): String = NSUUID().UUIDString()
