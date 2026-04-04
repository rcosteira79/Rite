package com.ricardocosteira.rite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.rite.di.HabitLockAppComponent
import com.ricardocosteira.rite.di.LocalAppComponent
import com.ricardocosteira.rite.presentation.navigation.HabitLockNavigation
import com.ricardocosteira.rite.presentation.ui.startup.StartupState
import com.ricardocosteira.rite.presentation.ui.theme.HabitLockTheme

@Composable
fun App(appComponent: HabitLockAppComponent) {
    HabitLockTheme {
        val state by appComponent.startupViewModel.state.collectAsStateWithLifecycle()

        when (val currentState = state) {
            StartupState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            is StartupState.Ready -> CompositionLocalProvider(LocalAppComponent provides appComponent) {
                HabitLockNavigation(isOnboardingCompleted = currentState.isOnboardingCompleted)
            }
        }
    }
}

expect fun generateUuid(): String
