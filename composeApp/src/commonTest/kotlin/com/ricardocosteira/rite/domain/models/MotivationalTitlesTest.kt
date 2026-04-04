package com.ricardocosteira.rite.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class MotivationalTitlesTest {
    @Test
    fun given_a_date_when_getting_title_index_then_returns_valid_index() {
        val date = LocalDate(2026, 3, 26)
        val actualIndex: Int = motivationalTitleIndexForDate(date)
        assertTrue(actualIndex in 0 until MOTIVATIONAL_TITLE_COUNT)
    }

    @Test
    fun given_same_date_when_getting_title_index_twice_then_returns_same_index() {
        val date = LocalDate(2026, 3, 26)
        val firstCall: Int = motivationalTitleIndexForDate(date)
        val secondCall: Int = motivationalTitleIndexForDate(date)
        assertEquals(firstCall, secondCall)
    }

    @Test
    fun given_different_dates_when_getting_title_indices_then_not_all_are_the_same() {
        val indices: Set<Int> = (1..30)
            .map { day ->
                motivationalTitleIndexForDate(LocalDate(2026, 3, day))
            }.toSet()
        assertTrue(
            indices.size > 1,
            "Expected different indices for different dates, got: $indices"
        )
    }
}
