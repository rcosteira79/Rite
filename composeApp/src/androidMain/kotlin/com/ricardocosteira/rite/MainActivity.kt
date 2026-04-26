package com.ricardocosteira.rite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ricardocosteira.rite.presentation.ui.startup.StartupState
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val appComponent = application.riteApplication.appComponent

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            appComponent.startupViewModel.state.value is StartupState.Loading
        }
        // Skip the system's default exit animation (icon zoom + fade) — the Compose
        // splash continues the seal seamlessly, so the system animation reads as a
        // flicker against the static Compose seal underneath.
        splashScreen.setOnExitAnimationListener { it.remove() }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(appComponent = appComponent)
        }
    }
}

actual fun generateUuid(): String = UUID.randomUUID().toString()
