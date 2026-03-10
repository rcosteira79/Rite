package com.ricardocosteira.habitlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ricardocosteira.habitlock.presentation.ui.startup.StartupState
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val appComponent = application.habitLockApplication.appComponent

        installSplashScreen().setKeepOnScreenCondition {
            appComponent.startupViewModel.state.value is StartupState.Loading
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(appComponent = appComponent)
        }
    }
}

actual fun generateUuid(): String = UUID.randomUUID().toString()
