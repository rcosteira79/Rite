package com.ricardocosteira.habitlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val appComponent = application.habitLockApplication.appComponent
        setContent {
            App(appComponent = { appComponent })
        }
    }
}

actual fun generateUuid(): String = UUID.randomUUID().toString()
