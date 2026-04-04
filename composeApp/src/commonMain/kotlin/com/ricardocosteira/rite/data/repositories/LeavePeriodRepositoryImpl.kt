package com.ricardocosteira.rite.data.repositories

import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.data.mappers.EntityMappers.toDomain
import com.ricardocosteira.rite.domain.models.LeavePeriod
import com.ricardocosteira.rite.domain.repositories.LeavePeriodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

@Inject
class LeavePeriodRepositoryImpl(
    private val database: RiteDatabase
) : LeavePeriodRepository {

    private val queries = database.habitLockQueries

    override suspend fun createLeavePeriod(leavePeriod: LeavePeriod): Unit = withContext(Dispatchers.IO) {
        queries.insertLeavePeriod(
            id = leavePeriod.id,
            habitId = leavePeriod.habitId,
            startDate = leavePeriod.startDate.toString(),
            endDate = leavePeriod.endDate?.toString(),
            reason = leavePeriod.reason,
            createdAt = leavePeriod.createdAt.toString()
        )
    }

    override suspend fun getLeavePeriodById(id: String): LeavePeriod? = withContext(Dispatchers.IO) {
        queries.getLeavePeriodById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getLeavePeriodsByHabit(habitId: String): List<LeavePeriod> = withContext(Dispatchers.IO) {
        queries.getLeavePeriodsByHabit(habitId).executeAsList().map { it.toDomain() }
    }

    override suspend fun getActiveLeavePeriod(habitId: String, date: LocalDate): LeavePeriod? = withContext(Dispatchers.IO) {
        val dateStr = date.toString()
        queries.getActiveLeavePeriod(habitId, dateStr, dateStr).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getAllActiveLeavePeriods(date: LocalDate): List<LeavePeriod> = withContext(Dispatchers.IO) {
        val dateStr = date.toString()
        queries.getAllActiveLeavePeriods(dateStr, dateStr).executeAsList().map { it.toDomain() }
    }

    override suspend fun updateLeavePeriod(leavePeriod: LeavePeriod): Unit = withContext(Dispatchers.IO) {
        queries.updateLeavePeriod(
            startDate = leavePeriod.startDate.toString(),
            endDate = leavePeriod.endDate?.toString(),
            reason = leavePeriod.reason,
            id = leavePeriod.id
        )
    }

    override suspend fun endLeavePeriod(id: String, endDate: LocalDate): Unit = withContext(Dispatchers.IO) {
        queries.updateLeavePeriodEndDate(
            endDate = endDate.toString(),
            id = id
        )
    }

    override suspend fun deleteLeavePeriod(id: String): Unit = withContext(Dispatchers.IO) {
        queries.deleteLeavePeriod(id)
    }

    override suspend fun deleteLeavePeriodsForHabit(habitId: String): Unit = withContext(Dispatchers.IO) {
        queries.deleteLeavePeriodsForHabit(habitId)
    }
}
