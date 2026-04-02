package com.ricardocosteira.habitlock.notifications

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.HabitType

expect class HabitNotification {
    fun scheduleReminder(habit: Habit, reminder: HabitReminder, instance: HabitInstance)
    fun cancelReminder(instanceId: String)
    fun cancelAllForHabit(habitId: String, instanceIds: List<String>)
    fun updateTrackingNotification(trackedHabits: List<TrackedHabitInfo>)
    fun hideTrackingNotification()
    fun isNotificationPermissionGranted(): Boolean
    fun openNotificationSettings()
}

data class TrackedHabitInfo(
    val instanceId: String,
    val habitId: String,
    val habitName: String,
    val type: HabitType,
    val currentProgress: Int,
    val targetValue: Int?,
    val unit: String?,
    val defaultIncrement: Int,
    val isCompleted: Boolean
)
