package com.ricardocosteira.habitlock.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val TealPrimary = Color(0xFF006A6B)
internal val TealPrimaryContainer = Color(0xFFC0EDED)
internal val TealOnPrimaryContainer = Color(0xFF002020)
internal val TealSurface = Color(0xFFFAFCFC)
internal val TealOnSurface = Color(0xFF191C1C)
internal val TealOnSurfaceVariant = Color(0xFF3F4948)

internal val TealDarkPrimary = Color(0xFF4CDADA)
internal val TealDarkPrimaryContainer = Color(0xFF004F50)
internal val TealDarkOnPrimaryContainer = Color(0xFF9FF3F3)
internal val TealDarkSurface = Color(0xFF1B2030)
internal val TealDarkOnSurface = Color(0xFFDCE4E4)
internal val TealDarkOnSurfaceVariant = Color(0xFFBEC9C8)

internal val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    surface = TealSurface,
    onSurface = TealOnSurface,
    onSurfaceVariant = TealOnSurfaceVariant,
)

internal val DarkColorScheme = darkColorScheme(
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
