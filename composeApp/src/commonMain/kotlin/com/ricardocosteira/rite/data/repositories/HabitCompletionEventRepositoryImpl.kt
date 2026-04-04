package com.ricardocosteira.rite.data.repositories

import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.data.mappers.EntityMappers.toDomain
import com.ricardocosteira.rite.domain.models.CompletionSource
import com.ricardocosteira.rite.domain.models.HabitCompletionEvent
import com.ricardocosteira.rite.domain.repositories.HabitCompletionEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import kotlin.time.Clock

@Inject
class HabitCompletionEventRepositoryImpl(
    private val database: RiteDatabase
) : HabitCompletionEventRepository {

    private val queries = database.riteQueries

    override suspend fun getEventsForInstance(instanceId: String): List<HabitCompletionEvent> =
        withContext(Dispatchers.IO) {
            queries.getEventsForInstance(instanceId).executeAsList().map { it.toDomain() }
        }

    override suspend fun recordEvent(
        instanceId: String,
        deltaValue: Int,
        source: CompletionSource
    ): HabitCompletionEvent = withContext(Dispatchers.IO) {
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

    override suspend fun deleteEvent(eventId: String): Unit = withContext(Dispatchers.IO) {
        queries.deleteEvent(eventId)
    }

    override suspend fun deleteEventsForInstance(instanceId: String): Unit = 
        withContext(Dispatchers.IO) {
            queries.deleteEventsForInstance(instanceId)
        }

    override suspend fun calculateCompletedValue(instanceId: String): Int =
        withContext(Dispatchers.IO) {
            val events = queries.getEventsForInstance(instanceId).executeAsList()
            events.sumOf { it.deltaValue.toInt() }
        }
}

