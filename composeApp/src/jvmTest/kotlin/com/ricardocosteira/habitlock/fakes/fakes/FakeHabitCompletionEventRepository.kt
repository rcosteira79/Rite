package com.ricardocosteira.habitlock.fakes

import com.ricardocosteira.habitlock.domain.models.CompletionSource
import com.ricardocosteira.habitlock.domain.models.HabitCompletionEvent
import com.ricardocosteira.habitlock.domain.repositories.HabitCompletionEventRepository
import kotlin.time.Clock

class FakeHabitCompletionEventRepository : HabitCompletionEventRepository {

    private val events: MutableMap<String, HabitCompletionEvent> = mutableMapOf()
    private var nextId: Int = 1

    override suspend fun getEventsForInstance(instanceId: String): List<HabitCompletionEvent> =
        events.values.filter { it.habitInstanceId == instanceId }

    override suspend fun recordEvent(
        instanceId: String,
        deltaValue: Int,
        source: CompletionSource
    ): HabitCompletionEvent {
        val event: HabitCompletionEvent = HabitCompletionEvent(
            id = "event-${nextId++}",
            habitInstanceId = instanceId,
            timestamp = Clock.System.now(),
            deltaValue = deltaValue,
            source = source
        )
        events[event.id] = event
        return event
    }

    override suspend fun deleteEvent(eventId: String) {
        events.remove(eventId)
    }

    override suspend fun deleteEventsForInstance(instanceId: String) {
        events.entries.removeAll { it.value.habitInstanceId == instanceId }
    }

    override suspend fun calculateCompletedValue(instanceId: String): Int = events.values
        .filter { it.habitInstanceId == instanceId }
        .sumOf { it.deltaValue }
}
