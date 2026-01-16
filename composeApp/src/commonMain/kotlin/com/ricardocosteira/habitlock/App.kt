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
import com.ricardocosteira.habitlock.data.database.HabitLockDatabase
import com.ricardocosteira.habitlock.data.repositories.HabitCompletionEventRepositoryImpl
import com.ricardocosteira.habitlock.data.repositories.HabitInstanceRepositoryImpl
import com.ricardocosteira.habitlock.data.repositories.HabitRepositoryImpl
import com.ricardocosteira.habitlock.data.repositories.SnoozeRepositoryImpl
import com.ricardocosteira.habitlock.data.repositories.UserRepositoryImpl
import com.ricardocosteira.habitlock.domain.usecases.ApplyStrictnessPresetUseCase
import com.ricardocosteira.habitlock.domain.usecases.CompleteHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.CreateHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabitsUseCase
import com.ricardocosteira.habitlock.domain.usecases.ProcessEndOfDayUseCase
import com.ricardocosteira.habitlock.domain.usecases.SkipHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.UndoHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.UuidProvider
import com.ricardocosteira.habitlock.presentation.navigation.HabitLockNavHost
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsViewModel
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarViewModel
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormViewModel
import com.ricardocosteira.habitlock.presentation.ui.onboarding.OnboardingViewModel
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsViewModel
import com.ricardocosteira.habitlock.presentation.ui.theme.HabitLockTheme
import com.ricardocosteira.habitlock.presentation.ui.today.TodayViewModel
import kotlinx.datetime.TimeZone

@Composable
expect fun rememberDatabaseDriverFactory(): DatabaseDriverFactory

@Composable
fun App() {
    HabitLockTheme {
        val driverFactory = rememberDatabaseDriverFactory()

        var isInitialized by remember { mutableStateOf(false) }
        var isOnboardingCompleted by remember { mutableStateOf(false) }

        // Create database and repositories
        val database = remember { HabitLockDatabase(driverFactory.createDriver()) }

        val userRepository = remember { UserRepositoryImpl(database) }
        val habitRepository = remember { HabitRepositoryImpl(database) }
        val habitInstanceRepository = remember { HabitInstanceRepositoryImpl(database) }
        val habitCompletionEventRepository = remember { HabitCompletionEventRepositoryImpl(database) }
        val snoozeRepository = remember { SnoozeRepositoryImpl(database) }

        // Create UUID provider
        val uuidProvider = remember {
            object : UuidProvider {
                override fun generate(): String = generateUuid()
            }
        }

        // Create use cases
        val generateDailyHabitsUseCase = remember {
            GenerateDailyHabitsUseCase(userRepository, habitRepository, habitInstanceRepository, uuidProvider)
        }
        val processEndOfDayUseCase = remember {
            ProcessEndOfDayUseCase(userRepository, habitInstanceRepository, habitRepository)
        }
        val completeHabitUseCase = remember {
            CompleteHabitUseCase(habitInstanceRepository, habitRepository, habitCompletionEventRepository)
        }
        val skipHabitUseCase = remember {
            SkipHabitUseCase(habitInstanceRepository, userRepository)
        }
        val undoHabitUseCase = remember {
            UndoHabitUseCase(habitInstanceRepository, habitCompletionEventRepository, habitRepository, userRepository)
        }
        val createHabitUseCase = remember {
            CreateHabitUseCase(habitRepository, uuidProvider)
        }
        val applyStrictnessPresetUseCase = remember {
            ApplyStrictnessPresetUseCase(userRepository)
        }

        // Create ViewModels
        val onboardingViewModel = remember {
            OnboardingViewModel(userRepository, applyStrictnessPresetUseCase, createHabitUseCase)
        }
        val todayViewModel = remember {
            TodayViewModel(
                userRepository, habitRepository, habitInstanceRepository,
                generateDailyHabitsUseCase, processEndOfDayUseCase,
                completeHabitUseCase, skipHabitUseCase, undoHabitUseCase
            )
        }
        val calendarViewModel = remember {
            CalendarViewModel(userRepository, habitInstanceRepository)
        }
        val settingsViewModel = remember {
            SettingsViewModel(userRepository)
        }
        val archivedHabitsViewModel = remember {
            ArchivedHabitsViewModel(habitRepository)
        }

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
                createHabitFormViewModel = {
                    HabitFormViewModel(habitRepository, createHabitUseCase)
                }
            )
        }
    }
}

expect fun generateUuid(): String
