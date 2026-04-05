package com.ricardocosteira.rite.di

import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.data.repositories.HabitCompletionEventRepositoryImpl
import com.ricardocosteira.rite.data.repositories.HabitInstanceRepositoryImpl
import com.ricardocosteira.rite.data.repositories.HabitRepositoryImpl
import com.ricardocosteira.rite.data.repositories.LeavePeriodRepositoryImpl
import com.ricardocosteira.rite.data.repositories.SnoozeRepositoryImpl
import com.ricardocosteira.rite.data.repositories.UserRepositoryImpl
import com.ricardocosteira.rite.domain.repositories.HabitCompletionEventRepository
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.LeavePeriodRepository
import com.ricardocosteira.rite.domain.repositories.SnoozeRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import com.ricardocosteira.rite.domain.usecases.CompleteHabit
import com.ricardocosteira.rite.domain.usecases.CreateHabit
import com.ricardocosteira.rite.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.rite.domain.usecases.ProcessEndOfDay
import com.ricardocosteira.rite.domain.usecases.SkipHabit
import com.ricardocosteira.rite.domain.usecases.SnoozeHabit
import com.ricardocosteira.rite.domain.usecases.UndoHabit
import com.ricardocosteira.rite.domain.usecases.UndoLastIncrement
import com.ricardocosteira.rite.domain.usecases.UuidProvider
import com.ricardocosteira.rite.generateUuid
import com.ricardocosteira.rite.notifications.HabitNotification
import com.ricardocosteira.rite.presentation.ui.archived.ArchivedHabitsViewModel
import com.ricardocosteira.rite.presentation.ui.calendar.CalendarViewModel
import com.ricardocosteira.rite.presentation.ui.habit.HabitFormViewModel
import com.ricardocosteira.rite.presentation.ui.habitdetail.HabitDetailViewModel
import com.ricardocosteira.rite.presentation.ui.onboarding.OnboardingViewModel
import com.ricardocosteira.rite.presentation.ui.settings.SettingsViewModel
import com.ricardocosteira.rite.presentation.ui.startup.StartupViewModel
import com.ricardocosteira.rite.presentation.ui.today.TodayViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
abstract class RiteAppComponent(
    @get:Provides val databaseDriverFactory: DatabaseDriverFactory,
    @get:Provides val habitNotification: HabitNotification
) {
    // IO Dispatcher (singleton)
    @AppScope
    @Provides
    fun provideIoDispatcher(): IoDispatcher = Dispatchers.IO

    // Database (singleton)
    @AppScope
    @Provides
    fun provideDatabase(driverFactory: DatabaseDriverFactory): RiteDatabase =
        RiteDatabase(driverFactory.createDriver())

    // UUID Provider (singleton)
    @AppScope
    @Provides
    fun provideUuidProvider(): UuidProvider = object : UuidProvider {
        override fun generate(): String = generateUuid()
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
    fun provideHabitInstanceRepository(impl: HabitInstanceRepositoryImpl): HabitInstanceRepository =
        impl

    @AppScope
    @Provides
    fun provideHabitCompletionEventRepository(
        impl: HabitCompletionEventRepositoryImpl
    ): HabitCompletionEventRepository = impl

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
        habitInstanceRepository: HabitInstanceRepository,
        createHabit: CreateHabit,
        uuidProvider: UuidProvider,
        habitNotification: HabitNotification
    ): HabitFormViewModel.Factory = object : HabitFormViewModel.Factory {
        override fun create(habitIdToEdit: String?): HabitFormViewModel = HabitFormViewModel(
            habitRepository,
            habitInstanceRepository,
            createHabit,
            uuidProvider,
            habitNotification,
            habitIdToEdit
        )
    }

    // Factory for HabitDetailViewModel (needs instanceId parameter)
    @AppScope
    @Provides
    fun provideHabitDetailViewModelFactory(
        habitRepository: HabitRepository,
        habitInstanceRepository: HabitInstanceRepository,
        userRepository: UserRepository,
        completeHabit: CompleteHabit,
        skipHabit: SkipHabit,
        undoHabit: UndoHabit
    ): HabitDetailViewModel.Factory = object : HabitDetailViewModel.Factory {
        override fun create(instanceId: String): HabitDetailViewModel = HabitDetailViewModel(
            habitRepository,
            habitInstanceRepository,
            userRepository,
            completeHabit,
            skipHabit,
            undoHabit,
            instanceId
        )
    }

    // Public accessors for App initialization
    abstract val startupViewModel: StartupViewModel
    abstract val userRepository: UserRepository
    abstract val habitRepository: HabitRepository
    abstract val habitInstanceRepository: HabitInstanceRepository
    abstract val onboardingViewModel: OnboardingViewModel
    abstract val todayViewModel: TodayViewModel
    abstract val calendarViewModel: CalendarViewModel
    abstract val settingsViewModel: SettingsViewModel
    abstract val archivedHabitsViewModel: ArchivedHabitsViewModel
    abstract val habitFormViewModelFactory: HabitFormViewModel.Factory
    abstract val habitDetailViewModelFactory: HabitDetailViewModel.Factory

    // Accessors for workers and receivers
    abstract val generateDailyHabits: GenerateDailyHabits
    abstract val processEndOfDay: ProcessEndOfDay
    abstract val completeHabit: CompleteHabit
    abstract val snoozeHabit: SnoozeHabit
    abstract val skipHabit: SkipHabit
    abstract val undoLastIncrement: UndoLastIncrement
    abstract val habitNotificationAccessor: HabitNotification

    companion object
}
