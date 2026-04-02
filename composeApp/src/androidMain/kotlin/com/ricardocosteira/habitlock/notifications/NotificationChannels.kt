package com.ricardocosteira.habitlock.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Manages notification channels for the app.
 * Android O+ requires notification channels to be created before sending notifications.
 */
object NotificationChannels {

    // Channel IDs
    const val CHANNEL_HABITS = "habit_reminders"
    const val CHANNEL_GRACE_PERIOD = "grace_period"
    const val CHANNEL_DAILY_SUMMARY = "daily_summary"
    const val CHANNEL_HABIT_TRACKING = "habit_tracking"

    // Notification IDs (used to update/cancel notifications)
    const val NOTIFICATION_ID_HABIT_BASE = 1000
    const val NOTIFICATION_ID_GRACE_BASE = 2000
    const val NOTIFICATION_ID_DAILY_SUMMARY = 3000
    const val NOTIFICATION_ID_TRACKING_SUMMARY = 4000
    const val NOTIFICATION_ID_TRACKING_BASE = 4100

    // Notification groups
    const val NOTIFICATION_GROUP_TRACKING = "com.ricardocosteira.habitlock.TRACKING"

    /**
     * Creates all notification channels required by the app.
     * Should be called when the app starts.
     *
     * @param context Application context
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            // Channel 1: Habit Reminders
            val habitChannel = NotificationChannel(
                CHANNEL_HABITS,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your daily and weekly habits"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            // Channel 2: Grace Period Notifications
            val gracePeriodChannel = NotificationChannel(
                CHANNEL_GRACE_PERIOD,
                "Grace Period Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Final reminders before habits are marked as failed"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            // Channel 3: Daily Summary (optional)
            val summaryChannel = NotificationChannel(
                CHANNEL_DAILY_SUMMARY,
                "Daily Summary",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "End of day summary of your habit progress"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }

            // Channel 4: Habit Tracking
            val trackingChannel = NotificationChannel(
                CHANNEL_HABIT_TRACKING,
                "Habit Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent notification showing tracked habit progress"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }

            // Create all channels
            notificationManager.createNotificationChannels(
                listOf(
                    habitChannel,
                    gracePeriodChannel,
                    summaryChannel,
                    trackingChannel
                )
            )
        }
    }

    /**
     * Deletes all notification channels.
     * Used for cleanup or testing.
     *
     * @param context Application context
     */
    fun deleteChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.deleteNotificationChannel(CHANNEL_HABITS)
            notificationManager.deleteNotificationChannel(CHANNEL_GRACE_PERIOD)
            notificationManager.deleteNotificationChannel(CHANNEL_DAILY_SUMMARY)
            notificationManager.deleteNotificationChannel(CHANNEL_HABIT_TRACKING)
        }
    }

    /**
     * Checks if notifications are enabled for the app.
     *
     * @param context Application context
     * @return true if notifications are enabled, false otherwise
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true // Assume enabled on older versions
        }
    }

    /**
     * Checks if a specific notification channel is enabled.
     *
     * @param context Application context
     * @param channelId The ID of the channel to check
     * @return true if the channel is enabled, false otherwise
     */
    fun isChannelEnabled(context: Context, channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return true // Assume enabled on older versions
    }
}
