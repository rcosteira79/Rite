package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class RiteDimensions(
    val iconSmall: Dp = 13.dp,
    val iconCompact: Dp = 16.dp,
    val iconDefault: Dp = 20.dp,
    val iconLarge: Dp = 22.dp,
    val touchTargetMin: Dp = 44.dp
)

val LocalRiteDimensions = staticCompositionLocalOf { RiteDimensions() }
