package com.ricardocosteira.habitlock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.presentation.navigation.HabitLockNavHost
import com.ricardocosteira.habitlock.presentation.ui.theme.HabitLockTheme
import kotlinx.datetime.TimeZone

@Composable
expect fun rememberDatabaseDriverFactory(): DatabaseDriverFactory

@Composable
fun App() {
    HabitLockTheme {
        val driverFactory = rememberDatabaseDriverFactory()

        var isInitialized by remember { mutableStateOf(false) }
        var isOnboardingCompleted by remember { mutableStateOf(false) }

        // Create DI component
        val appComponent = remember { HabitLockAppComponent.create(driverFactory) }

        // Get repositories and ViewModels from DI
        val userRepository = remember { appComponent.userRepository }
        val onboardingViewModel = remember { appComponent.createOnboardingViewModel() }
        val todayViewModel = remember { appComponent.createTodayViewModel() }
        val calendarViewModel = remember { appComponent.createCalendarViewModel() }
        val settingsViewModel = remember { appComponent.createSettingsViewModel() }
        val archivedHabitsViewModel = remember { appComponent.createArchivedHabitsViewModel() }
        val habitFormViewModelFactory = remember { appComponent.habitFormViewModelFactory }

        // Initialize user on first launch
        LaunchedEffect(Unit) {
            val existingUser = userRepository.getUser()
            if (existingUser == null) {
                userRepository.createDefaultUser(TimeZone.currentSystemDefault())
                isOnboardingCompleted = false
            } else {
                isOnboardingCompleted = existingUser.isOnboardingCompleted
            }
            isInitialized = true
        }

        if (!isInitialized) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            HabitLockNavHost(
                isOnboardingCompleted = isOnboardingCompleted,
                onboardingViewModel = onboardingViewModel,
                todayViewModel = todayViewModel,
                calendarViewModel = calendarViewModel,
                settingsViewModel = settingsViewModel,
                archivedHabitsViewModel = archivedHabitsViewModel,
                createHabitFormViewModel = habitFormViewModelFactory::create
            )
        }
    }
}

expect fun generateUuid(): String
