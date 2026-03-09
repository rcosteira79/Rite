package com.ricardocosteira.habitlock.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ricardocosteira.habitlock.habitLockApplication

/**
 * Worker responsible for generating daily habit instances and processing end-of-day logic.
 * Runs at the start of each day (configured time) to:
 * 1. Mark yesterday's pending habits as FAILED (via ProcessEndOfDay)
 * 2. Generate today's habit instances (via GenerateDailyHabits)
 */
class DailyHabitGenerationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val appComponent = applicationContext.habitLockApplication.appComponent
            appComponent.processEndOfDay.execute()
            appComponent.generateDailyHabits.execute()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
