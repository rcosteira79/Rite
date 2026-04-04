package com.ricardocosteira.rite.di

import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.notifications.HabitNotification

actual fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification
): RiteAppComponent = RiteAppComponent::class.create(driverFactory, habitNotification)
