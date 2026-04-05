package com.ricardocosteira.rite.notifications

import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitReminder
import com.ricardocosteira.rite.domain.models.HabitType

expect class HabitNotification {
    fun scheduleReminder(habit: Habit, reminder: HabitReminder, instance: HabitInstance)
    fun cancelReminder(instanceId: String)
    fun cancelReminder(instanceId: String, reminder: HabitReminder?)
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
