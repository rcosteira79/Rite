package com.ricardocosteira.rite.di

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppComponent = staticCompositionLocalOf<HabitLockAppComponent> {
    error("No HabitLockAppComponent provided")
}
