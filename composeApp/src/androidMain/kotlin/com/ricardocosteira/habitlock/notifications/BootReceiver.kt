package com.ricardocosteira.habitlock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.habitLockApplication
import com.ricardocosteira.habitlock.util.todayIn
import com.ricardocosteira.habitlock.workers.WorkManagerInitializer
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * BroadcastReceiver that handles device boot completion.
 * Reschedules all workers and notifications after device restart.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        WorkManagerInitializer.initialize(context)
        NotificationChannels.createChannels(context)

        val appComponent: HabitLockAppComponent = context.habitLockApplication.appComponent
        val habitNotification: HabitNotification = appComponent.habitNotificationAccessor
        val habitRepository: HabitRepository = appComponent.habitRepository
        val habitInstanceRepository: HabitInstanceRepository = appComponent.habitInstanceRepository

        val pendingResult: PendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

                rescheduleReminders(
                    habitNotification,
                    habitRepository,
                    habitInstanceRepository,
                    today
                )
                restoreTrackingNotification(
                    habitNotification,
                    habitRepository,
                    habitInstanceRepository,
                    today
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun rescheduleReminders(
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

    private suspend fun restoreTrackingNotification(
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
