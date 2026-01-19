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

/**
 * BroadcastReceiver that handles scheduled notifications from AlarmManager.
 * Receives notification intents and displays them using HabitNotificationManager.
 */
class NotificationReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val notificationType = intent.getStringExtra(NotificationScheduler.EXTRA_NOTIFICATION_TYPE)
        val instanceId = intent.getStringExtra(NotificationScheduler.EXTRA_INSTANCE_ID)
        val habitName = intent.getStringExtra(NotificationScheduler.EXTRA_HABIT_NAME)

        if (notificationType == null || instanceId == null) {
            return
        }

        // Use goAsync to allow for coroutine work
        val pendingResult = goAsync()

        scope.launch {
            try {
                // Create AppModule to access repositories
                val driverFactory = DatabaseDriverFactory(context)
                val appModule = AppModule(driverFactory)

                val habitInstanceRepository = appModule.provideHabitInstanceRepository()
                val habitRepository = appModule.provideHabitRepository()

                // Fetch the habit instance and habit
                val instance = habitInstanceRepository.getInstanceById(instanceId)

                if (instance != null) {
                    val habit = habitRepository.getHabitById(instance.habitId)

                    if (habit != null) {
                        val notificationManager = HabitNotificationManager(context)

                        when (NotificationScheduler.NotificationType.valueOf(notificationType)) {
                            NotificationScheduler.NotificationType.HABIT_REMINDER -> {
                                notificationManager.showHabitReminder(instance, habit)
                            }
                            NotificationScheduler.NotificationType.GRACE_PERIOD -> {
                                notificationManager.showGracePeriodNotification(instance, habit)
                            }
                            NotificationScheduler.NotificationType.SNOOZE_REMINDER -> {
                                notificationManager.showHabitReminder(instance, habit)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
