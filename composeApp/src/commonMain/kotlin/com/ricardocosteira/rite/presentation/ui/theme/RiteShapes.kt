package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class RiteShapes(
    val xs: Shape = RoundedCornerShape(2.dp),
    val sm: Shape = RoundedCornerShape(4.dp),
    val md: Shape = RoundedCornerShape(8.dp),
    val lg: Shape = RoundedCornerShape(12.dp),
    val xl: Shape = RoundedCornerShape(16.dp),
    val xxl: Shape = RoundedCornerShape(20.dp),
    val pill: Shape = RoundedCornerShape(percent = 50)
)

val LocalRiteShapes = staticCompositionLocalOf { RiteShapes() }
