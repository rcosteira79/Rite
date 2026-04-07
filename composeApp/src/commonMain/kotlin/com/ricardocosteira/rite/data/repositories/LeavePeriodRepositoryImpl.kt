package com.ricardocosteira.rite.data.repositories

import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.data.mappers.EntityMappers.toDomain
import com.ricardocosteira.rite.di.IoDispatcher
import com.ricardocosteira.rite.domain.models.LeavePeriod
import com.ricardocosteira.rite.domain.repositories.LeavePeriodRepository
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

@Inject
class LeavePeriodRepositoryImpl(
    private val database: RiteDatabase,
    private val ioDispatcher: IoDispatcher
) : LeavePeriodRepository {

    private val queries = database.riteQueries

    override suspend fun createLeavePeriod(leavePeriod: LeavePeriod): Unit =
        withContext(ioDispatcher) {
            queries.insertLeavePeriod(
                id = leavePeriod.id,
                habitId = leavePeriod.habitId,
                startDate = leavePeriod.startDate.toString(),
                endDate = leavePeriod.endDate?.toString(),
                reason = leavePeriod.reason,
                createdAt = leavePeriod.createdAt.toString()
            )
        }

    override suspend fun getLeavePeriodById(id: String): LeavePeriod? = withContext(ioDispatcher) {
        queries.getLeavePeriodById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getLeavePeriodsByHabit(habitId: String): List<LeavePeriod> =
        withContext(ioDispatcher) {
            queries.getLeavePeriodsByHabit(habitId).executeAsList().map { it.toDomain() }
        }

    override suspend fun getActiveLeavePeriod(habitId: String, date: LocalDate): LeavePeriod? =
        withContext(ioDispatcher) {
            val dateStr = date.toString()
            queries.getActiveLeavePeriod(habitId, dateStr, dateStr).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun getAllActiveLeavePeriods(date: LocalDate): List<LeavePeriod> =
        withContext(ioDispatcher) {
            val dateStr = date.toString()
            queries.getAllActiveLeavePeriods(dateStr, dateStr).executeAsList().map { it.toDomain() }
        }

    override suspend fun updateLeavePeriod(leavePeriod: LeavePeriod): Unit =
        withContext(ioDispatcher) {
            queries.updateLeavePeriod(
                startDate = leavePeriod.startDate.toString(),
                endDate = leavePeriod.endDate?.toString(),
                reason = leavePeriod.reason,
                id = leavePeriod.id
            )
        }

    override suspend fun endLeavePeriod(id: String, endDate: LocalDate): Unit =
        withContext(ioDispatcher) {
            queries.updateLeavePeriodEndDate(
                endDate = endDate.toString(),
                id = id
            )
        }

    override suspend fun deleteLeavePeriod(id: String): Unit = withContext(ioDispatcher) {
        queries.deleteLeavePeriod(id)
    }

    override suspend fun deleteLeavePeriodsForHabit(habitId: String): Unit =
        withContext(ioDispatcher) {
            queries.deleteLeavePeriodsForHabit(habitId)
        }
}
