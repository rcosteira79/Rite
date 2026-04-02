package com.ricardocosteira.habitlock.di

import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.notifications.HabitNotification

/**
 * Platform-specific factory for [HabitLockAppComponent].
 * Each platform instantiates the kotlin-inject generated [InjectHabitLockAppComponent].
 */
expect fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification
): HabitLockAppComponent
