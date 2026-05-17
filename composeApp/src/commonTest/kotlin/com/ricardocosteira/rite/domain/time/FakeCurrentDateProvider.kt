package com.ricardocosteira.rite.domain.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

/**
 * Test double for [CurrentDateProvider] that lets tests imperatively drive the
 * "current date" to simulate day rollovers without touching the wall clock.
 */
class FakeCurrentDateProvider(initial: LocalDate) : CurrentDateProvider {
    private val _today = MutableStateFlow(initial)
    override val today: StateFlow<LocalDate> = _today.asStateFlow()

    fun setToday(date: LocalDate) {
        _today.value = date
    }
}
