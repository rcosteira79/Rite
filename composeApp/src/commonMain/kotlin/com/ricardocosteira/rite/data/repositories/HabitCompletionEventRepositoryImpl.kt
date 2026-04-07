package com.ricardocosteira.rite.data.repositories

import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.data.mappers.EntityMappers.toDomain
import com.ricardocosteira.rite.di.IoDispatcher
import com.ricardocosteira.rite.domain.models.CompletionSource
import com.ricardocosteira.rite.domain.models.HabitCompletionEvent
import com.ricardocosteira.rite.domain.repositories.HabitCompletionEventRepository
import kotlin.time.Clock
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class HabitCompletionEventRepositoryImpl(
    private val database: RiteDatabase,
    private val ioDispatcher: IoDispatcher
) : HabitCompletionEventRepository {

    private val queries = database.riteQueries

    override suspend fun getEventsForInstance(instanceId: String): List<HabitCompletionEvent> =
        withContext(ioDispatcher) {
            queries.getEventsForInstance(instanceId).executeAsList().map { it.toDomain() }
        }

    override suspend fun recordEvent(
        instanceId: String,
        deltaValue: Int,
        source: CompletionSource
    ): HabitCompletionEvent = withContext(ioDispatcher) {
        val id = generateUuid()
        val timestamp = Clock.System.now()

        queries.insertEvent(
            id = id,
            habitInstanceId = instanceId,
            timestamp = timestamp.toString(),
            deltaValue = deltaValue.toLong(),
            source = source.name
        )

        HabitCompletionEvent(
            id = id,
            habitInstanceId = instanceId,
            timestamp = timestamp,
            deltaValue = deltaValue,
            source = source
        )
    }

    override suspend fun deleteEvent(eventId: String): Unit = withContext(ioDispatcher) {
        queries.deleteEvent(eventId)
    }

    override suspend fun deleteEventsForInstance(instanceId: String): Unit =
        withContext(ioDispatcher) {
            queries.deleteEventsForInstance(instanceId)
        }

    override suspend fun calculateCompletedValue(instanceId: String): Int =
        withContext(ioDispatcher) {
            val events = queries.getEventsForInstance(instanceId).executeAsList()
            events.sumOf { it.deltaValue.toInt() }
        }
}
