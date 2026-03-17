package com.ricardocosteira.habitlock.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TealPrimary = Color(0xFF006A6B)
val TealPrimaryContainer = Color(0xFFC0EDED)
val TealOnPrimaryContainer = Color(0xFF002020)
val TealSurface = Color(0xFFFAFCFC)
val TealOnSurface = Color(0xFF191C1C)
val TealOnSurfaceVariant = Color(0xFF3F4948)

val TealDarkPrimary = Color(0xFF4CDADA)
val TealDarkPrimaryContainer = Color(0xFF004F50)
val TealDarkOnPrimaryContainer = Color(0xFF9FF3F3)
val TealDarkSurface = Color(0xFF1B2030)
val TealDarkOnSurface = Color(0xFFDCE4E4)
val TealDarkOnSurfaceVariant = Color(0xFFBEC9C8)

val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    surface = TealSurface,
    onSurface = TealOnSurface,
    onSurfaceVariant = TealOnSurfaceVariant,
)

val DarkColorScheme = darkColorScheme(
    primary = TealDarkPrimary,
    primaryContainer = TealDarkPrimaryContainer,
    onPrimaryContainer = TealDarkOnPrimaryContainer,
    surface = TealDarkSurface,
    onSurface = TealDarkOnSurface,
    onSurfaceVariant = TealDarkOnSurfaceVariant,
)

@Composable
expect fun HabitLockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
)

@Composable
fun HabitLockThemeFallback(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
