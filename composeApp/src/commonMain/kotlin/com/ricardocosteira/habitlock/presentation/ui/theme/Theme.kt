package com.ricardocosteira.habitlock.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Forest Discipline — Light palette
private val ForestPrimary = Color(0xFF163829)
private val ForestOnPrimary = Color(0xFFFFFFFF)
private val ForestPrimaryContainer = Color(0xFF2D4F3F)
private val ForestOnPrimaryContainer = Color(0xFFA9CFBA)
private val ForestSecondary = Color(0xFF545F72)
private val ForestOnSecondary = Color(0xFFFFFFFF)
private val ForestSecondaryContainer = Color(0xFFD5E0F7)
private val ForestOnSecondaryContainer = Color(0xFF1C2536)
private val ForestTertiary = Color(0xFF3B6352)
private val ForestOnTertiary = Color(0xFFFFFFFF)
private val ForestTertiaryContainer = Color(0xFFA9CFBA)
private val ForestOnTertiaryContainer = Color(0xFF0C2118)
private val ForestSurface = Color(0xFFFDF9F6)
private val ForestOnSurface = Color(0xFF1C1B1A)
private val ForestSurfaceVariant = Color(0xFFE5E2DF)
private val ForestOnSurfaceVariant = Color(0xFF545F72)
private val ForestSurfaceContainerLowest = Color(0xFFFFFFFF)
private val ForestSurfaceContainerLow = Color(0xFFF7F3F0)
private val ForestSurfaceContainer = Color(0xFFF1EDE9)
private val ForestSurfaceContainerHigh = Color(0xFFEBE7E4)
private val ForestSurfaceContainerHighest = Color(0xFFE5E2DF)
private val ForestOutline = Color(0xFF8A9A8B)
private val ForestOutlineVariant = Color(0xFFC1C8C2)
private val ForestBackground = Color(0xFFFDF9F6)
private val ForestOnBackground = Color(0xFF1C1B1A)
private val ForestError = Color(0xFFBA1A1A)
private val ForestOnError = Color(0xFFFFFFFF)
private val ForestErrorContainer = Color(0xFFFFDAD6)
private val ForestOnErrorContainer = Color(0xFF410002)
private val ForestInverseSurface = Color(0xFF2F3031)
private val ForestInverseOnSurface = Color(0xFFF0F1EF)
private val ForestInversePrimary = Color(0xFFA9CFBA)

// Forest Discipline — Dark palette
private val ForestDarkPrimary = Color(0xFFA9CFBA)
private val ForestDarkOnPrimary = Color(0xFF0C2118)
private val ForestDarkPrimaryContainer = Color(0xFF2D4F3F)
private val ForestDarkOnPrimaryContainer = Color(0xFFC8E6D5)
private val ForestDarkSecondary = Color(0xFFB8C4D8)
private val ForestDarkOnSecondary = Color(0xFF253141)
private val ForestDarkSecondaryContainer = Color(0xFF3B4759)
private val ForestDarkOnSecondaryContainer = Color(0xFFD5E0F7)
private val ForestDarkTertiary = Color(0xFFA9CFBA)
private val ForestDarkOnTertiary = Color(0xFF0C2118)
private val ForestDarkTertiaryContainer = Color(0xFF22493A)
private val ForestDarkOnTertiaryContainer = Color(0xFFC8E6D5)
private val ForestDarkSurface = Color(0xFF121412)
private val ForestDarkOnSurface = Color(0xFFE3E3E1)
private val ForestDarkSurfaceVariant = Color(0xFF3A3D3B)
private val ForestDarkOnSurfaceVariant = Color(0xFFBBC5BC)
private val ForestDarkSurfaceContainerLowest = Color(0xFF0C0E0D)
private val ForestDarkSurfaceContainerLow = Color(0xFF1A1C1A)
private val ForestDarkSurfaceContainer = Color(0xFF1E201E)
private val ForestDarkSurfaceContainerHigh = Color(0xFF282A28)
private val ForestDarkSurfaceContainerHighest = Color(0xFF333533)
private val ForestDarkOutline = Color(0xFF858F87)
private val ForestDarkOutlineVariant = Color(0xFF3A3D3B)
private val ForestDarkBackground = Color(0xFF121412)
private val ForestDarkOnBackground = Color(0xFFE3E3E1)

internal val LightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = ForestSecondary,
    onSecondary = ForestOnSecondary,
    secondaryContainer = ForestSecondaryContainer,
    onSecondaryContainer = ForestOnSecondaryContainer,
    tertiary = ForestTertiary,
    onTertiary = ForestOnTertiary,
    tertiaryContainer = ForestTertiaryContainer,
    onTertiaryContainer = ForestOnTertiaryContainer,
    surface = ForestSurface,
    onSurface = ForestOnSurface,
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = ForestOnSurfaceVariant,
    surfaceContainerLowest = ForestSurfaceContainerLowest,
    surfaceContainerLow = ForestSurfaceContainerLow,
    surfaceContainer = ForestSurfaceContainer,
    surfaceContainerHigh = ForestSurfaceContainerHigh,
    surfaceContainerHighest = ForestSurfaceContainerHighest,
    outline = ForestOutline,
    outlineVariant = ForestOutlineVariant,
    background = ForestBackground,
    onBackground = ForestOnBackground,
    error = ForestError,
    onError = ForestOnError,
    errorContainer = ForestErrorContainer,
    onErrorContainer = ForestOnErrorContainer,
    inverseSurface = ForestInverseSurface,
    inverseOnSurface = ForestInverseOnSurface,
    inversePrimary = ForestInversePrimary,
)

internal val DarkColorScheme = darkColorScheme(
    primary = ForestDarkPrimary,
    onPrimary = ForestDarkOnPrimary,
    primaryContainer = ForestDarkPrimaryContainer,
    onPrimaryContainer = ForestDarkOnPrimaryContainer,
    secondary = ForestDarkSecondary,
    onSecondary = ForestDarkOnSecondary,
    secondaryContainer = ForestDarkSecondaryContainer,
    onSecondaryContainer = ForestDarkOnSecondaryContainer,
    tertiary = ForestDarkTertiary,
    onTertiary = ForestDarkOnTertiary,
    tertiaryContainer = ForestDarkTertiaryContainer,
    onTertiaryContainer = ForestDarkOnTertiaryContainer,
    surface = ForestDarkSurface,
    onSurface = ForestDarkOnSurface,
    surfaceVariant = ForestDarkSurfaceVariant,
    onSurfaceVariant = ForestDarkOnSurfaceVariant,
    surfaceContainerLowest = ForestDarkSurfaceContainerLowest,
    surfaceContainerLow = ForestDarkSurfaceContainerLow,
    surfaceContainer = ForestDarkSurfaceContainer,
    surfaceContainerHigh = ForestDarkSurfaceContainerHigh,
    surfaceContainerHighest = ForestDarkSurfaceContainerHighest,
    outline = ForestDarkOutline,
    outlineVariant = ForestDarkOutlineVariant,
    background = ForestDarkBackground,
    onBackground = ForestDarkOnBackground,
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
        typography = habitLockTypography(),
        content = content
    )
}
