package com.ricardocosteira.rite.domain.time

import com.ricardocosteira.rite.di.AppCoroutineScope
import com.ricardocosteira.rite.di.AppScope
import com.ricardocosteira.rite.domain.repositories.UserRepository
import kotlin.coroutines.coroutineContext
import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import me.tatarka.inject.annotations.Inject

/**
 * Source of truth for "what is today" in the user's local timezone. Re-emits when:
 * - The app comes to the foreground (via [AppForegroundObserver]).
 * - The wall clock crosses midnight (self-scheduled `delay` loop).
 *
 * Consumers (e.g. `TodayViewModel`) collect this and re-derive their state on each
 * change. The provider de-duplicates emissions via [StateFlow] semantics — only
 * actual date changes propagate.
 */
interface CurrentDateProvider {
    val today: StateFlow<LocalDate>
}

@AppScope
@Inject
class DefaultCurrentDateProvider(
    private val userRepository: UserRepository,
    private val foregroundObserver: AppForegroundObserver,
    private val applicationScope: AppCoroutineScope,
    private val clock: Clock
) : CurrentDateProvider {

    private val _today = MutableStateFlow(initialDate())
    override val today: StateFlow<LocalDate> = _today.asStateFlow()

    init {
        applicationScope.launch { refineInitialDate() }
        applicationScope.launch { observeForegroundChanges() }
        applicationScope.launch { tickAtMidnight() }
    }

    private fun initialDate(): LocalDate = clock.todayIn(TimeZone.currentSystemDefault())

    private suspend fun refineInitialDate() {
        _today.value = computeToday()
    }

    private suspend fun observeForegroundChanges() {
        foregroundObserver.onForeground.collect {
            _today.value = computeToday()
        }
    }

    private suspend fun tickAtMidnight() {
        while (coroutineContext.isActive) {
            val tz = currentTimezone()
            val current = clock.todayIn(tz)
            val nextMidnight = current.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)
            val nowMs = clock.now().toEpochMilliseconds()
            val delayMs = (nextMidnight.toEpochMilliseconds() - nowMs).coerceAtLeast(
                MIN_TICK_DELAY_MS
            )
            delay(delayMs)
            _today.value = clock.todayIn(tz)
        }
    }

    private suspend fun computeToday(): LocalDate = clock.todayIn(currentTimezone())

    private suspend fun currentTimezone(): TimeZone =
        userRepository.getUser()?.timezone ?: TimeZone.currentSystemDefault()

    private companion object {
        // Floor for the midnight delay so a clock-skew bug can't busy-loop.
        const val MIN_TICK_DELAY_MS: Long = 60_000L
    }
}
