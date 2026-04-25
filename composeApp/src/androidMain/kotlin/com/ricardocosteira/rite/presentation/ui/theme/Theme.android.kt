package com.ricardocosteira.rite.presentation.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun RiteTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val riteColors = if (darkTheme) DarkSageColors else LightSageColors
    val riteTypography = riteTypography()
    val riteShapes = RiteShapes()
    val riteSpacing = RiteSpacing()
    val riteMotion = RiteMotion()
    val riteDimensions = RiteDimensions()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalRiteColorScheme provides riteColors,
        LocalRiteTypography provides riteTypography,
        LocalRiteShapes provides riteShapes,
        LocalRiteSpacing provides riteSpacing,
        LocalRiteMotion provides riteMotion,
        LocalRiteDimensions provides riteDimensions
    ) {
        MaterialTheme(
            colorScheme = riteColors.toMaterialColorScheme(),
            typography = riteTypography.toMaterialTypography(),
            content = content
        )
    }
}
