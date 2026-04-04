package com.ricardocosteira.rite.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ricardocosteira.rite.domain.models.ReminderType
import com.ricardocosteira.rite.habitLockApplication
import com.ricardocosteira.rite.util.todayIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

/**
 * BroadcastReceiver that handles timezone changes.
 * Reschedules all notifications to account for the new timezone.
 */
class TimezoneChangeReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_TIMEZONE_CHANGED) return

        val pendingResult = goAsync()

        scope.launch {
            try {
                val notificationScheduler = NotificationScheduler(context)
                notificationScheduler.cancelAllNotifications()
                rescheduleAllNotifications(context, notificationScheduler)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun rescheduleAllNotifications(
        context: Context,
        notificationScheduler: NotificationScheduler
    ) {
        try {
            val appComponent = context.habitLockApplication.appComponent
            val habitRepository = appComponent.habitRepository
            val habitInstanceRepository = appComponent.habitInstanceRepository

            val habits = habitRepository.getActiveHabits()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val todayInstances = habitInstanceRepository.getInstancesForDate(today)

            for (habit in habits) {
                val instance = todayInstances.firstOrNull { it.habitId == habit.id } ?: continue

                val reminders = habitRepository.getRemindersForHabit(habit.id)
                val activeFixedReminder = reminders.firstOrNull {
                    it.isActive && it.reminderType == ReminderType.FIXED && it.time != null
                }

                if (activeFixedReminder?.time != null) {
                    notificationScheduler.scheduleHabitReminder(
                        instance,
                        habit,
                        reminderTimeHour = activeFixedReminder.time.hour,
                        reminderTimeMinute = activeFixedReminder.time.minute
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
