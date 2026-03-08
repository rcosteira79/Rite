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
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.presentation.navigation.HabitLockNavHost
import com.ricardocosteira.habitlock.presentation.ui.theme.HabitLockTheme
import kotlinx.datetime.TimeZone

@Composable
fun App(appComponent: () -> HabitLockAppComponent) {
    HabitLockTheme {
        var isInitialized by remember { mutableStateOf(false) }
        var isOnboardingCompleted by remember { mutableStateOf(false) }


        // Resolve the DI component once so we don't invoke the factory repeatedly
        val component = remember { appComponent() }

        // Get repositories and ViewModels from DI
        val userRepository = remember { component.userRepository }
        val onboardingViewModel = remember { component.onboardingViewModel }
        val todayViewModel = remember { component.todayViewModel }
        val calendarViewModel = remember { component.calendarViewModel }
        val settingsViewModel = remember { component.settingsViewModel }
        val archivedHabitsViewModel = remember { component.archivedHabitsViewModel }
        val habitFormViewModelFactory = remember { component.habitFormViewModelFactory }

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
