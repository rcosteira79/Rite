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
import com.ricardocosteira.habitlock.domain.usecases.CompleteHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabitsUseCase
import com.ricardocosteira.habitlock.domain.usecases.ProcessEndOfDayUseCase
import com.ricardocosteira.habitlock.domain.usecases.SkipHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.SnoozeHabitUseCase
import com.ricardocosteira.habitlock.domain.usecases.UuidProvider
import com.ricardocosteira.habitlock.generateUuid

/**
 * Manual dependency injection module for Android-specific components.
 *
 * This is used by BroadcastReceivers and Workers which cannot use kotlin-inject
 * directly because they are instantiated by the Android framework.
 *
 * Uses lazy initialization to ensure singletons are created only once per instance.
 */
class AppModule(
    private val driverFactory: DatabaseDriverFactory
) {
    // Database (singleton per AppModule instance)
    private val database: HabitLockDatabase by lazy {
        HabitLockDatabase(driverFactory.createDriver())
    }

    // UUID Provider
    private val uuidProvider: UuidProvider by lazy {
        object : UuidProvider {
            override fun generate(): String = generateUuid()
        }
    }

    // Repositories (lazy singletons)
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

    private val leavePeriodRepository: LeavePeriodRepository by lazy {
        LeavePeriodRepositoryImpl(database)
    }

    private val snoozeRepository: SnoozeRepository by lazy {
        SnoozeRepositoryImpl(database)
    }

    // Repository providers (for external access)
    fun provideUserRepository(): UserRepository = userRepository

    fun provideHabitRepository(): HabitRepository = habitRepository

    fun provideHabitInstanceRepository(): HabitInstanceRepository = habitInstanceRepository

    fun provideHabitCompletionEventRepository(): HabitCompletionEventRepository = habitCompletionEventRepository

    fun provideLeavePeriodRepository(): LeavePeriodRepository = leavePeriodRepository

    fun provideSnoozeRepository(): SnoozeRepository = snoozeRepository

    // Use case providers
    fun provideCompleteHabitUseCase(): CompleteHabitUseCase {
        return CompleteHabitUseCase(
            habitInstanceRepository,
            habitRepository,
            habitCompletionEventRepository
        )
    }

    fun provideSkipHabitUseCase(): SkipHabitUseCase {
        return SkipHabitUseCase(
            habitInstanceRepository,
            userRepository
        )
    }

    fun provideSnoozeHabitUseCase(): SnoozeHabitUseCase {
        return SnoozeHabitUseCase(
            habitInstanceRepository,
            snoozeRepository,
            userRepository
        )
    }

    fun provideGenerateDailyHabitsUseCase(): GenerateDailyHabitsUseCase {
        return GenerateDailyHabitsUseCase(
            userRepository,
            habitRepository,
            habitInstanceRepository,
            leavePeriodRepository,
            uuidProvider
        )
    }

    fun provideProcessEndOfDayUseCase(): ProcessEndOfDayUseCase {
        return ProcessEndOfDayUseCase(
            userRepository,
            habitInstanceRepository,
            habitRepository
        )
    }
}
