package com.ricardocosteira.habitlock.notifications

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitReminder

actual class HabitNotification {
    actual fun scheduleReminder(habit: Habit, reminder: HabitReminder, instance: HabitInstance) {}
    actual fun cancelReminder(instanceId: String) {}
    actual fun cancelAllForHabit(habitId: String, instanceIds: List<String>) {}
    actual fun updateTrackingNotification(trackedHabits: List<TrackedHabitInfo>) {}
    actual fun hideTrackingNotification() {}
    actual fun isNotificationPermissionGranted(): Boolean = true
    actual fun openNotificationSettings() {}
}
