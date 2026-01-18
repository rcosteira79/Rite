package com.ricardocosteira.habitlock.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar

/**
 * Service responsible for scheduling habit notifications using AlarmManager.
 * Handles:
 * - Habit reminder notifications at user-defined times
 * - Grace period notifications before habits are marked as failed
 * - Snooze reminder notifications
 */
class NotificationScheduler(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules a reminder notification for a habit instance.
     * 
     * @param instance The habit instance to send notification for
     * @param habit The habit details
     * @param reminderTimeHour Hour of day (0-23) to send notification
     * @param reminderTimeMinute Minute of hour (0-59) to send notification
     */
    fun scheduleHabitReminder(
        instance: HabitInstance,
        habit: Habit,
        reminderTimeHour: Int,
        reminderTimeMinute: Int
    ) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, reminderTimeHour)
            set(Calendar.MINUTE, reminderTimeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        scheduleNotification(
            notificationId = instance.id.hashCode(),
            triggerTimeMillis = calendar.timeInMillis,
            notificationType = NotificationType.HABIT_REMINDER,
            instanceId = instance.id,
            habitName = habit.name
        )
    }

    /**
     * Schedules a grace period notification for a pending habit.
     * Grace period is typically sent late in the day before the habit is marked as failed.
     * 
     * @param instance The habit instance to send grace period notification for
     * @param habit The habit details
     */
    fun scheduleGracePeriodNotification(instance: HabitInstance, habit: Habit) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 22) // 10 PM grace period notification
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Only schedule if it hasn't passed yet today
        if (calendar.timeInMillis > System.currentTimeMillis()) {
            scheduleNotification(
                notificationId = "${instance.id}_grace".hashCode(),
                triggerTimeMillis = calendar.timeInMillis,
                notificationType = NotificationType.GRACE_PERIOD,
                instanceId = instance.id,
                habitName = habit.name
            )
        }
    }

    /**
     * Schedules a snooze reminder notification.
     * 
     * @param instance The habit instance that was snoozed
     * @param habit The habit details
     * @param snoozeUntilMillis The time when the snooze should end and notification should fire
     */
    fun scheduleSnoozeReminder(
        instance: HabitInstance,
        habit: Habit,
        snoozeUntilMillis: Long
    ) {
        scheduleNotification(
            notificationId = "${instance.id}_snooze".hashCode(),
            triggerTimeMillis = snoozeUntilMillis,
            notificationType = NotificationType.SNOOZE_REMINDER,
            instanceId = instance.id,
            habitName = habit.name
        )
    }

    /**
     * Cancels all notifications for a specific habit instance.
     * Used when habit is completed, skipped, or suspended.
     * 
     * @param instanceId The ID of the habit instance
     */
    fun cancelNotificationsForInstance(instanceId: String) {
        // Cancel main reminder
        cancelNotification(instanceId.hashCode())
        
        // Cancel grace period notification
        cancelNotification("${instanceId}_grace".hashCode())
        
        // Cancel snooze reminder
        cancelNotification("${instanceId}_snooze".hashCode())
    }

    /**
     * Cancels all scheduled notifications for all habits.
     * Used when user changes timezone or when app needs to reschedule everything.
     */
    fun cancelAllNotifications() {
        // This is a simplified version - in production, you'd need to track all notification IDs
        // For now, we'll handle cancellations on a per-instance basis
    }

    /**
     * Reschedules all notifications.
     * Called when timezone changes or when user modifies reminder settings.
     */
    suspend fun rescheduleAllNotifications() {
        // Implementation will be added when we integrate with repositories
        // This would fetch all active habit instances and reschedule their notifications
    }

    // Private helper methods

    private fun scheduleNotification(
        notificationId: Int,
        triggerTimeMillis: Long,
        notificationType: NotificationType,
        instanceId: String,
        habitName: String
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_NOTIFICATION_TYPE, notificationType.name)
            putExtra(EXTRA_INSTANCE_ID, instanceId)
            putExtra(EXTRA_HABIT_NAME, habitName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use exact alarms for precise timing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    private fun cancelNotification(notificationId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_INSTANCE_ID = "instance_id"
        const val EXTRA_HABIT_NAME = "habit_name"
    }

    enum class NotificationType {
        HABIT_REMINDER,
        GRACE_PERIOD,
        SNOOZE_REMINDER
    }
}
