package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Immutable
data class RiteMotion(
    val quick: Duration = 160.milliseconds,
    val standard: Duration = 280.milliseconds,
    val deliberate: Duration = 480.milliseconds,
    val easeQuiet: Easing = CubicBezierEasing(0.2f, 0.6f, 0.2f, 1f),
    val easeWeighted: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
)

val LocalRiteMotion = staticCompositionLocalOf { RiteMotion() }
