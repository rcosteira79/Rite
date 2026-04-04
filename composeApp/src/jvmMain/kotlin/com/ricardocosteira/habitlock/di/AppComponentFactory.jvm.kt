package com.ricardocosteira.habitlock.di

import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.notifications.HabitNotification

actual fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification
): HabitLockAppComponent = HabitLockAppComponent::class.create(driverFactory, habitNotification)
