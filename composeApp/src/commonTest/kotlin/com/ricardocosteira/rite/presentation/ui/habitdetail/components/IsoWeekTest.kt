package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class IsoWeekTest {
    @Test
    fun `given a Monday, isoWeekNumber returns its week`() {
        val mon = LocalDate(2026, 4, 13) // Monday, ISO week 16
        assertEquals(16, isoWeekNumber(mon))
    }

    @Test
    fun `given a Sunday, isoWeekNumber returns the same week as previous Monday`() {
        val sun = LocalDate(2026, 4, 19) // Sunday, still ISO week 16
        assertEquals(16, isoWeekNumber(sun))
    }

    @Test
    fun `given Jan 1 2026 (Thursday), isoWeekNumber returns 1`() {
        val jan1 = LocalDate(2026, 1, 1)
        assertEquals(1, isoWeekNumber(jan1))
    }

    @Test
    fun `given Jan 1 2023 (Sunday), isoWeekNumber returns 52 (prior year's last week)`() {
        val jan1 = LocalDate(2023, 1, 1)
        assertEquals(52, isoWeekNumber(jan1))
    }

    @Test
    fun `given Dec 31 2020 (Thursday in ISO long year), isoWeekNumber returns 53`() {
        assertEquals(53, isoWeekNumber(LocalDate(2020, 12, 31)))
    }

    @Test
    fun `formatWeekRange formats start to end`() {
        val from = LocalDate(2026, 1, 26) // ISO week 5
        val to = LocalDate(2026, 4, 19) // ISO week 16
        assertEquals("W05 — W16", formatWeekRange(from = from, to = to))
    }
}
