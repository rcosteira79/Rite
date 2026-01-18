package com.ricardocosteira.habitlock.di

import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsViewModel
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarViewModel
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormViewModel
import com.ricardocosteira.habitlock.presentation.ui.onboarding.OnboardingViewModel
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsViewModel
import com.ricardocosteira.habitlock.presentation.ui.today.TodayViewModel

/**
 * Main application component for dependency injection.
 * Provides a clean interface for accessing all dependencies with lazy initialization.
 */
class HabitLockAppComponent private constructor(
    driverFactory: DatabaseDriverFactory
) {

    private val appModule = AppModule(driverFactory)

    // Repositories (exposed for app initialization)
    val userRepository: UserRepository by lazy { appModule.provideUserRepository() }

    // ViewModels (created on demand)
    fun createOnboardingViewModel(): OnboardingViewModel = appModule.provideOnboardingViewModel()

    fun createTodayViewModel(): TodayViewModel = appModule.provideTodayViewModel()

    fun createCalendarViewModel(): CalendarViewModel = appModule.provideCalendarViewModel()

    fun createSettingsViewModel(): SettingsViewModel = appModule.provideSettingsViewModel()

    fun createArchivedHabitsViewModel(): ArchivedHabitsViewModel = appModule.provideArchivedHabitsViewModel()

    // Factory for creating habit form viewmodels
    val habitFormViewModelFactory: HabitFormViewModel.Factory by lazy { appModule.provideHabitFormViewModelFactory() }

    companion object {
        /**
         * Creates the application component with the database driver factory.
         */
        fun create(driverFactory: DatabaseDriverFactory): HabitLockAppComponent {
            return HabitLockAppComponent(driverFactory)
        }
    }
}
