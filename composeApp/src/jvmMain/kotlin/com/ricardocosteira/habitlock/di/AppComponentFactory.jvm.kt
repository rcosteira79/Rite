package com.ricardocosteira.habitlock.di

import com.ricardocosteira.habitlock.data.DatabaseDriverFactory

actual fun createAppComponent(driverFactory: DatabaseDriverFactory): HabitLockAppComponent =
    HabitLockAppComponent::class.create(driverFactory)

