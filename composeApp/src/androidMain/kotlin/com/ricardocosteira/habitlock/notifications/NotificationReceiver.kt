package com.ricardocosteira.habitlock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ricardocosteira.habitlock.habitLockApplication
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

        if (notificationType == null || instanceId == null) {
            return
        }

        // Use goAsync to allow for coroutine work
        val pendingResult = goAsync()

        scope.launch {
            try {
                val appComponent = context.habitLockApplication.appComponent
                val instance = appComponent.habitInstanceRepository.getInstanceById(instanceId)

                if (instance != null) {
                    val habit = appComponent.habitRepository.getHabitById(instance.habitId)

                    if (habit != null) {
                        val notificationManager = HabitNotificationManager(context)

                        when (NotificationScheduler.NotificationType.valueOf(notificationType)) {
                            NotificationScheduler.NotificationType.HABIT_REMINDER,
                            NotificationScheduler.NotificationType.SNOOZE_REMINDER -> {
                                notificationManager.showHabitReminder(instance, habit)
                            }
                            NotificationScheduler.NotificationType.GRACE_PERIOD -> {
                                notificationManager.showGracePeriodNotification(instance, habit)
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
