package com.ricardocosteira.rite.domain.usecases

import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import com.ricardocosteira.rite.util.todayIn
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject

/**
 * Generates today's instance for every active, non-archived habit by delegating to
 * [GenerateInstanceForHabit]. Also reconciles a timezone change if the system
 * timezone differs from the user's stored one. Called on app launch and by the
 * daily background job.
 */
@Inject
class GenerateDailyHabits(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val generateInstanceForHabit: GenerateInstanceForHabit
) {
    suspend fun execute(): List<HabitInstance> {
        val user = userRepository.getUser() ?: return emptyList()
        val today = Clock.System.todayIn(user.timezone)

        checkTimezoneChange(user.timezone)

        val activeHabits = habitRepository.getActiveHabits()
        val newInstances = mutableListOf<HabitInstance>()

        for (habit in activeHabits) {
            val schedule = habitRepository.getScheduleForHabit(habit.id) ?: continue
            generateInstanceForHabit.execute(habit, schedule, today)?.let {
                newInstances.add(it)
            }
        }

        return newInstances
    }

    private suspend fun checkTimezoneChange(currentTimezone: TimeZone) {
        val systemTimezone = TimeZone.currentSystemDefault()
        if (systemTimezone != currentTimezone) {
            val user = userRepository.getUser() ?: return
            userRepository.updateTimezone(
                userId = user.id,
                newTimezone = systemTimezone,
                previousTimezone = currentTimezone
            )
        }
    }
}
