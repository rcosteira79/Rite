package com.ricardocosteira.rite.di

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppComponent = staticCompositionLocalOf<RiteAppComponent> {
    error("No RiteAppComponent provided")
}
