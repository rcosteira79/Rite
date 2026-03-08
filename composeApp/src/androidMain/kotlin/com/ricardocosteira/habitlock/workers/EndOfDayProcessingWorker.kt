package com.ricardocosteira.habitlock.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.habitLockApplication
import com.ricardocosteira.habitlock.notifications.NotificationScheduler
import com.ricardocosteira.habitlock.util.todayIn
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

/**
 * Worker responsible for end-of-day processing.
 * Runs late in the evening (e.g., 11:30 PM) to:
 * 1. Send grace period notifications for still-pending habits
 * 2. Prepare for next day's generation
 */
class EndOfDayProcessingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val appComponent = applicationContext.habitLockApplication.appComponent
            val habitInstanceRepository = appComponent.habitInstanceRepository
            val habitRepository = appComponent.habitRepository
            val notificationScheduler = NotificationScheduler(applicationContext)

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val pendingInstances = habitInstanceRepository.getInstancesForDate(today)
                .filter { it.status == HabitStatus.PENDING }

            for (instance in pendingInstances) {
                val habit = habitRepository.getHabitById(instance.habitId)
                if (habit != null) {
                    notificationScheduler.scheduleGracePeriodNotification(instance, habit)
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

