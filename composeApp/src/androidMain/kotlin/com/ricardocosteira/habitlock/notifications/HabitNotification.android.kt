package com.ricardocosteira.habitlock.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.ReminderType
import kotlinx.datetime.LocalTime

actual class HabitNotification(private val context: Context) {
    private val scheduler: NotificationScheduler = NotificationScheduler(context)
    private val trackingManager: TrackingNotificationManager = TrackingNotificationManager(context)

    actual fun scheduleReminder(habit: Habit, reminder: HabitReminder, instance: HabitInstance) {
        if (!isNotificationPermissionGranted()) return
        when (reminder.reminderType) {
            ReminderType.FIXED -> {
                val time: LocalTime = reminder.time ?: return
                scheduler.scheduleHabitReminder(instance, habit, time.hour, time.minute)
            }

            ReminderType.PERIODIC -> {
                // TODO: future work — schedule repeating alarms at intervalMinutes within startTime/endTime window
            }
        }
    }

    actual fun cancelReminder(instanceId: String) {
        scheduler.cancelNotificationsForInstance(instanceId)
    }

    actual fun cancelAllForHabit(habitId: String, instanceIds: List<String>) {
        instanceIds.forEach { scheduler.cancelNotificationsForInstance(it) }
        instanceIds.forEach { trackingManager.removeHabit(it) }
    }

    actual fun updateTrackingNotification(trackedHabits: List<TrackedHabitInfo>) {
        if (!isNotificationPermissionGranted()) return
        trackingManager.update(trackedHabits)
    }

    actual fun hideTrackingNotification() {
        trackingManager.hide()
    }

    actual fun isNotificationPermissionGranted(): Boolean =
        NotificationChannels.areNotificationsEnabled(context)

    actual fun openNotificationSettings() {
        val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
