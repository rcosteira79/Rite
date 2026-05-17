package com.ricardocosteira.rite

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.di.RiteAppComponent
import com.ricardocosteira.rite.di.create
import com.ricardocosteira.rite.notifications.HabitNotification
import com.ricardocosteira.rite.time.IosAppForegroundObserver
import platform.Foundation.NSUUID

fun MainViewController() = ComposeUIViewController {
    val appComponent =
        remember {
            RiteAppComponent::class.create(
                DatabaseDriverFactory(),
                HabitNotification(),
                IosAppForegroundObserver()
            )
        }
    App(appComponent = appComponent)
}

actual fun generateUuid(): String = NSUUID().UUIDString()
