package com.ricardocosteira.habitlock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import platform.Foundation.NSUUID

fun MainViewController() {
//    val driverFactory = DatabaseDriverFactory()
//
//    val appComponent = HabitLockAppComponent::class.create(driverFactory)
//    ComposeUIViewController { App(appComponent = { appComponent }) }
}

actual fun generateUuid(): String = NSUUID().UUIDString()
