package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class RiteSpacing(
    val gap1: Dp = 4.dp,
    val gap2: Dp = 8.dp,
    val gap3: Dp = 12.dp,
    val gap4: Dp = 16.dp,
    val gap5: Dp = 20.dp,
    val gap6: Dp = 24.dp,
    val gap7: Dp = 28.dp,
    val gap8: Dp = 32.dp
)

val LocalRiteSpacing = staticCompositionLocalOf { RiteSpacing() }
