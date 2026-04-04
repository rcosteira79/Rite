package com.ricardocosteira.rite

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ricardocosteira.rite.presentation.ui.startup.StartupState
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        // No action needed — form UI checks permission dynamically
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val appComponent = application.habitLockApplication.appComponent

        installSplashScreen().setKeepOnScreenCondition {
            appComponent.startupViewModel.state.value is StartupState.Loading
        }

        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        super.onCreate(savedInstanceState)

        setContent {
            App(appComponent = appComponent)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission: String = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

actual fun generateUuid(): String = UUID.randomUUID().toString()
