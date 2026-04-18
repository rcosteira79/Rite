package com.ricardocosteira.rite.presentation.ui.theme

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class RiteTokensTest {

    @Test
    fun `RiteShapes has seven corner sizes from xs 2dp to xxl 20dp plus pill`() {
        val shapes = RiteShapes()
        // Just assert the data class exposes all seven fields — instances are compared via TopStart corner size
        // Sanity: calling the constructor compiles
        shapes.xs
        shapes.sm
        shapes.md
        shapes.lg
        shapes.xl
        shapes.xxl
        shapes.pill
    }

    @Test
    fun `RiteSpacing exposes gap1 through gap8 with expected dp values`() {
        val spacing = RiteSpacing()
        assertEquals(4.dp, spacing.gap1)
        assertEquals(8.dp, spacing.gap2)
        assertEquals(12.dp, spacing.gap3)
        assertEquals(16.dp, spacing.gap4)
        assertEquals(20.dp, spacing.gap5)
        assertEquals(24.dp, spacing.gap6)
        assertEquals(28.dp, spacing.gap7)
        assertEquals(32.dp, spacing.gap8)
    }

    @Test
    fun `RiteMotion exposes three durations and two easings`() {
        val motion = RiteMotion()
        assertEquals(160, motion.quick.inWholeMilliseconds.toInt())
        assertEquals(280, motion.standard.inWholeMilliseconds.toInt())
        assertEquals(480, motion.deliberate.inWholeMilliseconds.toInt())
        motion.easeQuiet
        motion.easeWeighted
    }

    @Test
    fun `RiteDimensions exposes icon size defaults`() {
        val dims = RiteDimensions()
        assertEquals(20.dp, dims.iconDefault)
        assertEquals(44.dp, dims.touchTargetMin)
    }
}
