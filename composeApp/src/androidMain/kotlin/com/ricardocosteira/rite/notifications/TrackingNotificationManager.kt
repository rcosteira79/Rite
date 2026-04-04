package com.ricardocosteira.rite.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ricardocosteira.rite.MainActivity
import com.ricardocosteira.rite.R
import com.ricardocosteira.rite.domain.models.HabitType

/**
 * Manages the persistent grouped tracking notification.
 * Each tracked habit is shown as a child notification inside a notification group.
 * A summary notification acts as the group parent.
 *
 * Binary habits show: name + "Not completed" + "Complete" action
 * Quantitative habits show: name + progress (e.g. "3 / 10 glasses") + "+N" and "Undo" actions
 * Completed habits are removed from the group automatically.
 * When all habits are complete, the entire group is dismissed.
 */
class TrackingNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(
        Context.NOTIFICATION_SERVICE
    ) as NotificationManager

    /**
     * Shows or updates child notifications for all tracked habits plus the group summary.
     * Habits that are already completed are skipped (removed via [removeHabit]).
     *
     * @param trackedHabits The list of habits currently being tracked today.
     */
    fun update(trackedHabits: List<TrackedHabitInfo>) {
        val pendingHabits = trackedHabits.filter { !it.isCompleted }

        if (pendingHabits.isEmpty()) {
            hideAll(trackedHabits.map { it.instanceId })
            return
        }

        pendingHabits.forEach { trackedHabit ->
            val notification = buildChildNotification(trackedHabit)
            val notificationId = childNotificationId(trackedHabit.instanceId)
            notificationManager.notify(notificationId, notification)
        }

        val completedInstanceIds = trackedHabits
            .filter { it.isCompleted }
            .map { it.instanceId }
        completedInstanceIds.forEach { instanceId -> removeHabit(instanceId) }

        notificationManager.notify(
            NotificationChannels.NOTIFICATION_ID_TRACKING_SUMMARY,
            buildSummaryNotification(pendingHabits.size)
        )
    }

    /**
     * Cancels only the summary notification, leaving children intact.
     * Use when temporarily hiding the group header.
     */
    fun hide() {
        notificationManager.cancel(NotificationChannels.NOTIFICATION_ID_TRACKING_SUMMARY)
    }

    /**
     * Cancels all child notifications for the given instance IDs plus the summary.
     *
     * @param trackedInstanceIds All instance IDs whose child notifications should be cancelled.
     */
    fun hideAll(trackedInstanceIds: List<String>) {
        trackedInstanceIds.forEach { instanceId ->
            notificationManager.cancel(childNotificationId(instanceId))
        }
        notificationManager.cancel(NotificationChannels.NOTIFICATION_ID_TRACKING_SUMMARY)
    }

    /**
     * Cancels the child notification for a single habit instance.
     *
     * @param instanceId The habit instance ID whose notification should be removed.
     */
    fun removeHabit(instanceId: String) {
        notificationManager.cancel(childNotificationId(instanceId))
    }

    // Private helpers

    private fun buildChildNotification(trackedHabit: TrackedHabitInfo): android.app.Notification {
        val contentText: String = when (trackedHabit.type) {
            HabitType.BINARY -> context.getString(R.string.tracking_child_not_completed)

            HabitType.QUANTITATIVE -> {
                val target: Int = trackedHabit.targetValue ?: 0
                val unit: String = trackedHabit.unit?.let { " $it" } ?: ""
                context.getString(
                    R.string.tracking_child_progress,
                    trackedHabit.currentProgress,
                    target,
                    unit
                )
            }
        }

        val builder = NotificationCompat.Builder(
            context,
            NotificationChannels.CHANNEL_HABIT_TRACKING
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(trackedHabit.habitName)
            .setContentText(contentText)
            .setGroup(NotificationChannels.NOTIFICATION_GROUP_TRACKING)
            .setOngoing(true)
            .setContentIntent(buildOpenAppPendingIntent(trackedHabit.instanceId))

        when (trackedHabit.type) {
            HabitType.BINARY -> builder.addAction(buildCompleteAction(trackedHabit.instanceId))

            HabitType.QUANTITATIVE -> {
                builder.addAction(
                    buildIncrementAction(trackedHabit.instanceId, trackedHabit.defaultIncrement)
                )
                builder.addAction(buildUndoAction(trackedHabit.instanceId))
            }
        }

        return builder.build()
    }

    private fun buildSummaryNotification(pendingCount: Int): android.app.Notification {
        val contentText: String = context.resources.getQuantityString(
            R.plurals.tracking_summary_text,
            pendingCount,
            pendingCount
        )

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_HABIT_TRACKING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.tracking_summary_title))
            .setContentText(contentText)
            .setGroup(NotificationChannels.NOTIFICATION_GROUP_TRACKING)
            .setGroupSummary(true)
            .setOngoing(true)
            .setContentIntent(buildOpenAppPendingIntent(instanceId = null))
            .build()
    }

    private fun buildOpenAppPendingIntent(instanceId: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val requestCode = instanceId?.hashCode() ?: 0
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildCompleteAction(instanceId: String): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE
            putExtra(NotificationActionReceiver.EXTRA_INSTANCE_ID, instanceId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "tracking_complete_$instanceId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val label: String = context.getString(R.string.tracking_action_complete)
        return NotificationCompat.Action.Builder(0, label, pendingIntent).build()
    }

    private fun buildIncrementAction(
        instanceId: String,
        defaultIncrement: Int
    ): NotificationCompat.Action {
        val label: String = context.getString(R.string.tracking_action_increment, defaultIncrement)
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_ADD_ONE
            putExtra(NotificationActionReceiver.EXTRA_INSTANCE_ID, instanceId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "tracking_increment_$instanceId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(0, label, pendingIntent).build()
    }

    private fun buildUndoAction(instanceId: String): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_UNDO_LAST_INCREMENT
            putExtra(NotificationActionReceiver.EXTRA_INSTANCE_ID, instanceId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "tracking_undo_$instanceId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val label: String = context.getString(R.string.tracking_action_undo)
        return NotificationCompat.Action.Builder(0, label, pendingIntent).build()
    }

    private fun childNotificationId(instanceId: String): Int =
        NotificationChannels.NOTIFICATION_ID_TRACKING_BASE + (instanceId.hashCode() % 1000)
}
