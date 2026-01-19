package com.ricardocosteira.habitlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.di.create
import com.ricardocosteira.habitlock.notifications.NotificationChannels
import com.ricardocosteira.habitlock.workers.WorkManagerInitializer
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize notification channels
        NotificationChannels.createChannels(applicationContext)

        // Initialize WorkManager for background tasks
        WorkManagerInitializer.initialize(applicationContext)

        val driverFactory = DatabaseDriverFactory(applicationContext)

        val appComponent = HabitLockAppComponent::class.create(driverFactory)
        setContent {
            App(appComponent = { appComponent })
        }
    }
}

actual fun generateUuid(): String = UUID.randomUUID().toString()
