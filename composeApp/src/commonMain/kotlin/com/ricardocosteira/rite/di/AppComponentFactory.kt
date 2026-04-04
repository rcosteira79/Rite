package com.ricardocosteira.rite.di

import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.notifications.HabitNotification

/**
 * Platform-specific factory for [HabitLockAppComponent].
 * Each platform instantiates the kotlin-inject generated [InjectHabitLockAppComponent].
 */
expect fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification
): HabitLockAppComponent
