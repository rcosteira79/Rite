package com.ricardocosteira.habitlock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.AppModule
import com.ricardocosteira.habitlock.domain.models.CompletionSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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

        // Use goAsync to allow for coroutine work
        val pendingResult = goAsync()

        scope.launch {
            try {
                // Create AppModule to access use cases
                val driverFactory = DatabaseDriverFactory(context)
                val appModule = AppModule(driverFactory)

                when (action) {
                    ACTION_COMPLETE -> handleComplete(context, appModule, instanceId)
                    ACTION_ADD_ONE -> handleAddOne(context, appModule, instanceId)
                    ACTION_SNOOZE -> handleSnooze(context, appModule, instanceId)
                    ACTION_SKIP -> handleSkip(context, appModule, instanceId)
                }

                // Cancel the notification after action is taken
                val notificationManager = HabitNotificationManager(context)
                notificationManager.cancelAllNotificationsForInstance(instanceId)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleComplete(context: Context, appModule: AppModule, instanceId: String) {
        val completeHabitUseCase = appModule.provideCompleteHabitUseCase()
        val habitInstanceRepository = appModule.provideHabitInstanceRepository()

        val instance = habitInstanceRepository.getInstanceById(instanceId)
        if (instance != null) {
            completeHabitUseCase.executeBinary(instanceId, CompletionSource.NOTIFICATION)
        }
    }

    private suspend fun handleAddOne(context: Context, appModule: AppModule, instanceId: String) {
        val completeHabitUseCase = appModule.provideCompleteHabitUseCase()
        val habitInstanceRepository = appModule.provideHabitInstanceRepository()

        val instance = habitInstanceRepository.getInstanceById(instanceId)
        if (instance != null) {
            completeHabitUseCase.executeQuantitative(instanceId, 1, CompletionSource.NOTIFICATION)
        }
    }

    private suspend fun handleSnooze(context: Context, appModule: AppModule, instanceId: String) {
        val snoozeHabitUseCase = appModule.provideSnoozeHabitUseCase()
        
        // Snooze for 15 minutes by default
        val result = snoozeHabitUseCase.execute(instanceId, durationMinutes = 15)

        if (result.isSuccess) {
            val snoozeState = result.getOrNull()
            if (snoozeState != null) {
                // Reschedule notification for when snooze expires
                val habitInstanceRepository = appModule.provideHabitInstanceRepository()
                val habitRepository = appModule.provideHabitRepository()
                val instance = habitInstanceRepository.getInstanceById(instanceId)

                if (instance != null) {
                    val habit = habitRepository.getHabitById(instance.habitId)
                    if (habit != null) {
                        val notificationScheduler = NotificationScheduler(context)
                        notificationScheduler.scheduleSnoozeReminder(
                            instance,
                            habit,
                            snoozeState.scheduledTime.toEpochMilliseconds()
                        )
                    }
                }
            }
        }
    }

    private suspend fun handleSkip(context: Context, appModule: AppModule, instanceId: String) {
        val skipHabitUseCase = appModule.provideSkipHabitUseCase()
        skipHabitUseCase.execute(instanceId)
    }

    companion object {
        const val ACTION_COMPLETE = "com.ricardocosteira.habitlock.ACTION_COMPLETE"
        const val ACTION_ADD_ONE = "com.ricardocosteira.habitlock.ACTION_ADD_ONE"
        const val ACTION_SNOOZE = "com.ricardocosteira.habitlock.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.ricardocosteira.habitlock.ACTION_SKIP"

        const val EXTRA_INSTANCE_ID = "instance_id"
        const val EXTRA_HABIT_ID = "habit_id"
    }
}
