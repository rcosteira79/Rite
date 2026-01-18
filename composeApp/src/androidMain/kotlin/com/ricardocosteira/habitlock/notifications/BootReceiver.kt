package com.ricardocosteira.habitlock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ricardocosteira.habitlock.workers.WorkManagerInitializer

/**
 * BroadcastReceiver that handles device boot completion.
 * Reschedules all workers and notifications after device restart.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reinitialize WorkManager after boot
            WorkManagerInitializer.initialize(context)

            // Recreate notification channels
            NotificationChannels.createChannels(context)

            // TODO: Reschedule all habit notifications
            // This would require iterating through all active habits and rescheduling their reminders
            // Implementation will be added when we integrate with the repository layer
        }
    }
}
