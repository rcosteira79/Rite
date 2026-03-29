package com.ricardocosteira.habitlock.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class MotivationalTitlesTest {
    @Test
    fun given_a_date_when_getting_title_then_returns_non_blank_string() {
        val date = LocalDate(2026, 3, 26)
        val actualTitle = motivationalTitleForDate(date)
        assertTrue(actualTitle.isNotBlank())
    }

    @Test
    fun given_same_date_when_getting_title_twice_then_returns_same_title() {
        val date = LocalDate(2026, 3, 26)
        val firstCall = motivationalTitleForDate(date)
        val secondCall = motivationalTitleForDate(date)
        assertEquals(firstCall, secondCall)
    }

    @Test
    fun given_different_dates_when_getting_titles_then_not_all_are_the_same() {
        val titles = (1..30)
            .map { day ->
                motivationalTitleForDate(LocalDate(2026, 3, day))
            }.toSet()
        assertTrue(titles.size > 1, "Expected different titles for different dates, got: $titles")
    }
}
