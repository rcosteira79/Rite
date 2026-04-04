package com.ricardocosteira.rite

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.di.HabitLockAppComponent
import com.ricardocosteira.rite.di.create
import com.ricardocosteira.rite.notifications.HabitNotification
import platform.Foundation.NSUUID

fun MainViewController() = ComposeUIViewController {
    val appComponent =
        remember {
            HabitLockAppComponent::class.create(DatabaseDriverFactory(), HabitNotification())
        }
    App(appComponent = appComponent)
}

actual fun generateUuid(): String = NSUUID().UUIDString()
