package com.ricardocosteira.rite.data.repositories

import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.data.mappers.EntityMappers.toDomain
import com.ricardocosteira.rite.di.IoDispatcher
import com.ricardocosteira.rite.domain.models.SnoozeState
import com.ricardocosteira.rite.domain.repositories.SnoozeRepository
import kotlin.time.Instant
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class SnoozeRepositoryImpl(
    private val database: RiteDatabase,
    private val ioDispatcher: IoDispatcher
) : SnoozeRepository {

    private val queries = database.riteQueries

    override suspend fun getSnoozeState(instanceId: String): SnoozeState? =
        withContext(ioDispatcher) {
            queries.getSnoozeState(instanceId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun saveSnoozeState(
        instanceId: String,
        scheduledTime: Instant,
        snoozeCount: Int
    ): Unit = withContext(ioDispatcher) {
        queries.insertOrReplaceSnoozeState(
            habitInstanceId = instanceId,
            scheduledTime = scheduledTime.toString(),
            snoozeCount = snoozeCount.toLong()
        )
    }

    override suspend fun clearSnoozeState(instanceId: String): Unit = withContext(ioDispatcher) {
        queries.deleteSnoozeState(instanceId)
    }

    override suspend fun getAllSnoozeStates(): List<SnoozeState> = withContext(ioDispatcher) {
        queries.getAllSnoozeStates().executeAsList().map { it.toDomain() }
    }
}
