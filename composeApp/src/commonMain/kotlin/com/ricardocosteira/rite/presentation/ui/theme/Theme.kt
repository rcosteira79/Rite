package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalRiteColorScheme = staticCompositionLocalOf { LightSageColors }

object RiteAppTheme {
    val colors: RiteColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalRiteColorScheme.current

    val typography: RiteTypography
        @Composable @ReadOnlyComposable
        get() = LocalRiteTypography.current

    val shapes: RiteShapes
        @Composable @ReadOnlyComposable
        get() = LocalRiteShapes.current

    val spacing: RiteSpacing
        @Composable @ReadOnlyComposable
        get() = LocalRiteSpacing.current

    val motion: RiteMotion
        @Composable @ReadOnlyComposable
        get() = LocalRiteMotion.current

    val dimensions: RiteDimensions
        @Composable @ReadOnlyComposable
        get() = LocalRiteDimensions.current
}

@Composable
expect fun RiteTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)

@Composable
fun RiteThemeFallback(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val riteColors = if (darkTheme) DarkSageColors else LightSageColors
    val riteTypography = riteTypography()
    val riteShapes = RiteShapes()
    val riteSpacing = RiteSpacing()
    val riteMotion = RiteMotion()
    val riteDimensions = RiteDimensions()

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
            typography = riteTypography.toMaterialTypography()
        ) {
            Surface(content = content)
        }
    }
}
