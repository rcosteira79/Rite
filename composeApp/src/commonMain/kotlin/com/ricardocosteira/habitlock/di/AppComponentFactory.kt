package com.ricardocosteira.habitlock.di

import com.ricardocosteira.habitlock.data.DatabaseDriverFactory

/**
 * Platform-specific factory for [HabitLockAppComponent].
 * Each platform instantiates the kotlin-inject generated [InjectHabitLockAppComponent].
 */
expect fun createAppComponent(driverFactory: DatabaseDriverFactory): HabitLockAppComponent

