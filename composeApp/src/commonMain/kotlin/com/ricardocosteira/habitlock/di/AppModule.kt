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
import com.ricardocosteira.habitlock.domain.usecases.ApplyStrictnessPresetUseCase
import com.ricardocosteira.habitlock.domain.usecases.CalculateHabitScoreUseCase
import com.ricardocosteira.habitlock.domain.usecases.CompleteHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.CreateHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabitsUseCase
import com.ricardocosteira.habitlock.domain.usecases.GetWeeklyInstancesUseCase
import com.ricardocosteira.habitlock.domain.usecases.ProcessEndOfDayUseCase
import com.ricardocosteira.habitlock.domain.usecases.SkipHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.SuspendHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.UndoHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.UnsuspendHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.UuidProvider
import com.ricardocosteira.habitlock.generateUuid
import com.ricardocosteira.habitlock.presentation.ui.archived.ArchivedHabitsViewModel
import com.ricardocosteira.habitlock.presentation.ui.calendar.CalendarViewModel
import com.ricardocosteira.habitlock.presentation.ui.habit.HabitFormViewModel
import com.ricardocosteira.habitlock.presentation.ui.onboarding.OnboardingViewModel
import com.ricardocosteira.habitlock.presentation.ui.settings.SettingsViewModel
import com.ricardocosteira.habitlock.presentation.ui.today.TodayViewModel

/**
 * Application module that provides dependencies for the entire app.
 * Uses lazy initialization to create singletons only when needed.
 */
class AppModule(
    private val databaseDriverFactory: DatabaseDriverFactory
) {

    // Database (singleton)
    private val database: HabitLockDatabase by lazy {
        HabitLockDatabase(databaseDriverFactory.createDriver())
    }

    // Repositories (singletons)
    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database)
    }

    private val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(database)
    }

    private val habitInstanceRepository: HabitInstanceRepository by lazy {
        HabitInstanceRepositoryImpl(database)
    }

    private val habitCompletionEventRepository: HabitCompletionEventRepository by lazy {
        HabitCompletionEventRepositoryImpl(database)
    }

    private val snoozeRepository: SnoozeRepository by lazy {
        SnoozeRepositoryImpl(database)
    }

    private val leavePeriodRepository: LeavePeriodRepository by lazy {
        LeavePeriodRepositoryImpl(database)
    }

    // UUID Provider (singleton)
    private val uuidProvider: UuidProvider by lazy {
        object : UuidProvider {
            override fun generate(): String = generateUuid()
        }
    }

    // Use Cases (singletons)
    private val generateDailyHabitsUseCase: GenerateDailyHabitsUseCase by lazy {
        GenerateDailyHabitsUseCase(
            userRepository,
            habitRepository,
            habitInstanceRepository,
            leavePeriodRepository,
            uuidProvider
        )
    }

    private val getWeeklyInstancesUseCase: GetWeeklyInstancesUseCase by lazy {
        GetWeeklyInstancesUseCase(
            userRepository,
            habitRepository,
            habitInstanceRepository
        )
    }

    private val processEndOfDayUseCase: ProcessEndOfDayUseCase by lazy {
        ProcessEndOfDayUseCase(userRepository, habitInstanceRepository, habitRepository)
    }

    private val completeHabitUseCase: CompleteHabitUseCase by lazy {
        CompleteHabitUseCase(habitInstanceRepository, habitRepository, habitCompletionEventRepository)
    }

    private val skipHabitUseCase: SkipHabitUseCase by lazy {
        SkipHabitUseCase(habitInstanceRepository, userRepository)
    }

    private val undoHabitUseCase: UndoHabitUseCase by lazy {
        UndoHabitUseCase(
            habitInstanceRepository,
            habitCompletionEventRepository,
            habitRepository,
            userRepository
        )
    }

    private val createHabitUseCase: CreateHabitUseCase by lazy {
        CreateHabitUseCase(habitRepository, uuidProvider)
    }

    private val applyStrictnessPresetUseCase: ApplyStrictnessPresetUseCase by lazy {
        ApplyStrictnessPresetUseCase(userRepository)
    }

    private val calculateHabitScoreUseCase: CalculateHabitScoreUseCase by lazy {
        CalculateHabitScoreUseCase(habitRepository)
    }

    private val suspendHabitUseCase: SuspendHabitUseCase by lazy {
        SuspendHabitUseCase(habitRepository, leavePeriodRepository, uuidProvider)
    }

    private val unsuspendHabitUseCase: UnsuspendHabitUseCase by lazy {
        UnsuspendHabitUseCase(leavePeriodRepository, userRepository)
    }

    // Public provider methods
    fun provideUserRepository(): UserRepository = userRepository
    
    fun provideCalculateHabitScoreUseCase(): CalculateHabitScoreUseCase = calculateHabitScoreUseCase
    
    fun provideSuspendHabitUseCase(): SuspendHabitUseCase = suspendHabitUseCase
    
    fun provideUnsuspendHabitUseCase(): UnsuspendHabitUseCase = unsuspendHabitUseCase
    
    fun provideLeavePeriodRepository(): LeavePeriodRepository = leavePeriodRepository

    fun provideOnboardingViewModel(): OnboardingViewModel {
        return OnboardingViewModel(
            userRepository,
            applyStrictnessPresetUseCase,
            createHabitUseCase,
            generateDailyHabitsUseCase
        )
    }

    fun provideTodayViewModel(): TodayViewModel {
        return TodayViewModel(
            userRepository,
            habitRepository,
            habitInstanceRepository,
            generateDailyHabitsUseCase,
            processEndOfDayUseCase,
            completeHabitUseCase,
            skipHabitUseCase,
            undoHabitUseCase
        )
    }

    fun provideCalendarViewModel(): CalendarViewModel {
        return CalendarViewModel(userRepository, habitInstanceRepository)
    }

    fun provideSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(userRepository)
    }

    fun provideArchivedHabitsViewModel(): ArchivedHabitsViewModel {
        return ArchivedHabitsViewModel(habitRepository)
    }

    fun provideHabitFormViewModelFactory(): HabitFormViewModel.Factory {
        return object : HabitFormViewModel.Factory {
            override fun create(habitIdToEdit: String?): HabitFormViewModel {
                return HabitFormViewModel(habitRepository, createHabitUseCase, habitIdToEdit)
            }
        }
    }
}
