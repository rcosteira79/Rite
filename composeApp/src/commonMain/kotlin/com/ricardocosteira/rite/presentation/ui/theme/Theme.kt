package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Forest Discipline — Light palette
private val ForestPrimary = Color(0xFF163829)
private val ForestOnPrimary = Color(0xFFFFFFFF)
private val ForestPrimaryContainer = Color(0xFF2D4F3F)
private val ForestOnPrimaryContainer = Color(0xFFFFFFFF)
private val ForestSecondary = Color(0xFF545F72)
private val ForestOnSecondary = Color(0xFFFFFFFF)
private val ForestSecondaryContainer = Color(0xFFD5E0F7)
private val ForestOnSecondaryContainer = Color(0xFF1C2536)
private val ForestTertiary = Color(0xFF3B6352)
private val ForestOnTertiary = Color(0xFFFFFFFF)
private val ForestTertiaryContainer = Color(0xFFA9CFBA)
private val ForestOnTertiaryContainer = Color(0xFF0C2118)
private val ForestSurface = Color(0xFFFDF9F6)
private val ForestOnSurface = Color(0xFF2D4F3F)
private val ForestSurfaceVariant = Color(0xFFE5E2DF)
private val ForestOnSurfaceVariant = Color(0xFF334155)
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

// Stoic Night — Dark palette (design system: night_discipline/DESIGN.md)
private val ForestDarkPrimary = Color(0xFFA9CFBA) // sage — high-importance actions
private val ForestDarkOnPrimary = Color(0xFF143728) // dark text on sage buttons
private val ForestDarkPrimaryContainer = Color(0xFF2D4F3F) // forest green — completion/selected
private val ForestDarkOnPrimaryContainer = Color(0xFFE5E2DF) // off-white (tertiary-fixed) — 6.35:1 on forest green
private val ForestDarkSecondary = Color(0xFFB8C4D8)
private val ForestDarkOnSecondary = Color(0xFF253141)
private val ForestDarkSecondaryContainer = Color(0xFF3B4759)
private val ForestDarkOnSecondaryContainer = Color(0xFFD5E0F7)
private val ForestDarkTertiary = Color(0xFFA9CFBA)
private val ForestDarkOnTertiary = Color(0xFF143728)
private val ForestDarkTertiaryContainer = Color(0xFF22493A)
private val ForestDarkOnTertiaryContainer = Color(0xFFA9CFBA)
private val ForestDarkSurface = Color(0xFF131313) // obsidian void
private val ForestDarkOnSurface = Color(0xFFE5E2DF) // tertiary-fixed — stone under moonlight
private val ForestDarkSurfaceVariant = Color(0xFF414844)
private val ForestDarkOnSurfaceVariant = Color(0xFFBBC5BC)
private val ForestDarkSurfaceContainerLowest = Color(0xFF0E0E0E)
private val ForestDarkSurfaceContainerLow = Color(0xFF1C1B1B)
private val ForestDarkSurfaceContainer = Color(0xFF201F1F)
private val ForestDarkSurfaceContainerHigh = Color(0xFF2A2A2A)
private val ForestDarkSurfaceContainerHighest = Color(0xFF333333)
private val ForestDarkOutline = Color(0xFF858F87)
private val ForestDarkOutlineVariant = Color(0xFF414844) // ghost border
private val ForestDarkBackground = Color(0xFF131313)
private val ForestDarkOnBackground = Color(0xFFE5E2DF)

// --- Rite Color Scheme ---

@Immutable
data class RiteColorScheme(
    private val material: ColorScheme,
    // Day classification colours — heatmap, calendar, etc.
    val perfect: Color,
    val bestEffort: Color,
    val partial: Color,
    val roughDay: Color,
    val failed: Color,
    val noData: Color,
    val skipped: Color
) {
    // M3 delegation
    val primary: Color get() = material.primary
    val onPrimary: Color get() = material.onPrimary
    val primaryContainer: Color get() = material.primaryContainer
    val onPrimaryContainer: Color get() = material.onPrimaryContainer
    val secondary: Color get() = material.secondary
    val onSecondary: Color get() = material.onSecondary
    val secondaryContainer: Color get() = material.secondaryContainer
    val onSecondaryContainer: Color get() = material.onSecondaryContainer
    val tertiary: Color get() = material.tertiary
    val onTertiary: Color get() = material.onTertiary
    val tertiaryContainer: Color get() = material.tertiaryContainer
    val onTertiaryContainer: Color get() = material.onTertiaryContainer
    val error: Color get() = material.error
    val onError: Color get() = material.onError
    val errorContainer: Color get() = material.errorContainer
    val onErrorContainer: Color get() = material.onErrorContainer
    val surface: Color get() = material.surface
    val onSurface: Color get() = material.onSurface
    val surfaceVariant: Color get() = material.surfaceVariant
    val onSurfaceVariant: Color get() = material.onSurfaceVariant
    val surfaceContainerLowest: Color get() = material.surfaceContainerLowest
    val surfaceContainerLow: Color get() = material.surfaceContainerLow
    val surfaceContainer: Color get() = material.surfaceContainer
    val surfaceContainerHigh: Color get() = material.surfaceContainerHigh
    val surfaceContainerHighest: Color get() = material.surfaceContainerHighest
    val outline: Color get() = material.outline
    val outlineVariant: Color get() = material.outlineVariant
    val background: Color get() = material.background
    val onBackground: Color get() = material.onBackground
    val inverseSurface: Color get() = material.inverseSurface
    val inverseOnSurface: Color get() = material.inverseOnSurface
    val inversePrimary: Color get() = material.inversePrimary

    internal fun toMaterialColorScheme(): ColorScheme = material
}

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
    inversePrimary = ForestInversePrimary
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
    onBackground = ForestDarkOnBackground
)

internal val LightRiteColorScheme = RiteColorScheme(
    material = LightColorScheme,
    perfect = Color(0xFF4CAF7A),
    bestEffort = Color(0xFF26A8BF),
    partial = Color(0xFF5C7CDB),
    roughDay = Color(0xFFCD8B62),
    failed = Color(0xFFE57373),
    noData = Color(0xFFE0E0E0),
    skipped = Color(0xFFBDBDBD)
)

internal val DarkRiteColorScheme = RiteColorScheme(
    material = DarkColorScheme,
    perfect = Color(0xFF2D6B4A),
    bestEffort = Color(0xFF1F7A8A),
    partial = Color(0xFF3B5998),
    roughDay = Color(0xFF8B5E3C),
    failed = Color(0xFF8B3A3A),
    noData = Color(0xFF333333),
    skipped = Color(0xFF5A5A5A)
)

val LocalRiteColorScheme = staticCompositionLocalOf { DarkRiteColorScheme }

object RiteAppTheme {
    val colorScheme: RiteColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalRiteColorScheme.current

    val typography: Typography
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography
}

@Composable
expect fun RiteTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)

@Composable
fun RiteThemeFallback(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val riteColors = if (darkTheme) DarkRiteColorScheme else LightRiteColorScheme
    CompositionLocalProvider(LocalRiteColorScheme provides riteColors) {
        MaterialTheme(
            colorScheme = riteColors.toMaterialColorScheme(),
            typography = habitLockTypography()
        ) {
            Surface(content = content)
        }
    }
}
