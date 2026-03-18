package com.ricardocosteira.habitlock.di

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppComponent = staticCompositionLocalOf<HabitLockAppComponent> {
    error("No HabitLockAppComponent provided")
}
