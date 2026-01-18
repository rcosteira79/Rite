package com.ricardocosteira.habitlock.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ricardocosteira.habitlock.MainActivity
import com.ricardocosteira.habitlock.R
import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitType

/**
 * Manager for creating and displaying habit notifications.
 * Handles:
 * - Habit reminder notifications with action buttons
 * - Grace period warnings
 * - Daily summary notifications (optional)
 */
class HabitNotificationManager(
    private val context: Context
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Shows a habit reminder notification.
     * Includes action buttons based on habit type:
     * - Binary habits: "Complete", "Snooze", "Skip"
     * - Quantitative habits: "+1", "Snooze", "Skip"
     * 
     * @param instance The habit instance to remind about
     * @param habit The habit details
     */
    fun showHabitReminder(instance: HabitInstance, habit: Habit) {
        val notification = buildHabitReminderNotification(instance, habit)
        val notificationId = getNotificationId(instance.id)
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Shows a grace period notification.
     * This is a more urgent notification sent before habits are marked as failed.
     * 
     * @param instance The habit instance in grace period
     * @param habit The habit details
     */
    fun showGracePeriodNotification(instance: HabitInstance, habit: Habit) {
        val notification = buildGracePeriodNotification(instance, habit)
        val notificationId = getGracePeriodNotificationId(instance.id)
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Shows a daily summary notification.
     * Provides an overview of the day's habit completion.
     * 
     * @param completedCount Number of completed habits
     * @param totalCount Total number of habits for the day
     * @param perfectDay Whether all habits were completed
     */
    fun showDailySummary(completedCount: Int, totalCount: Int, perfectDay: Boolean) {
        val notification = buildDailySummaryNotification(completedCount, totalCount, perfectDay)
        notificationManager.notify(NotificationChannels.NOTIFICATION_ID_DAILY_SUMMARY, notification)
    }

    /**
     * Cancels a habit reminder notification.
     * 
     * @param instanceId The ID of the habit instance
     */
    fun cancelHabitReminder(instanceId: String) {
        val notificationId = getNotificationId(instanceId)
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancels a grace period notification.
     * 
     * @param instanceId The ID of the habit instance
     */
    fun cancelGracePeriodNotification(instanceId: String) {
        val notificationId = getGracePeriodNotificationId(instanceId)
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancels all notifications for a specific habit instance.
     * 
     * @param instanceId The ID of the habit instance
     */
    fun cancelAllNotificationsForInstance(instanceId: String) {
        cancelHabitReminder(instanceId)
        cancelGracePeriodNotification(instanceId)
    }

    /**
     * Cancels all active notifications from this app.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    // Private helper methods

    private fun buildHabitReminderNotification(instance: HabitInstance, habit: Habit): Notification {
        // Intent to open the app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            instance.id.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification title and content based on habit type
        val title = habit.name
        val content = when (habit.type) {
            HabitType.BINARY -> "Time to complete your habit"
            HabitType.QUANTITATIVE -> {
                val progress = instance.currentProgress
                val quota = habit.quota
                "Progress: $progress / $quota"
            }
        }

        val builder = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_HABITS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)

        // Add action buttons based on habit type
        when (habit.type) {
            HabitType.BINARY -> {
                builder.addAction(createCompleteAction(instance.id, habit.id))
            }
            HabitType.QUANTITATIVE -> {
                builder.addAction(createAddOneAction(instance.id, habit.id))
            }
        }

        builder.addAction(createSnoozeAction(instance.id))
        builder.addAction(createSkipAction(instance.id))

        return builder.build()
    }

    private fun buildGracePeriodNotification(instance: HabitInstance, habit: Habit): Notification {
        // Intent to open the app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            instance.id.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚠️ ${habit.name}"
        val content = "Last chance! This habit will be marked as failed soon."

        val builder = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_GRACE_PERIOD)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)

        // Add action buttons
        when (habit.type) {
            HabitType.BINARY -> {
                builder.addAction(createCompleteAction(instance.id, habit.id))
            }
            HabitType.QUANTITATIVE -> {
                builder.addAction(createAddOneAction(instance.id, habit.id))
            }
        }

        builder.addAction(createSkipAction(instance.id))

        return builder.build()
    }

    private fun buildDailySummaryNotification(
        completedCount: Int,
        totalCount: Int,
        perfectDay: Boolean
    ): Notification {
        // Intent to open the app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            NotificationChannels.NOTIFICATION_ID_DAILY_SUMMARY,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (perfectDay) {
            "🎉 Perfect day!"
        } else {
            "Daily Summary"
        }

        val content = if (perfectDay) {
            "You completed all $totalCount habits today!"
        } else {
            "You completed $completedCount out of $totalCount habits today."
        }

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_DAILY_SUMMARY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .build()
    }

    private fun createCompleteAction(instanceId: String, habitId: String): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE
            putExtra(NotificationActionReceiver.EXTRA_INSTANCE_ID, instanceId)
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            instanceId.hashCode(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            0, // No icon for now
            "Complete",
            pendingIntent
        ).build()
    }

    private fun createAddOneAction(instanceId: String, habitId: String): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_ADD_ONE
            putExtra(NotificationActionReceiver.EXTRA_INSTANCE_ID, instanceId)
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            instanceId.hashCode(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            0, // No icon for now
            "+1",
            pendingIntent
        ).build()
    }

    private fun createSnoozeAction(instanceId: String): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_INSTANCE_ID, instanceId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            instanceId.hashCode() + 1, // Different request code
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            0, // No icon for now
            "Snooze",
            pendingIntent
        ).build()
    }

    private fun createSkipAction(instanceId: String): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SKIP
            putExtra(NotificationActionReceiver.EXTRA_INSTANCE_ID, instanceId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            instanceId.hashCode() + 2, // Different request code
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            0, // No icon for now
            "Skip",
            pendingIntent
        ).build()
    }

    private fun getNotificationId(instanceId: String): Int {
        return NotificationChannels.NOTIFICATION_ID_HABIT_BASE + (instanceId.hashCode() % 1000)
    }

    private fun getGracePeriodNotificationId(instanceId: String): Int {
        return NotificationChannels.NOTIFICATION_ID_GRACE_BASE + (instanceId.hashCode() % 1000)
    }
}
