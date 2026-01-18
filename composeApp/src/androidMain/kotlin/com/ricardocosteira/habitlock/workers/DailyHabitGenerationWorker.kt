package com.ricardocosteira.habitlock.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ricardocosteira.habitlock.data.DatabaseDriverFactory
import com.ricardocosteira.habitlock.di.AppModule

/**
 * Worker responsible for generating daily habit instances and processing end-of-day logic.
 * Runs at the start of each day (configured time) to:
 * 1. Mark yesterday's pending habits as FAILED (via ProcessEndOfDayUseCase)
 * 2. Generate today's habit instances (via GenerateDailyHabitsUseCase)
 */
class DailyHabitGenerationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Create AppModule manually since we're not using a DI framework with Worker integration
            val driverFactory = DatabaseDriverFactory(applicationContext)
            val appModule = AppModule(driverFactory)

            // Get required use cases
            val processEndOfDayUseCase = appModule.provideProcessEndOfDayUseCase()
            val generateDailyHabitsUseCase = appModule.provideGenerateDailyHabitsUseCase()

            // Step 1: Mark yesterday's pending habits as FAILED
            processEndOfDayUseCase.execute()

            // Step 2: Generate today's habit instances
            generateDailyHabitsUseCase.execute()

            Result.success()
        } catch (e: Exception) {
            // Log error and retry
            e.printStackTrace()
            Result.retry()
        }
    }
}
