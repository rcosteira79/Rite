package com.ricardocosteira.rite

import android.app.Application
import android.content.Context
import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.di.HabitLockAppComponent
import com.ricardocosteira.rite.di.createAppComponent
import com.ricardocosteira.rite.notifications.HabitNotification
import com.ricardocosteira.rite.notifications.NotificationChannels
import com.ricardocosteira.rite.workers.WorkManagerInitializer

/**
 * Application class that holds the singleton [HabitLockAppComponent].
 *
 * Workers and BroadcastReceivers retrieve the shared component via the
 * [Context.habitLockApplication] extension, eliminating the per-invocation
 * AppModule pattern and ensuring a single SQLite connection per process.
 */
class HabitLockApplication : Application() {

    lateinit var appComponent: HabitLockAppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        val driverFactory = DatabaseDriverFactory(this)
        val habitNotification = HabitNotification(this)
        appComponent = createAppComponent(driverFactory, habitNotification)
        NotificationChannels.createChannels(this)
        WorkManagerInitializer.initialize(this)
    }
}

val Context.habitLockApplication: HabitLockApplication
    get() = applicationContext as HabitLockApplication
