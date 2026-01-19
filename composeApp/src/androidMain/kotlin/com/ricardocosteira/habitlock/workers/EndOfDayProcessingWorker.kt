package com.ricardocosteira.habitlock.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.AppModule
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.notifications.NotificationScheduler
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

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
            val notificationScheduler = NotificationScheduler(applicationContext)

            // Get all pending habits for today
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val pendingInstances = habitInstanceRepository.getInstancesForDate(today)
                .filter { it.status == HabitStatus.PENDING }

            // Send grace period notifications for pending habits
            for (instance in pendingInstances) {
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

private fun Clock.System.todayIn(timezone: TimeZone): LocalDate = now().toLocalDateTime(timezone).date
