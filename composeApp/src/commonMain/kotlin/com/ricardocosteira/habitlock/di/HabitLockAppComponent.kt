package com.ricardocosteira.habitlock.di

import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.data.database.HabitLockDatabase
import com.ricardocosteira.habitlock.data.repositories.HabitCompletionEventRepositoryImpl
import com.ricardocosteira.habitlock.data.repositories.HabitInstanceRepositoryImpl
import com.ricardocosteira.habitlock.data.repositories.HabitRepositoryImpl
import com.ricardocosteira.habitlock.data.repositories.LeavePeriodRepositoryImpl
import com.ricardocosteira.habitlock.data.repositories.SnoozeRepositoryImpl
import com.ricardocosteira.habitlock.data.repositories.UserRepositoryImpl
import com.ricardocosteira.habitlock.domain.repositories.HabitCompletionEventRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.domain.repositories.LeavePeriodRepository
import com.ricardocosteira.habitlock.domain.repositories.SnoozeRepository
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.domain.usecases.CompleteHabit
import com.ricardocosteira.habitlock.domain.usecases.CreateHabit
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.habitlock.domain.usecases.ProcessEndOfDay
import com.ricardocosteira.habitlock.domain.usecases.SkipHabit
import com.ricardocosteira.habitlock.domain.usecases.SnoozeHabit
import com.ricardocosteira.habitlock.domain.usecases.UuidProvider
import com.ricardocosteira.habitlock.generateUuid
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsViewModel
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarViewModel
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormViewModel
import com.ricardocosteira.habitlock.presentation.ui.onboarding.OnboardingViewModel
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsViewModel
import com.ricardocosteira.habitlock.presentation.ui.today.TodayViewModel
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

/**
 * Main application component for dependency injection using kotlin-inject.
 * 
 * This component is generated at compile-time by KSP and provides all dependencies
 * needed by the application. Dependencies marked with @Inject are automatically wired.
 */
@Component
@AppScope
abstract class HabitLockAppComponent(
    @get:Provides val databaseDriverFactory: DatabaseDriverFactory
) {

    // Database (singleton)
    @AppScope
    @Provides
    fun provideDatabase(driverFactory: DatabaseDriverFactory): HabitLockDatabase {
        return HabitLockDatabase(driverFactory.createDriver())
    }

    // UUID Provider (singleton)
    @AppScope
    @Provides
    fun provideUuidProvider(): UuidProvider {
        return object : UuidProvider {
            override fun generate(): String = generateUuid()
        }
    }

    // Repository bindings (interface -> implementation)
    @AppScope
    @Provides
    fun provideUserRepository(impl: UserRepositoryImpl): UserRepository = impl

    @AppScope
    @Provides
    fun provideHabitRepository(impl: HabitRepositoryImpl): HabitRepository = impl

    @AppScope
    @Provides
    fun provideHabitInstanceRepository(impl: HabitInstanceRepositoryImpl): HabitInstanceRepository = impl

    @AppScope
    @Provides
    fun provideHabitCompletionEventRepository(impl: HabitCompletionEventRepositoryImpl): HabitCompletionEventRepository = impl

    @AppScope
    @Provides
    fun provideLeavePeriodRepository(impl: LeavePeriodRepositoryImpl): LeavePeriodRepository = impl

    @AppScope
    @Provides
    fun provideSnoozeRepository(impl: SnoozeRepositoryImpl): SnoozeRepository = impl

    // Factory for HabitFormViewModel (needs habitIdToEdit parameter)
    @AppScope
    @Provides
    fun provideHabitFormViewModelFactory(
        habitRepository: HabitRepository,
        createHabit: CreateHabit
    ): HabitFormViewModel.Factory {
        return object : HabitFormViewModel.Factory {
            override fun create(habitIdToEdit: String?): HabitFormViewModel {
                return HabitFormViewModel(habitRepository, createHabit, habitIdToEdit)
            }
        }
    }

    // Public accessors for App initialization
    abstract val userRepository: UserRepository
    abstract val habitRepository: HabitRepository
    abstract val habitInstanceRepository: HabitInstanceRepository
    abstract val onboardingViewModel: OnboardingViewModel
    abstract val todayViewModel: TodayViewModel
    abstract val calendarViewModel: CalendarViewModel
    abstract val settingsViewModel: SettingsViewModel
    abstract val archivedHabitsViewModel: ArchivedHabitsViewModel
    abstract val habitFormViewModelFactory: HabitFormViewModel.Factory

    // Accessors for workers and receivers
    abstract val generateDailyHabits: GenerateDailyHabits
    abstract val processEndOfDay: ProcessEndOfDay
    abstract val completeHabit: CompleteHabit
    abstract val snoozeHabit: SnoozeHabit
    abstract val skipHabit: SkipHabit

    companion object
}
