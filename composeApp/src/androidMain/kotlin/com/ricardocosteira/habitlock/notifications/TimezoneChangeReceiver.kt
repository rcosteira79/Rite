package com.ricardocosteira.habitlock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * BroadcastReceiver that handles timezone changes.
 * Reschedules all notifications to account for the new timezone.
 */
class TimezoneChangeReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            // Use goAsync to allow for coroutine work
            val pendingResult = goAsync()

            scope.launch {
                try {
                    // Cancel all existing notifications
                    val notificationScheduler = NotificationScheduler(context)
                    notificationScheduler.cancelAllNotifications()

                    // Reschedule all notifications with new timezone
                    rescheduleAllNotifications(context, notificationScheduler)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun rescheduleAllNotifications(context: Context, notificationScheduler: NotificationScheduler) {
        try {
            // Create AppModule to access repositories
            val driverFactory = DatabaseDriverFactory(context)
            val appModule = AppModule(driverFactory)

            val habitRepository = appModule.provideHabitRepository()
            val habitInstanceRepository = appModule.provideHabitInstanceRepository()

            // Get all active habits
            val habits = habitRepository.getActiveHabits()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val todayInstances = habitInstanceRepository.getInstancesForDate(today)

            // For each active habit, reschedule its reminders
            for (habit in habits) {
                // Get today's instance for this habit
                val instance = todayInstances.firstOrNull { it.habitId == habit.id }

                if (instance != null) {
                    // TODO: Reschedule based on habit's reminder settings
                    // This requires adding reminder time fields to the Habit model
                    // For now, we'll just use a default time
                    notificationScheduler.scheduleHabitReminder(
                        instance,
                        habit,
                        reminderTimeHour = 9, // Default 9:00 AM
                        reminderTimeMinute = 0
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun Clock.System.todayIn(timezone: TimeZone): LocalDate = now().toLocalDateTime(timezone).date

