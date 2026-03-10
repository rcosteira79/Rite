package com.ricardocosteira.habitlock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.presentation.navigation.HabitLockNavHost
import com.ricardocosteira.habitlock.presentation.ui.startup.StartupState
import com.ricardocosteira.habitlock.presentation.ui.theme.HabitLockTheme

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
            is StartupState.Ready -> HabitLockNavHost(
                isOnboardingCompleted = currentState.isOnboardingCompleted,
                onboardingViewModel = appComponent.onboardingViewModel,
                todayViewModel = appComponent.todayViewModel,
                calendarViewModel = appComponent.calendarViewModel,
                settingsViewModel = appComponent.settingsViewModel,
                archivedHabitsViewModel = appComponent.archivedHabitsViewModel,
                createHabitFormViewModel = appComponent.habitFormViewModelFactory::create
            )
        }
    }
}

expect fun generateUuid(): String
