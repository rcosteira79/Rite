package com.ricardocosteira.habitlock.fakes

import com.ricardocosteira.habitlock.domain.models.LeavePeriod
import com.ricardocosteira.habitlock.domain.repositories.LeavePeriodRepository
import kotlinx.datetime.LocalDate

class FakeLeavePeriodRepository : LeavePeriodRepository {

    private val leavePeriods: MutableMap<String, LeavePeriod> = mutableMapOf()

    override suspend fun createLeavePeriod(leavePeriod: LeavePeriod) {
        leavePeriods[leavePeriod.id] = leavePeriod
    }

    override suspend fun getLeavePeriodById(id: String): LeavePeriod? = leavePeriods[id]

    override suspend fun getLeavePeriodsByHabit(habitId: String): List<LeavePeriod> =
        leavePeriods.values.filter { it.habitId == habitId }

    override suspend fun getActiveLeavePeriod(habitId: String, date: LocalDate): LeavePeriod? =
        leavePeriods.values.firstOrNull { period ->
            period.habitId == habitId &&
                date >= period.startDate &&
                (period.endDate == null || date <= period.endDate)
        }

    override suspend fun getAllActiveLeavePeriods(date: LocalDate): List<LeavePeriod> =
        leavePeriods.values.filter { period ->
            date >= period.startDate &&
                (period.endDate == null || date <= period.endDate)
        }

    override suspend fun updateLeavePeriod(leavePeriod: LeavePeriod) {
        leavePeriods[leavePeriod.id] = leavePeriod
    }

    override suspend fun endLeavePeriod(id: String, endDate: LocalDate) {
        val period: LeavePeriod = leavePeriods[id] ?: return
        leavePeriods[id] = period.copy(endDate = endDate)
    }

    override suspend fun deleteLeavePeriod(id: String) {
        leavePeriods.remove(id)
    }

    override suspend fun deleteLeavePeriodsForHabit(habitId: String) {
        leavePeriods.entries.removeAll { it.value.habitId == habitId }
    }
}
