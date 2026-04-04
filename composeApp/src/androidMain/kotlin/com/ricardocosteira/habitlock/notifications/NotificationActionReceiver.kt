package com.ricardocosteira.habitlock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ricardocosteira.habitlock.di.HabitLockAppComponent
import com.ricardocosteira.habitlock.domain.models.CompletionSource
import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.habitlock.domain.repositories.HabitRepository
import com.ricardocosteira.habitlock.habitLockApplication
import com.ricardocosteira.habitlock.util.todayIn
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * BroadcastReceiver that handles notification action button clicks.
 * Handles:
 * - Complete action (for binary habits)
 * - +1 action (for quantitative habits)
 * - Snooze action
 * - Skip action
 */
class NotificationActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val instanceId = intent.getStringExtra(EXTRA_INSTANCE_ID)

        if (action == null || instanceId == null) {
            return
        }

        val pendingResult = goAsync()

        scope.launch {
            try {
                val appComponent = context.habitLockApplication.appComponent

                when (action) {
                    ACTION_COMPLETE -> handleComplete(appComponent, instanceId)
                    ACTION_ADD_ONE -> handleAddOne(appComponent, instanceId)
                    ACTION_SNOOZE -> handleSnooze(context, appComponent, instanceId)
                    ACTION_SKIP -> handleSkip(appComponent, instanceId)
                    ACTION_UNDO_LAST_INCREMENT -> handleUndoLastIncrement(appComponent, instanceId)
                }

                refreshTrackingNotification(appComponent)
                HabitNotificationManager(context).cancelAllNotificationsForInstance(instanceId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleComplete(appComponent: HabitLockAppComponent, instanceId: String) {
        val instance = appComponent.habitInstanceRepository.getInstanceById(instanceId)
        if (instance != null) {
            appComponent.completeHabit.executeBinary(instanceId, CompletionSource.NOTIFICATION)
        }
    }

    private suspend fun handleAddOne(appComponent: HabitLockAppComponent, instanceId: String) {
        val instance = appComponent.habitInstanceRepository.getInstanceById(instanceId)
        if (instance != null) {
            appComponent.completeHabit.executeQuantitative(
                instanceId,
                deltaValue = 1,
                source = CompletionSource.NOTIFICATION
            )
        }
    }

    private suspend fun handleSnooze(
        context: Context,
        appComponent: HabitLockAppComponent,
        instanceId: String
    ) {
        val result = appComponent.snoozeHabit.execute(instanceId, durationMinutes = 15)
        if (result.isSuccess) {
            val snoozeState = result.getOrNull()
            if (snoozeState != null) {
                val instance = appComponent.habitInstanceRepository.getInstanceById(instanceId)
                val habit = instance?.let { appComponent.habitRepository.getHabitById(it.habitId) }
                if (instance != null && habit != null) {
                    NotificationScheduler(context).scheduleSnoozeReminder(
                        instance,
                        habit,
                        snoozeState.scheduledTime.toEpochMilliseconds()
                    )
                }
            }
        }
    }

    private suspend fun handleSkip(appComponent: HabitLockAppComponent, instanceId: String) {
        appComponent.skipHabit.execute(instanceId)
    }

    private suspend fun handleUndoLastIncrement(
        appComponent: HabitLockAppComponent,
        instanceId: String
    ) {
        appComponent.undoLastIncrement.execute(instanceId)
    }

    private suspend fun refreshTrackingNotification(appComponent: HabitLockAppComponent) {
        val habitNotification: HabitNotification = appComponent.habitNotificationAccessor
        val habitRepository: HabitRepository = appComponent.habitRepository
        val habitInstanceRepository: HabitInstanceRepository = appComponent.habitInstanceRepository
        val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val trackedHabits: List<Habit> = habitRepository.getHabitsWithTrackingEnabled()
        val trackedInfoList: List<TrackedHabitInfo> = trackedHabits.mapNotNull { habit: Habit ->
            val instance: HabitInstance =
                habitInstanceRepository.getInstanceForHabitAndDate(habit.id, today)
                    ?: return@mapNotNull null
            TrackedHabitInfo(
                instanceId = instance.id,
                habitId = habit.id,
                habitName = habit.name,
                type = habit.type,
                currentProgress = instance.currentProgress,
                targetValue = instance.targetValue,
                unit = habit.unit,
                defaultIncrement = habit.defaultIncrement,
                isCompleted = instance.status == HabitStatus.COMPLETED
            )
        }
        if (trackedInfoList.isNotEmpty()) {
            habitNotification.updateTrackingNotification(trackedInfoList)
        } else {
            habitNotification.hideTrackingNotification()
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.ricardocosteira.habitlock.ACTION_COMPLETE"
        const val ACTION_ADD_ONE = "com.ricardocosteira.habitlock.ACTION_ADD_ONE"
        const val ACTION_SNOOZE = "com.ricardocosteira.habitlock.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.ricardocosteira.habitlock.ACTION_SKIP"
        const val ACTION_UNDO_LAST_INCREMENT = "com.ricardocosteira.habitlock.ACTION_UNDO_LAST_INCREMENT"

        const val EXTRA_INSTANCE_ID = "instance_id"
        const val EXTRA_HABIT_ID = "habit_id"
    }
}
