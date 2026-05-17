package com.ricardocosteira.rite

import android.app.Application
import android.content.Context
import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.di.RiteAppComponent
import com.ricardocosteira.rite.di.createAppComponent
import com.ricardocosteira.rite.notifications.HabitNotification
import com.ricardocosteira.rite.notifications.NotificationChannels
import com.ricardocosteira.rite.time.AndroidAppForegroundObserver
import com.ricardocosteira.rite.workers.WorkManagerInitializer

/**
 * Application class that holds the singleton [RiteAppComponent].
 *
 * Workers and BroadcastReceivers retrieve the shared component via the
 * [Context.riteApplication] extension, eliminating the per-invocation
 * AppModule pattern and ensuring a single SQLite connection per process.
 */
class RiteApplication : Application() {

    lateinit var appComponent: RiteAppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        val driverFactory = DatabaseDriverFactory(this)
        val habitNotification = HabitNotification(this)
        val appForegroundObserver = AndroidAppForegroundObserver()
        appComponent = createAppComponent(driverFactory, habitNotification, appForegroundObserver)
        NotificationChannels.createChannels(this)
        WorkManagerInitializer.initialize(this)
    }
}

val Context.riteApplication: RiteApplication
    get() = applicationContext as RiteApplication
