package com.ricardocosteira.rite.di

import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import com.ricardocosteira.rite.notifications.HabitNotification

actual fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification,
    appForegroundObserver: AppForegroundObserver
): RiteAppComponent =
    RiteAppComponent::class.create(driverFactory, habitNotification, appForegroundObserver)
