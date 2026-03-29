package com.ricardocosteira.habitlock.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ricardocosteira.habitlock.data.database.HabitLockDatabase
import com.ricardocosteira.habitlock.data.mappers.EntityMappers.toDomain
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

@Inject
class HabitInstanceRepositoryImpl(private val database: HabitLockDatabase) :
    HabitInstanceRepository {
    private val queries = database.habitLockQueries

    override fun observeInstancesForDate(date: LocalDate): Flow<List<HabitInstance>> = queries
        .getInstancesForDate(date.toString())
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { list -> list.map { it.toDomain() } }

    override suspend fun getInstancesForDate(date: LocalDate): List<HabitInstance> =
        withContext(Dispatchers.IO) {
            queries.getInstancesForDate(date.toString()).executeAsList().map { it.toDomain() }
        }

    override suspend fun getPendingInstancesForDate(date: LocalDate): List<HabitInstance> =
        withContext(Dispatchers.IO) {
            queries.getPendingInstancesForDate(date.toString()).executeAsList().map {
                it.toDomain()
            }
        }

    override suspend fun getInstanceById(instanceId: String): HabitInstance? =
        withContext(Dispatchers.IO) {
            queries.getInstanceById(instanceId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun getInstanceForHabitAndDate(
        habitId: String,
        date: LocalDate
    ): HabitInstance? = withContext(Dispatchers.IO) {
        queries.getInstanceForHabitAndDate(
            habitId,
            date.toString()
        ).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getInstancesForHabit(habitId: String): List<HabitInstance> =
        withContext(Dispatchers.IO) {
            queries.getInstancesForHabit(habitId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getInstancesInDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitInstance> = withContext(Dispatchers.IO) {
        queries
            .getInstancesInDateRange(startDate.toString(), endDate.toString())
            .executeAsList()
            .map { it.toDomain() }
    }

    override suspend fun createInstance(instance: HabitInstance): Unit =
        withContext(Dispatchers.IO) {
            queries.insertInstance(
                id = instance.id,
                habitId = instance.habitId,
                date = instance.date.toString(),
                status = instance.status.name,
                completedValue = instance.completedValue?.toLong(),
                targetValue = instance.targetValue?.toLong(),
                consecutiveSkipsAtCreation = instance.consecutiveSkipsAtCreation.toLong(),
                createdAt = instance.createdAt.toString(),
                completedAt = instance.completedAt?.toString()
            )
        }

    override suspend fun updateInstanceStatus(
        instanceId: String,
        status: HabitStatus,
        completedValue: Int?,
        completedAt: Instant?
    ): Unit = withContext(Dispatchers.IO) {
        queries.updateInstanceStatus(
            status = status.name,
            completedValue = completedValue?.toLong(),
            completedAt = completedAt?.toString(),
            id = instanceId
        )
    }

    override suspend fun updateInstanceCompletedValue(
        instanceId: String,
        completedValue: Int
    ): Unit = withContext(Dispatchers.IO) {
        queries.updateInstanceCompletedValue(
            completedValue = completedValue.toLong(),
            id = instanceId
        )
    }
}
