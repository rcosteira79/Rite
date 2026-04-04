package com.ricardocosteira.habitlock.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.habitLockApplication
import com.ricardocosteira.habitlock.notifications.HabitNotification
import com.ricardocosteira.habitlock.notifications.TrackedHabitInfo
import com.ricardocosteira.habitlock.util.todayIn
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * Worker responsible for generating daily habit instances and processing end-of-day logic.
 * Runs at the start of each day (configured time) to:
 * 1. Mark yesterday's pending habits as FAILED (via ProcessEndOfDay)
 * 2. Generate today's habit instances (via GenerateDailyHabits)
 * 3. Schedule reminders for all active habits that have reminders configured
 * 4. Show the tracking notification for habits with tracking enabled
 */
class DailyHabitGenerationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val appComponent: HabitLockAppComponent = applicationContext.habitLockApplication.appComponent
        appComponent.processEndOfDay.execute()
        appComponent.generateDailyHabits.execute()

        val habitNotification: HabitNotification = appComponent.habitNotificationAccessor
        val habitRepository: HabitRepository = appComponent.habitRepository
        val habitInstanceRepository: HabitInstanceRepository = appComponent.habitInstanceRepository
        val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

        scheduleRemindersForToday(
            habitNotification,
            habitRepository,
            habitInstanceRepository,
            today
        )
        showTrackingNotification(
            habitNotification,
            habitRepository,
            habitInstanceRepository,
            today
        )

        Result.success()
    } catch (exception: Exception) {
        exception.printStackTrace()
        Result.retry()
    }

    private suspend fun scheduleRemindersForToday(
        habitNotification: HabitNotification,
        habitRepository: HabitRepository,
        habitInstanceRepository: HabitInstanceRepository,
        today: LocalDate
    ) {
        val activeHabits: List<Habit> = habitRepository.getActiveHabits()
        for (habit: Habit in activeHabits) {
            val reminders: List<HabitReminder> = habitRepository.getRemindersForHabit(habit.id)
            val reminder: HabitReminder? = reminders.firstOrNull()
            if (reminder != null) {
                val instance: HabitInstance? = habitInstanceRepository.getInstanceForHabitAndDate(
                    habit.id,
                    today
                )
                if (instance != null) {
                    habitNotification.scheduleReminder(habit, reminder, instance)
                }
            }
        }
    }

    private suspend fun showTrackingNotification(
        habitNotification: HabitNotification,
        habitRepository: HabitRepository,
        habitInstanceRepository: HabitInstanceRepository,
        today: LocalDate
    ) {
        val trackedHabits: List<Habit> = habitRepository.getHabitsWithTrackingEnabled()
        val trackedInfoList: List<TrackedHabitInfo> = trackedHabits.mapNotNull { habit: Habit ->
            val instance: HabitInstance =
                habitInstanceRepository.getInstanceForHabitAndDate(habit.id, today)
                    ?: return@mapNotNull null
            TrackedHabitInfo(
                instanceId = instance.id,
                habitId = habit.id,
                habitName = habit.name,
                type = habit.type,
                currentProgress = instance.currentProgress,
                targetValue = instance.targetValue,
                unit = habit.unit,
                defaultIncrement = habit.defaultIncrement,
                isCompleted = instance.status == HabitStatus.COMPLETED
            )
        }
        if (trackedInfoList.isNotEmpty()) {
            habitNotification.updateTrackingNotification(trackedInfoList)
        }
    }
}
