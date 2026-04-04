package com.ricardocosteira.rite.data.repositories

import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.data.mappers.EntityMappers.toDomain
import com.ricardocosteira.rite.domain.models.SnoozeState
import com.ricardocosteira.rite.domain.repositories.SnoozeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import kotlin.time.Instant

@Inject
class SnoozeRepositoryImpl(
    private val database: RiteDatabase
) : SnoozeRepository {

    private val queries = database.riteQueries

    override suspend fun getSnoozeState(instanceId: String): SnoozeState? =
        withContext(Dispatchers.IO) {
            queries.getSnoozeState(instanceId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun saveSnoozeState(
        instanceId: String,
        scheduledTime: Instant,
        snoozeCount: Int
    ): Unit = withContext(Dispatchers.IO) {
        queries.insertOrReplaceSnoozeState(
            habitInstanceId = instanceId,
            scheduledTime = scheduledTime.toString(),
            snoozeCount = snoozeCount.toLong()
        )
    }

    override suspend fun clearSnoozeState(instanceId: String): Unit = withContext(Dispatchers.IO) {
        queries.deleteSnoozeState(instanceId)
    }

    override suspend fun getAllSnoozeStates(): List<SnoozeState> = withContext(Dispatchers.IO) {
        queries.getAllSnoozeStates().executeAsList().map { it.toDomain() }
    }
}

