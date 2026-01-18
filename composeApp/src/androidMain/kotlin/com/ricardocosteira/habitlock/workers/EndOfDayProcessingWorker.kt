package com.ricardocosteira.habitlock.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.AppModule

/**
 * Worker responsible for end-of-day processing.
 * Runs late in the evening (e.g., 11:30 PM) to:
 * 1. Send end-of-day notifications/reminders
 * 2. Handle grace period notifications
 * 3. Prepare for next day's generation
 * 
 * This worker is separate from DailyHabitGenerationWorker to allow for 
 * grace period notifications before habits are marked as FAILED.
 */
class EndOfDayProcessingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Create AppModule manually since we're not using a DI framework with Worker integration
            val driverFactory = DatabaseDriverFactory(applicationContext)
            val appModule = AppModule(driverFactory)

            // Get required repository and create notification scheduler
            val habitInstanceRepository = appModule.provideHabitInstanceRepository()
            val habitRepository = appModule.provideHabitRepository()
            val notificationScheduler = com.ricardocosteira.habitlock.notifications.NotificationScheduler(applicationContext)

            // Get all pending habits for today
            val pendingInstances = habitInstanceRepository.getTodayInstances()
                .filter { it.status == com.ricardocosteira.habitlock.domain.models.HabitStatus.PENDING }

            // Send grace period notifications for pending habits
            pendingInstances.forEach { instance ->
                val habit = habitRepository.getHabitById(instance.habitId)
                if (habit != null) {
                    notificationScheduler.scheduleGracePeriodNotification(instance, habit)
                }
            }

            Result.success()
        } catch (e: Exception) {
            // Log error and retry
            e.printStackTrace()
            Result.retry()
        }
    }
}
