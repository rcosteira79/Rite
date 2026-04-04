package com.ricardocosteira.rite.domain.usecases

import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.repositories.HabitCompletionEventRepository
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import me.tatarka.inject.annotations.Inject

/**
 * Removes the most recent increment from a quantitative habit instance
 * that is still in progress (PENDING with completedValue > 0).
 */
@Inject
class UndoLastIncrement(
    private val habitInstanceRepository: HabitInstanceRepository,
    private val habitCompletionEventRepository: HabitCompletionEventRepository
) {

    suspend fun execute(instanceId: String): Result<HabitInstance> {
        val instance = habitInstanceRepository.getInstanceById(instanceId)
            ?: return Result.failure(IllegalArgumentException("Instance not found"))

        if (instance.status != HabitStatus.PENDING) {
            return Result.failure(
                IllegalStateException("Can only undo increments on pending instances")
            )
        }

        val events = habitCompletionEventRepository.getEventsForInstance(instanceId)
        if (events.isEmpty()) {
            return Result.failure(IllegalStateException("No progress to undo"))
        }

        val latestEvent = events.maxBy { it.timestamp }
        habitCompletionEventRepository.deleteEvent(latestEvent.id)

        val newCompletedValue = habitCompletionEventRepository.calculateCompletedValue(instanceId)

        habitInstanceRepository.updateInstanceStatus(
            instanceId = instanceId,
            status = HabitStatus.PENDING,
            completedValue = newCompletedValue,
            completedAt = null
        )

        return habitInstanceRepository.getInstanceById(instanceId)
            ?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Failed to retrieve updated instance"))
    }
}
