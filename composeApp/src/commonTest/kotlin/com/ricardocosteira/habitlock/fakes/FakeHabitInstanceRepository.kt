package com.ricardocosteira.habitlock.fakes

import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class FakeHabitInstanceRepository : HabitInstanceRepository {

    private val instances: MutableMap<String, HabitInstance> = mutableMapOf()
    private val instancesFlow: MutableStateFlow<List<HabitInstance>> =
        MutableStateFlow(emptyList())

    fun addInstance(instance: HabitInstance) {
        instances[instance.id] = instance
        refreshFlow()
    }

    private fun refreshFlow() {
        instancesFlow.value = instances.values.toList()
    }

    override fun observeInstancesForDate(date: LocalDate): Flow<List<HabitInstance>> =
        instancesFlow.map { list -> list.filter { it.date == date } }

    override suspend fun getInstancesForDate(date: LocalDate): List<HabitInstance> =
        instances.values.filter { it.date == date }

    override suspend fun getPendingInstancesForDate(date: LocalDate): List<HabitInstance> =
        instances.values.filter { it.date == date && it.status == HabitStatus.PENDING }

    override suspend fun getInstanceById(instanceId: String): HabitInstance? = instances[instanceId]

    override suspend fun getInstanceForHabitAndDate(
        habitId: String,
        date: LocalDate
    ): HabitInstance? = instances.values.firstOrNull { it.habitId == habitId && it.date == date }

    override suspend fun getInstancesForHabit(habitId: String): List<HabitInstance> =
        instances.values.filter { it.habitId == habitId }

    override suspend fun getInstancesInDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitInstance> = instances.values.filter { it.date in startDate..endDate }

    override suspend fun createInstance(instance: HabitInstance) {
        instances[instance.id] = instance
        refreshFlow()
    }

    override suspend fun updateInstanceStatus(
        instanceId: String,
        status: HabitStatus,
        completedValue: Int?,
        completedAt: Instant?
    ) {
        val instance: HabitInstance = instances[instanceId] ?: return
        instances[instanceId] = instance.copy(
            status = status,
            completedValue = completedValue,
            completedAt = completedAt
        )
        refreshFlow()
    }

    override suspend fun updateInstanceCompletedValue(instanceId: String, completedValue: Int) {
        val instance: HabitInstance = instances[instanceId] ?: return
        instances[instanceId] = instance.copy(completedValue = completedValue)
        refreshFlow()
    }
}
