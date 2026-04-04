package com.ricardocosteira.rite.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Initializer for WorkManager tasks.
 * Sets up periodic workers for:
 * - Daily habit generation (runs at start of day)
 * - End of day processing (runs late evening for grace period notifications)
 */
object WorkManagerInitializer {

    private const val DAILY_GENERATION_WORK_NAME = "daily_habit_generation"
    private const val END_OF_DAY_WORK_NAME = "end_of_day_processing"

    /**
     * Schedules all periodic workers for the app.
     * Should be called when the app starts.
     * 
     * @param context Application context
     */
    fun initialize(context: Context) {
        val workManager = WorkManager.getInstance(context)

        scheduleDailyHabitGeneration(workManager)
        scheduleEndOfDayProcessing(workManager)
    }

    /**
     * Schedules the daily habit generation worker.
     * Runs once per day at the start of the day (configured by user timezone).
     * 
     * The worker:
     * 1. Marks yesterday's pending habits as FAILED
     * 2. Generates today's habit instances
     * 
     * We use a 24-hour periodic work with initial delay calculated to run at
     * the start of the next day (e.g., midnight or user-configured day start time).
     */
    private fun scheduleDailyHabitGeneration(workManager: WorkManager) {
        // Calculate initial delay to run at start of next day
        val currentTime = Calendar.getInstance()
        val nextRunTime = Calendar.getInstance().apply {
            // Set to next day at configured hour (default 00:00)
            // TODO: Read user's preferred day start time from settings
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If time has passed today, move to tomorrow
            if (timeInMillis <= currentTime.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelayMillis = nextRunTime.timeInMillis - currentTime.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<DailyHabitGenerationWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false) // Run even on low battery
                    .build()
            )
            .build()

        // Use REPLACE policy to update the work if it already exists
        workManager.enqueueUniquePeriodicWork(
            DAILY_GENERATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Schedules the end-of-day processing worker.
     * Runs once per day late in the evening (e.g., 10:00 PM).
     * 
     * The worker:
     * - Sends grace period notifications for pending habits
     * - Prepares for next day's processing
     * 
     * This runs separately from daily generation to allow a grace period
     * between the last reminder and when habits are marked as failed.
     */
    private fun scheduleEndOfDayProcessing(workManager: WorkManager) {
        // Calculate initial delay to run at end of day (10:00 PM)
        val currentTime = Calendar.getInstance()
        val nextRunTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22) // 10:00 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If time has passed today, move to tomorrow
            if (timeInMillis <= currentTime.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelayMillis = nextRunTime.timeInMillis - currentTime.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<EndOfDayProcessingWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false) // Run even on low battery
                    .build()
            )
            .build()

        // Use REPLACE policy to update the work if it already exists
        workManager.enqueueUniquePeriodicWork(
            END_OF_DAY_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancels all scheduled workers.
     * Useful for testing or when user disables the app.
     */
    fun cancelAllWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(DAILY_GENERATION_WORK_NAME)
        workManager.cancelUniqueWork(END_OF_DAY_WORK_NAME)
    }
}
