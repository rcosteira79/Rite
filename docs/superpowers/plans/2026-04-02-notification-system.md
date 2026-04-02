# Notification System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix broken reminder scheduling and add persistent tracking notifications for habits.

**Architecture:** Expect/actual `HabitNotification` interface in commonMain, with Android actual wrapping existing `NotificationScheduler` + `HabitNotificationManager` + new `TrackingNotificationManager`. Wires into `HabitFormViewModel` (schedule on save), `TodayViewModel` (update tracking), `DailyHabitGenerationWorker` (schedule on generation), and `BootReceiver` (reschedule on boot).

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, SQLDelight, kotlin-inject, Android NotificationCompat, AlarmManager

---

## File Structure

### New Files
| File | Responsibility |
|------|---------------|
| `commonMain/.../notifications/HabitNotification.kt` | Expect declaration — common notification interface |
| `androidMain/.../notifications/AndroidHabitNotification.kt` | Actual implementation — delegates to existing Android classes |
| `androidMain/.../notifications/TrackingNotificationManager.kt` | Persistent grouped notification for tracked habits |
| `iosMain/.../notifications/HabitNotification.ios.kt` | No-op actual stub |
| `commonMain/composeResources/values/strings_notifications.xml` | String resources for notification UI |

### Modified Files
| File | Change |
|------|--------|
| `commonMain/.../domain/models/Habit.kt` | Add `isTrackingEnabled` field |
| `commonMain/.../data/mappers/EntityMappers.kt` | Map `isTrackingEnabled` |
| `commonMain/sqldelight/.../HabitLock.sq` | Add column + update queries |
| `commonMain/.../data/repositories/HabitRepositoryImpl.kt` | Persist `isTrackingEnabled` |
| `commonMain/.../domain/usecases/CreateHabit.kt` | Accept `isTrackingEnabled` param |
| `commonMain/.../presentation/ui/habit/HabitFormState.kt` | Add `isTrackingEnabled`, `isNotificationPermissionGranted` |
| `commonMain/.../presentation/ui/habit/HabitFormUiAction.kt` | Add `IsTrackingEnabledChanged` |
| `commonMain/.../presentation/ui/habit/HabitFormViewModel.kt` | Add `HabitNotification` dep, schedule on save |
| `commonMain/.../presentation/ui/habit/HabitFormScreen.kt` | Add tracking row, contextual messages, permission state |
| `commonMain/.../di/HabitLockAppComponent.kt` | Provide `HabitNotification` |
| `commonMain/.../presentation/ui/today/TodayViewModel.kt` | Update tracking notification on actions |
| `androidMain/.../notifications/NotificationChannels.kt` | Add `CHANNEL_HABIT_TRACKING` |
| `androidMain/.../notifications/BootReceiver.kt` | Implement reschedule-all |
| `androidMain/.../workers/DailyHabitGenerationWorker.kt` | Schedule reminders + tracking after generation |
| `androidMain/.../notifications/NotificationActionReceiver.kt` | Update tracking notification after actions |
| `androidMain/.../MainActivity.kt` | Request POST_NOTIFICATIONS permission |

---

### Task 1: Add `isTrackingEnabled` to Domain Model and Database

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/Habit.kt:8-24`
- Modify: `composeApp/src/commonMain/sqldelight/com/ricardocosteira/habitlock/data/database/HabitLock.sq:15-32,152-166`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/mappers/EntityMappers.kt:50-66`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/repositories/HabitRepositoryImpl.kt`

- [ ] **Step 1: Add `isTrackingEnabled` field to `Habit` domain model**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/Habit.kt`, add the new field after `defaultIncrement`:

```kotlin
data class Habit(
    val id: String,
    val name: String,
    val description: String?,
    val type: HabitType,
    val targetValue: Int?,
    val unit: String?,
    val defaultIncrement: Int = 1,
    val isTrackingEnabled: Boolean = false,
    val isActive: Boolean,
    val isArchived: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val expectedCompletions: Int,
    val createdAt: Instant,
    val archivedAt: Instant?
)
```

- [ ] **Step 2: Add column to database schema**

In `composeApp/src/commonMain/sqldelight/com/ricardocosteira/habitlock/data/database/HabitLock.sq`, add `isTrackingEnabled` column to the Habit table after `defaultIncrement`:

```sql
CREATE TABLE Habit (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    type TEXT NOT NULL DEFAULT 'BINARY',
    targetValue INTEGER,
    unit TEXT,
    defaultIncrement INTEGER NOT NULL DEFAULT 1,
    isTrackingEnabled INTEGER NOT NULL DEFAULT 0,
    isActive INTEGER NOT NULL DEFAULT 1,
    isArchived INTEGER NOT NULL DEFAULT 0,
    currentStreak INTEGER NOT NULL DEFAULT 0,
    longestStreak INTEGER NOT NULL DEFAULT 0,
    totalCompletions INTEGER NOT NULL DEFAULT 0,
    expectedCompletions INTEGER NOT NULL DEFAULT 0,
    createdAt TEXT NOT NULL,
    archivedAt TEXT
);
```

- [ ] **Step 3: Update `insertHabit` query**

In the same file, update the `insertHabit` query to include the new column:

```sql
insertHabit:
INSERT INTO Habit (id, name, description, type, targetValue, unit, defaultIncrement, isTrackingEnabled, isActive, isArchived, currentStreak, longestStreak, totalCompletions, expectedCompletions, createdAt, archivedAt)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
```

- [ ] **Step 4: Update `updateHabit` query**

In the same file, add `isTrackingEnabled` to the `updateHabit` query:

```sql
updateHabit:
UPDATE Habit SET
    name = ?,
    description = ?,
    type = ?,
    targetValue = ?,
    unit = ?,
    isTrackingEnabled = ?,
    isActive = ?,
    isArchived = ?,
    archivedAt = ?
WHERE id = ?;
```

- [ ] **Step 5: Add a query to get habits with tracking enabled**

In the same file, add a new query after `getHabitById`:

```sql
getHabitsWithTrackingEnabled:
SELECT * FROM Habit WHERE isTrackingEnabled = 1 AND isActive = 1 AND isArchived = 0;
```

- [ ] **Step 6: Update entity mapper**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/mappers/EntityMappers.kt`, add `isTrackingEnabled` mapping in the `DbHabit.toDomain()` function:

```kotlin
fun DbHabit.toDomain(): Habit = Habit(
    id = id,
    name = name,
    description = description,
    type = HabitType.valueOf(type),
    targetValue = targetValue?.toInt(),
    unit = unit,
    defaultIncrement = defaultIncrement.toInt(),
    isTrackingEnabled = isTrackingEnabled == 1L,
    isActive = isActive == 1L,
    isArchived = isArchived == 1L,
    currentStreak = currentStreak.toInt(),
    longestStreak = longestStreak.toInt(),
    totalCompletions = totalCompletions.toInt(),
    expectedCompletions = expectedCompletions.toInt(),
    createdAt = Instant.parse(createdAt),
    archivedAt = archivedAt?.let { Instant.parse(it) }
)
```

- [ ] **Step 7: Update `HabitRepositoryImpl.createHabit()`**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/repositories/HabitRepositoryImpl.kt`, the `insertHabit` call must now include `isTrackingEnabled`. Find the `queries.insertHabit(...)` call inside `createHabit()` and add `if (habit.isTrackingEnabled) 1L else 0L` in the correct parameter position (after `habit.defaultIncrement.toLong()`, before `if (habit.isActive) 1L else 0L`).

- [ ] **Step 8: Update `HabitRepositoryImpl.updateHabit()`**

The `updateHabit` query now takes an extra parameter. Update the `queries.updateHabit(...)` call to include `if (habit.isTrackingEnabled) 1L else 0L` in the correct position (after `habit.unit`, before `if (habit.isActive) 1L else 0L`).

- [ ] **Step 9: Add `getHabitsWithTrackingEnabled()` to `HabitRepository` interface**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/repositories/HabitRepository.kt`, add:

```kotlin
/**
 * Get all active, non-archived habits with tracking notifications enabled.
 */
suspend fun getHabitsWithTrackingEnabled(): List<Habit>
```

- [ ] **Step 10: Implement `getHabitsWithTrackingEnabled()` in `HabitRepositoryImpl`**

In `HabitRepositoryImpl.kt`, add:

```kotlin
override suspend fun getHabitsWithTrackingEnabled(): List<Habit> = withContext(ioDispatcher) {
    queries.getHabitsWithTrackingEnabled().executeAsList().map { it.toDomain() }
}
```

- [ ] **Step 11: Update `CreateHabit` use case**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/usecases/CreateHabit.kt`, add `isTrackingEnabled` to `CreateHabitParams`:

```kotlin
data class CreateHabitParams(
    val name: String,
    val description: String?,
    val type: HabitType,
    val targetValue: Int?,
    val unit: String?,
    val defaultIncrement: Int = 1,
    val isTrackingEnabled: Boolean = false,
    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val quota: Int = 1,
    val weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
    val specificDays: Set<DayOfWeek>? = null,
    val reminder: HabitReminder?
)
```

Also update the `Habit(...)` constructor call inside `execute()` to pass `isTrackingEnabled = params.isTrackingEnabled`.

- [ ] **Step 12: Build the project to verify schema and model changes compile**

Run: `./gradlew composeApp:compileKotlinDesktop`

Expected: BUILD SUCCESSFUL (desktop target compiles fastest and validates commonMain code)

- [ ] **Step 13: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/Habit.kt \
       composeApp/src/commonMain/sqldelight/com/ricardocosteira/habitlock/data/database/HabitLock.sq \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/mappers/EntityMappers.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/repositories/HabitRepositoryImpl.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/repositories/HabitRepository.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/usecases/CreateHabit.kt
git commit -m "feat: add isTrackingEnabled to Habit model and database schema"
```

---

### Task 2: Create `HabitNotification` Expect/Actual Interface

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/notifications/HabitNotification.kt`
- Create: `composeApp/src/iosMain/kotlin/com/ricardocosteira/habitlock/notifications/HabitNotification.ios.kt`

- [ ] **Step 1: Create the expect declaration in commonMain**

Create `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/notifications/HabitNotification.kt`:

```kotlin
package com.ricardocosteira.habitlock.notifications

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitReminder

/**
 * Platform abstraction for notification operations.
 * Android provides real scheduling/display; iOS is a no-op stub.
 */
expect class HabitNotification {

    /**
     * Schedule a reminder notification for a habit instance.
     */
    fun scheduleReminder(habit: Habit, reminder: HabitReminder, instance: HabitInstance)

    /**
     * Cancel all scheduled reminders for a specific instance.
     */
    fun cancelReminder(instanceId: String)

    /**
     * Cancel all notifications (reminders + tracking) for a habit.
     */
    fun cancelAllForHabit(habitId: String, instanceIds: List<String>)

    /**
     * Show or update the persistent tracking notification with current habit states.
     */
    fun updateTrackingNotification(trackedHabits: List<TrackedHabitInfo>)

    /**
     * Hide the persistent tracking notification entirely.
     */
    fun hideTrackingNotification()

    /**
     * Whether the app has notification permission.
     */
    fun isNotificationPermissionGranted(): Boolean

    /**
     * Open the system notification settings for this app.
     */
    fun openNotificationSettings()
}

/**
 * Snapshot of a tracked habit's state for the tracking notification.
 */
data class TrackedHabitInfo(
    val instanceId: String,
    val habitId: String,
    val habitName: String,
    val type: com.ricardocosteira.habitlock.domain.models.HabitType,
    val currentProgress: Int,
    val targetValue: Int?,
    val unit: String?,
    val defaultIncrement: Int,
    val isCompleted: Boolean
)
```

- [ ] **Step 2: Create the iOS no-op actual**

Create `composeApp/src/iosMain/kotlin/com/ricardocosteira/habitlock/notifications/HabitNotification.ios.kt`:

```kotlin
package com.ricardocosteira.habitlock.notifications

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitReminder

actual class HabitNotification {

    actual fun scheduleReminder(habit: Habit, reminder: HabitReminder, instance: HabitInstance) {
        // No-op on iOS
    }

    actual fun cancelReminder(instanceId: String) {
        // No-op on iOS
    }

    actual fun cancelAllForHabit(habitId: String, instanceIds: List<String>) {
        // No-op on iOS
    }

    actual fun updateTrackingNotification(trackedHabits: List<TrackedHabitInfo>) {
        // No-op on iOS
    }

    actual fun hideTrackingNotification() {
        // No-op on iOS
    }

    actual fun isNotificationPermissionGranted(): Boolean = true

    actual fun openNotificationSettings() {
        // No-op on iOS
    }
}
```

- [ ] **Step 3: Build to verify expect/actual compiles**

Run: `./gradlew composeApp:compileKotlinDesktop`

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/notifications/HabitNotification.kt \
       composeApp/src/iosMain/kotlin/com/ricardocosteira/habitlock/notifications/HabitNotification.ios.kt
git commit -m "feat: add HabitNotification expect/actual interface"
```

---

### Task 3: Add Tracking Notification Channel + Android `HabitNotification` Actual

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/NotificationChannels.kt`
- Create: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/TrackingNotificationManager.kt`
- Create: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/AndroidHabitNotification.kt`

- [ ] **Step 1: Add `CHANNEL_HABIT_TRACKING` to `NotificationChannels`**

In `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/NotificationChannels.kt`, add channel ID and notification ID constants:

```kotlin
const val CHANNEL_HABIT_TRACKING = "habit_tracking"
const val NOTIFICATION_ID_TRACKING_SUMMARY = 4000
const val NOTIFICATION_ID_TRACKING_BASE = 4100
const val NOTIFICATION_GROUP_TRACKING = "com.ricardocosteira.habitlock.TRACKING"
```

In the `createChannels()` function, add the new channel to the list:

```kotlin
val trackingChannel = NotificationChannel(
    CHANNEL_HABIT_TRACKING,
    "Habit Tracking",
    NotificationManager.IMPORTANCE_LOW
).apply {
    description = "Persistent notification showing tracked habit progress"
    enableLights(false)
    enableVibration(false)
    setShowBadge(false)
}
```

Add `trackingChannel` to the `createNotificationChannels(listOf(...))` call.

Also add it to `deleteChannels()`:

```kotlin
notificationManager.deleteNotificationChannel(CHANNEL_HABIT_TRACKING)
```

- [ ] **Step 2: Create `TrackingNotificationManager`**

Create `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/TrackingNotificationManager.kt`:

```kotlin
package com.ricardocosteira.habitlock.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ricardocosteira.habitlock.MainActivity
import com.ricardocosteira.habitlock.R
import com.ricardocosteira.habitlock.domain.models.HabitType

/**
 * Manages the persistent grouped tracking notification.
 * Each tracked habit appears as a child notification in the group.
 */
class TrackingNotificationManager(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun update(trackedHabits: List<TrackedHabitInfo>) {
        val activeHabits: List<TrackedHabitInfo> = trackedHabits.filter { !it.isCompleted }

        if (activeHabits.isEmpty()) {
            hide()
            return
        }

        activeHabits.forEach { habit ->
            val childNotification = buildChildNotification(habit)
            val childId: Int = getChildNotificationId(habit.instanceId)
            notificationManager.notify(childId, childNotification)
        }

        // Cancel notifications for completed habits
        val completedHabits: List<TrackedHabitInfo> = trackedHabits.filter { it.isCompleted }
        completedHabits.forEach { habit ->
            notificationManager.cancel(getChildNotificationId(habit.instanceId))
        }

        val summaryNotification = buildSummaryNotification(activeHabits.size)
        notificationManager.notify(
            NotificationChannels.NOTIFICATION_ID_TRACKING_SUMMARY,
            summaryNotification
        )
    }

    fun hide() {
        // Cancel summary notification — child notifications are cancelled individually
        notificationManager.cancel(NotificationChannels.NOTIFICATION_ID_TRACKING_SUMMARY)
    }

    fun hideAll(trackedInstanceIds: List<String>) {
        trackedInstanceIds.forEach { instanceId ->
            notificationManager.cancel(getChildNotificationId(instanceId))
        }
        notificationManager.cancel(NotificationChannels.NOTIFICATION_ID_TRACKING_SUMMARY)
    }

    fun removeHabit(instanceId: String) {
        notificationManager.cancel(getChildNotificationId(instanceId))
    }

    private fun buildChildNotification(habit: TrackedHabitInfo): android.app.Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            habit.instanceId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val content: String = when (habit.type) {
            HabitType.BINARY -> "Not completed"
            HabitType.QUANTITATIVE -> {
                val target: String = habit.targetValue?.toString() ?: "?"
                val unitSuffix: String = habit.unit?.let { " $it" } ?: ""
                "${habit.currentProgress} / $target$unitSuffix"
            }
        }

        val builder: NotificationCompat.Builder = NotificationCompat
            .Builder(context, NotificationChannels.CHANNEL_HABIT_TRACKING)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(habit.habitName)
            .setContentText(content)
            .setOngoing(true)
            .setGroup(NotificationChannels.NOTIFICATION_GROUP_TRACKING)
            .setContentIntent(openAppPendingIntent)

        when (habit.type) {
            HabitType.BINARY -> {
                builder.addAction(createTrackingCompleteAction(habit.instanceId, habit.habitId))
            }
            HabitType.QUANTITATIVE -> {
                builder.addAction(
                    createTrackingIncrementAction(
                        habit.instanceId,
                        habit.habitId,
                        habit.defaultIncrement
                    )
                )
                builder.addAction(createTrackingUndoAction(habit.instanceId, habit.habitId))
            }
        }

        return builder.build()
    }

    private fun buildSummaryNotification(activeCount: Int): android.app.Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            NotificationChannels.NOTIFICATION_ID_TRACKING_SUMMARY,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat
            .Builder(context, NotificationChannels.CHANNEL_HABIT_TRACKING)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Tracking $activeCount habits")
            .setOngoing(true)
            .setGroup(NotificationChannels.NOTIFICATION_GROUP_TRACKING)
            .setGroupSummary(true)
            .setContentIntent(openAppPendingIntent)
            .build()
    }

    private fun createTrackingCompleteAction(
        instanceId: String,
        habitId: String
    ): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE
            putExtra(NotificationActionReceiver.EXTRA_INSTANCE_ID, instanceId)
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            "tracking_complete_$instanceId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(0, "Complete", pendingIntent).build()
    }

    private fun createTrackingIncrementAction(
        instanceId: String,
        habitId: String,
        defaultIncrement: Int
    ): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_ADD_ONE
            putExtra(NotificationActionReceiver.EXTRA_INSTANCE_ID, instanceId)
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            "tracking_increment_$instanceId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(0, "+$defaultIncrement", pendingIntent).build()
    }

    private fun createTrackingUndoAction(
        instanceId: String,
        habitId: String
    ): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_UNDO_LAST_INCREMENT
            putExtra(NotificationActionReceiver.EXTRA_INSTANCE_ID, instanceId)
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            "tracking_undo_$instanceId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(0, "Undo", pendingIntent).build()
    }

    private fun getChildNotificationId(instanceId: String): Int =
        NotificationChannels.NOTIFICATION_ID_TRACKING_BASE + (instanceId.hashCode() % 1000)
}
```

- [ ] **Step 3: Create `AndroidHabitNotification` actual**

Create `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/AndroidHabitNotification.kt`:

```kotlin
package com.ricardocosteira.habitlock.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitReminder
import com.ricardocosteira.habitlock.domain.models.ReminderType

actual class HabitNotification(private val context: Context) {

    private val scheduler: NotificationScheduler = NotificationScheduler(context)
    private val trackingManager: TrackingNotificationManager = TrackingNotificationManager(context)

    actual fun scheduleReminder(habit: Habit, reminder: HabitReminder, instance: HabitInstance) {
        if (!isNotificationPermissionGranted()) return

        when (reminder.reminderType) {
            ReminderType.FIXED -> {
                val time = reminder.time ?: return
                scheduler.scheduleHabitReminder(
                    instance = instance,
                    habit = habit,
                    reminderTimeHour = time.hour,
                    reminderTimeMinute = time.minute
                )
            }
            ReminderType.PERIODIC -> {
                // PERIODIC scheduling is out of scope for this work.
                // The existing NotificationScheduler only supports single-time alarms.
            }
        }
    }

    actual fun cancelReminder(instanceId: String) {
        scheduler.cancelNotificationsForInstance(instanceId)
    }

    actual fun cancelAllForHabit(habitId: String, instanceIds: List<String>) {
        instanceIds.forEach { instanceId ->
            scheduler.cancelNotificationsForInstance(instanceId)
        }
        // Also remove from tracking notification if present
        instanceIds.forEach { instanceId ->
            trackingManager.removeHabit(instanceId)
        }
    }

    actual fun updateTrackingNotification(trackedHabits: List<TrackedHabitInfo>) {
        if (!isNotificationPermissionGranted()) return
        trackingManager.update(trackedHabits)
    }

    actual fun hideTrackingNotification() {
        trackingManager.hide()
    }

    actual fun isNotificationPermissionGranted(): Boolean =
        NotificationChannels.areNotificationsEnabled(context)

    actual fun openNotificationSettings() {
        val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
```

- [ ] **Step 4: Build to verify Android compilation**

Run: `./gradlew composeApp:compileDebugKotlinAndroid`

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/NotificationChannels.kt \
       composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/TrackingNotificationManager.kt \
       composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/AndroidHabitNotification.kt
git commit -m "feat: add Android HabitNotification actual with tracking notification support"
```

---

### Task 4: Wire `HabitNotification` into DI

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/di/HabitLockAppComponent.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormViewModel.kt` (constructor only)
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt` (constructor only)

- [ ] **Step 1: Add `HabitNotification` to `HabitLockAppComponent`**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/di/HabitLockAppComponent.kt`:

Add an abstract accessor for `HabitNotification`:

```kotlin
abstract val habitNotification: HabitNotification
```

Note: `HabitNotification` is an expect class, so kotlin-inject needs a `@Provides` method. The Android actual takes `Context`, which isn't directly available in the common component. The approach is:

Add `HabitNotification` as a constructor parameter to the component (similar to `DatabaseDriverFactory`):

```kotlin
@Component
@AppScope
abstract class HabitLockAppComponent(
    @get:Provides val databaseDriverFactory: DatabaseDriverFactory,
    @get:Provides val habitNotification: HabitNotification
)
```

- [ ] **Step 2: Update `createAppComponent` expect/actual**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/di/AppComponentFactory.kt`:

```kotlin
expect fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification
): HabitLockAppComponent
```

In `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/di/AppComponentFactory.android.kt`:

```kotlin
actual fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification
): HabitLockAppComponent =
    HabitLockAppComponent::class.create(driverFactory, habitNotification)
```

In `composeApp/src/iosMain/kotlin/com/ricardocosteira/habitlock/di/AppComponentFactory.ios.kt`, update similarly with the iOS no-op `HabitNotification()`.

- [ ] **Step 3: Update `HabitLockApplication.onCreate()` to pass `HabitNotification`**

In `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/HabitLockApplication.kt`:

```kotlin
override fun onCreate() {
    super.onCreate()
    val driverFactory = DatabaseDriverFactory(this)
    val habitNotification = HabitNotification(this)
    appComponent = createAppComponent(driverFactory, habitNotification)
    NotificationChannels.createChannels(this)
    WorkManagerInitializer.initialize(this)
}
```

- [ ] **Step 4: Update `HabitFormViewModel.Factory` to include `HabitNotification`**

In `HabitLockAppComponent.kt`, update the factory provider:

```kotlin
@AppScope
@Provides
fun provideHabitFormViewModelFactory(
    habitRepository: HabitRepository,
    createHabit: CreateHabit,
    uuidProvider: UuidProvider,
    habitNotification: HabitNotification
): HabitFormViewModel.Factory =
    object : HabitFormViewModel.Factory {
        override fun create(habitIdToEdit: String?): HabitFormViewModel =
            HabitFormViewModel(habitRepository, createHabit, uuidProvider, habitNotification, habitIdToEdit)
    }
```

- [ ] **Step 5: Add `HabitNotification` parameter to `HabitFormViewModel` constructor**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormViewModel.kt`:

```kotlin
@Inject
class HabitFormViewModel(
    private val habitRepository: HabitRepository,
    private val createHabit: CreateHabit,
    private val uuidProvider: UuidProvider,
    private val habitNotification: HabitNotification,
    private val habitIdToEdit: String? = null
) : ViewModel()
```

Update the `Factory` interface:

```kotlin
interface Factory {
    fun create(habitIdToEdit: String? = null): HabitFormViewModel
}
```

- [ ] **Step 6: Add `HabitNotification` parameter to `TodayViewModel` constructor**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt`:

```kotlin
@AppScope
@Inject
class TodayViewModel(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val habitInstanceRepository: HabitInstanceRepository,
    private val generateDailyHabits: GenerateDailyHabits,
    private val processEndOfDay: ProcessEndOfDay,
    private val completeHabit: CompleteHabit,
    private val skipHabit: SkipHabit,
    private val undoHabit: UndoHabit,
    private val undoLastIncrement: UndoLastIncrement,
    private val habitNotification: HabitNotification
) : ViewModel()
```

- [ ] **Step 7: Build to verify DI compiles**

Run: `./gradlew composeApp:compileDebugKotlinAndroid`

Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/di/ \
       composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/di/ \
       composeApp/src/iosMain/kotlin/com/ricardocosteira/habitlock/di/ \
       composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/HabitLockApplication.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormViewModel.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt
git commit -m "feat: wire HabitNotification into DI and ViewModel constructors"
```

---

### Task 5: Wire Reminder Scheduling into `HabitFormViewModel`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormViewModel.kt`

- [ ] **Step 1: Schedule reminder after creating a new habit**

In `HabitFormViewModel.kt`, update `createNewHabit()` to schedule the reminder after successful creation. The `createHabit.execute()` returns `Result<Habit>` — we need the created habit to get the habitId for instance lookup. However, `scheduleReminder` needs a `HabitInstance`, and the instance is created by `GenerateDailyHabits` (which runs in the worker, not inline). 

The practical approach: schedule the reminder using the habit and reminder data we have. We need to fetch today's instance after creation. Update `createNewHabit()`:

```kotlin
private suspend fun createNewHabit(state: HabitFormState, reminder: HabitReminder?) {
    val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
    val specificDays = if (state.scheduleType == ScheduleType.WEEKLY) {
        state.selectedDays
    } else {
        null
    }

    val habit: Habit = createHabit
        .execute(
            params = CreateHabit.CreateHabitParams(
                name = state.name.trim(),
                description = state.description.trim().takeIf { it.isNotEmpty() },
                type = state.type,
                targetValue = if (state.type == HabitType.QUANTITATIVE) {
                    state.targetValue.toIntOrNull()
                } else {
                    null
                },
                unit = state.unit.trim().takeIf { it.isNotEmpty() },
                isTrackingEnabled = state.isTrackingEnabled,
                scheduleType = state.scheduleType,
                quota = state.quota.toIntOrNull() ?: 1,
                specificDays = specificDays,
                reminder = reminder
            ),
            startDate = today
        ).getOrThrow()

    // Schedule reminder if enabled
    if (reminder != null) {
        val instances: List<HabitInstance> =
            habitRepository.getInstancesForHabitOnDate(habit.id, today)
        val instance: HabitInstance? = instances.firstOrNull()
        if (instance != null) {
            habitNotification.scheduleReminder(habit, reminder, instance)
        }
    }
}
```

Note: `getInstancesForHabitOnDate` may not exist yet. Check `HabitInstanceRepository` — if missing, add it. If instances are created inline by `CreateHabit.execute()`, this works. If instances are only created by `GenerateDailyHabits`, the reminder will be scheduled by the worker instead (Task 7). In that case, skip the inline scheduling here and document the dependency.

- [ ] **Step 2: Cancel old reminder and schedule new on edit**

Update `updateExistingHabit()` — after the existing reminder deletion/creation block, add scheduling:

```kotlin
// After: habitRepository.createReminderForHabit(...)
// Cancel old reminders
val todayInstances: List<HabitInstance> =
    habitRepository.getInstancesForHabitOnDate(habitId, today)

todayInstances.forEach { instance ->
    habitNotification.cancelReminder(instance.id)
}

// Schedule new reminder if enabled
if (reminder != null) {
    val savedReminders: List<HabitReminder> = habitRepository.getRemindersForHabit(habitId)
    val savedReminder: HabitReminder? = savedReminders.firstOrNull()
    if (savedReminder != null) {
        todayInstances.forEach { instance ->
            habitNotification.scheduleReminder(existingHabit, savedReminder, instance)
        }
    }
}
```

Note: You need a `val today` at the start of `updateExistingHabit()`:
```kotlin
val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
```

- [ ] **Step 3: Cancel notifications on habit delete**

Update `deleteHabit()` to cancel notifications before deleting:

```kotlin
fun deleteHabit() {
    val habitId = _state.value.habitId ?: return

    viewModelScope.launch {
        try {
            val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
            val instances: List<HabitInstance> =
                habitRepository.getInstancesForHabitOnDate(habitId, today)
            habitNotification.cancelAllForHabit(habitId, instances.map { it.id })

            habitRepository.deleteHabit(habitId)
            _events.emit(HabitFormEvent.NavigateBack)
        } catch (e: Exception) {
            _events.emit(HabitFormEvent.ShowError(e.message))
        }
    }
}
```

- [ ] **Step 4: Add `getInstancesForHabitOnDate` if missing**

Check if `HabitInstanceRepository` has this method. If not, add to the interface:

```kotlin
suspend fun getInstancesForHabitOnDate(habitId: String, date: LocalDate): List<HabitInstance>
```

And implement in `HabitInstanceRepositoryImpl` using an appropriate SQL query. Add the query to `HabitLock.sq`:

```sql
getInstancesForHabitOnDate:
SELECT * FROM HabitInstance WHERE habitId = ? AND date = ?;
```

- [ ] **Step 5: Build to verify**

Run: `./gradlew composeApp:compileDebugKotlinAndroid`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormViewModel.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/repositories/HabitInstanceRepository.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/repositories/HabitInstanceRepositoryImpl.kt \
       composeApp/src/commonMain/sqldelight/com/ricardocosteira/habitlock/data/database/HabitLock.sq
git commit -m "feat: wire reminder scheduling into HabitFormViewModel save/edit/delete"
```

---

### Task 6: Wire Tracking Notification Updates into `TodayViewModel`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt`

- [ ] **Step 1: Add a helper method to build tracked habit info list**

In `TodayViewModel`, add a private method that builds `TrackedHabitInfo` from the current state:

```kotlin
private suspend fun refreshTrackingNotification() {
    val trackedHabits: List<Habit> = habitRepository.getHabitsWithTrackingEnabled()
    if (trackedHabits.isEmpty()) {
        habitNotification.hideTrackingNotification()
        return
    }

    val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
    val trackedInfoList: List<TrackedHabitInfo> = trackedHabits.mapNotNull { habit ->
        val instances: List<HabitInstance> =
            habitInstanceRepository.getInstancesForHabitOnDate(habit.id, today)
        val instance: HabitInstance = instances.firstOrNull() ?: return@mapNotNull null

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

    if (trackedInfoList.isEmpty()) {
        habitNotification.hideTrackingNotification()
    } else {
        habitNotification.updateTrackingNotification(trackedInfoList)
    }
}
```

- [ ] **Step 2: Call `refreshTrackingNotification()` after state-changing actions**

Add a call to `refreshTrackingNotification()` at the end of each success path in:

- `completeHabit()` — after `loadTodayHabits()`
- `incrementHabitProgress()` — after `loadTodayHabits()`
- `undoHabit()` — after `loadTodayHabits()`
- `undoLastIncrement()` — after `loadTodayHabits()`
- `skipHabit()` — after `loadTodayHabits()`
- `loadTodayHabits()` — at the end of the successful load, so the tracking notification reflects the latest state on screen open

For example, in `incrementHabitProgress()`:

```kotlin
fun incrementHabitProgress(instanceId: String) {
    // ... existing code ...
    result.onSuccess {
        loadTodayHabits()
        refreshTrackingNotification()
    }.onFailure { error ->
        _events.emit(TodayEvent.ShowError(error.message ?: "Something went wrong"))
    }
}
```

Apply the same pattern to all the methods listed above.

- [ ] **Step 3: Add required imports**

Add to imports in `TodayViewModel.kt`:

```kotlin
import com.ricardocosteira.habitlock.notifications.HabitNotification
import com.ricardocosteira.habitlock.notifications.TrackedHabitInfo
import com.ricardocosteira.habitlock.domain.models.HabitStatus
```

- [ ] **Step 4: Build to verify**

Run: `./gradlew composeApp:compileDebugKotlinAndroid`

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/today/TodayViewModel.kt
git commit -m "feat: wire tracking notification updates into TodayViewModel"
```

---

### Task 7: Wire Reminder + Tracking into `DailyHabitGenerationWorker` and `BootReceiver`

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/workers/DailyHabitGenerationWorker.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/BootReceiver.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/NotificationActionReceiver.kt`

- [ ] **Step 1: Update `DailyHabitGenerationWorker.doWork()`**

In `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/workers/DailyHabitGenerationWorker.kt`:

```kotlin
override suspend fun doWork(): Result {
    return try {
        val appComponent = applicationContext.habitLockApplication.appComponent
        appComponent.processEndOfDay.execute()
        appComponent.generateDailyHabits.execute()

        // Schedule reminders for today's instances
        val habitNotification: HabitNotification = appComponent.habitNotification
        val habitRepository: HabitRepository = appComponent.habitRepository
        val habitInstanceRepository: HabitInstanceRepository = appComponent.habitInstanceRepository
        val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())

        val activeHabits: List<Habit> = habitRepository.getActiveHabits()
        for (habit in activeHabits) {
            val reminders: List<HabitReminder> = habitRepository.getRemindersForHabit(habit.id)
            val reminder: HabitReminder? = reminders.firstOrNull()
            if (reminder != null) {
                val instances: List<HabitInstance> =
                    habitInstanceRepository.getInstancesForHabitOnDate(habit.id, today)
                instances.forEach { instance ->
                    habitNotification.scheduleReminder(habit, reminder, instance)
                }
            }
        }

        // Show tracking notification for tracked habits
        val trackedHabits: List<Habit> = habitRepository.getHabitsWithTrackingEnabled()
        val trackedInfoList: List<TrackedHabitInfo> = trackedHabits.mapNotNull { habit ->
            val instances: List<HabitInstance> =
                habitInstanceRepository.getInstancesForHabitOnDate(habit.id, today)
            val instance: HabitInstance = instances.firstOrNull() ?: return@mapNotNull null
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
        }

        Result.success()
    } catch (e: Exception) {
        e.printStackTrace()
        Result.retry()
    }
}
```

Add necessary imports for the new types.

- [ ] **Step 2: Implement `BootReceiver.onReceive()` reschedule**

In `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/BootReceiver.kt`:

```kotlin
override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
        WorkManagerInitializer.initialize(context)
        NotificationChannels.createChannels(context)

        val appComponent = context.habitLockApplication.appComponent
        val habitNotification: HabitNotification = appComponent.habitNotification
        val habitRepository: HabitRepository = appComponent.habitRepository
        val habitInstanceRepository: HabitInstanceRepository = appComponent.habitInstanceRepository

        CoroutineScope(Dispatchers.IO).launch {
            val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
            val activeHabits: List<Habit> = habitRepository.getActiveHabits()

            for (habit in activeHabits) {
                val reminders: List<HabitReminder> = habitRepository.getRemindersForHabit(habit.id)
                val reminder: HabitReminder? = reminders.firstOrNull()
                if (reminder != null) {
                    val instances: List<HabitInstance> =
                        habitInstanceRepository.getInstancesForHabitOnDate(habit.id, today)
                    instances.forEach { instance ->
                        habitNotification.scheduleReminder(habit, reminder, instance)
                    }
                }
            }

            // Restore tracking notification
            val trackedHabits: List<Habit> = habitRepository.getHabitsWithTrackingEnabled()
            val trackedInfoList: List<TrackedHabitInfo> = trackedHabits.mapNotNull { habit ->
                val instances: List<HabitInstance> =
                    habitInstanceRepository.getInstancesForHabitOnDate(habit.id, today)
                val instance: HabitInstance = instances.firstOrNull() ?: return@mapNotNull null
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
            }
        }
    }
}
```

- [ ] **Step 3: Add `ACTION_UNDO_LAST_INCREMENT` to `NotificationActionReceiver`**

The tracking notification uses an "Undo" action that calls `undoLastIncrement`. Add this action constant and handler in `NotificationActionReceiver`:

Add constant:
```kotlin
const val ACTION_UNDO_LAST_INCREMENT = "com.ricardocosteira.habitlock.ACTION_UNDO_LAST_INCREMENT"
```

Add to the `when` block in `onReceive()`:
```kotlin
ACTION_UNDO_LAST_INCREMENT -> handleUndoLastIncrement(appComponent, instanceId)
```

Add handler:
```kotlin
private suspend fun handleUndoLastIncrement(
    appComponent: HabitLockAppComponent,
    instanceId: String
) {
    appComponent.undoLastIncrement.execute(instanceId)
}
```

Also add `undoLastIncrement` as an accessor in `HabitLockAppComponent.kt`:
```kotlin
abstract val undoLastIncrement: UndoLastIncrement
```

- [ ] **Step 4: Update tracking notification after `NotificationActionReceiver` actions**

After each action in `NotificationActionReceiver.onReceive()`, refresh the tracking notification. After the `when` block and before `cancelAllNotificationsForInstance`, add tracking refresh logic. The simplest approach: after any action, rebuild the tracked habits list and update:

```kotlin
// After the when block, before finally:
refreshTrackingNotification(context, appComponent)
```

Add private method:
```kotlin
private suspend fun refreshTrackingNotification(
    context: Context,
    appComponent: HabitLockAppComponent
) {
    val habitNotification: HabitNotification = appComponent.habitNotification
    val habitRepository: HabitRepository = appComponent.habitRepository
    val habitInstanceRepository: HabitInstanceRepository = appComponent.habitInstanceRepository
    val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())

    val trackedHabits: List<Habit> = habitRepository.getHabitsWithTrackingEnabled()
    val trackedInfoList: List<TrackedHabitInfo> = trackedHabits.mapNotNull { habit ->
        val instances: List<HabitInstance> =
            habitInstanceRepository.getInstancesForHabitOnDate(habit.id, today)
        val instance: HabitInstance = instances.firstOrNull() ?: return@mapNotNull null
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
```

- [ ] **Step 5: Build to verify**

Run: `./gradlew composeApp:compileDebugKotlinAndroid`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/workers/DailyHabitGenerationWorker.kt \
       composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/BootReceiver.kt \
       composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/notifications/NotificationActionReceiver.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/di/HabitLockAppComponent.kt
git commit -m "feat: wire reminders and tracking into DailyHabitGenerationWorker and BootReceiver"
```

---

### Task 8: Add POST_NOTIFICATIONS Permission Request

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/MainActivity.kt`

- [ ] **Step 1: Add runtime permission request to `MainActivity`**

In `composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/MainActivity.kt`, add the permission request in `onCreate()`:

```kotlin
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result — no action needed here, form UI checks dynamically
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val appComponent = application.habitLockApplication.appComponent

        installSplashScreen().setKeepOnScreenCondition {
            appComponent.startupViewModel.state.value is StartupState.Loading
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        setContent {
            App(appComponent = appComponent)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew composeApp:compileDebugKotlinAndroid`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/androidMain/kotlin/com/ricardocosteira/habitlock/MainActivity.kt
git commit -m "feat: request POST_NOTIFICATIONS permission on first launch"
```

---

### Task 9: Update Form UI — State, Actions, and ViewModel Handlers

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormState.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormUiAction.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormViewModel.kt`

- [ ] **Step 1: Add fields to `HabitFormState`**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormState.kt`, add two new fields:

```kotlin
data class HabitFormState(
    val habitId: String? = null,
    val name: String = "",
    val description: String = "",
    val type: HabitType = HabitType.BINARY,
    val targetValue: String = "",
    val unit: String = "",
    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val selectedDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val quota: String = "1",
    val hasReminder: Boolean = false,
    val reminderType: ReminderType = ReminderType.FIXED,
    val reminderTime: LocalTime? = null,
    val intervalMinutes: String = "60",
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val isTrackingEnabled: Boolean = false,
    val isNotificationPermissionGranted: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)
```

Add a computed property for the "both enabled" hint:

```kotlin
val showBothEnabledHint: Boolean get() = hasReminder && isTrackingEnabled

val areNotificationTogglesEnabled: Boolean get() = isNotificationPermissionGranted
```

- [ ] **Step 2: Add action to `HabitFormUiAction`**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormUiAction.kt`, add:

```kotlin
data class IsTrackingEnabledChanged(val isEnabled: Boolean) : HabitFormUiAction
data object NotificationSettingsClicked : HabitFormUiAction
```

- [ ] **Step 3: Add handlers in `HabitFormViewModel`**

In `HabitFormViewModel.kt`, add:

```kotlin
fun updateIsTrackingEnabled(isEnabled: Boolean) {
    _state.update { it.copy(isTrackingEnabled = isEnabled) }
}

fun openNotificationSettings() {
    habitNotification.openNotificationSettings()
}
```

Update `init` block to check notification permission:

```kotlin
init {
    _state.update {
        it.copy(isNotificationPermissionGranted = habitNotification.isNotificationPermissionGranted())
    }
    if (habitIdToEdit != null) {
        loadHabit(habitIdToEdit)
    }
}
```

Update `loadHabit()` to populate `isTrackingEnabled` from the loaded habit:

In the `_state.update { it.copy(...) }` block inside `loadHabit()`, add:
```kotlin
isTrackingEnabled = habit.isTrackingEnabled,
```

Update `createNewHabit()` — `isTrackingEnabled` is already passed via `CreateHabitParams` (from Task 5 Step 1).

Update `updateExistingHabit()` — include `isTrackingEnabled` in the `existingHabit.copy(...)`:
```kotlin
val updatedHabit = existingHabit.copy(
    name = state.name.trim(),
    description = state.description.trim().takeIf { it.isNotEmpty() },
    type = state.type,
    targetValue = if (state.type == HabitType.QUANTITATIVE) {
        state.targetValue.toIntOrNull()
    } else {
        null
    },
    unit = state.unit.trim().takeIf { it.isNotEmpty() },
    isTrackingEnabled = state.isTrackingEnabled
)
```

- [ ] **Step 4: Handle new actions in `HabitFormScreen` (stateful composable)**

In `HabitFormScreen.kt`, in the `onAction` lambda of the stateful composable, add:

```kotlin
is HabitFormUiAction.IsTrackingEnabledChanged ->
    viewModel.updateIsTrackingEnabled(action.isEnabled)

HabitFormUiAction.NotificationSettingsClicked ->
    viewModel.openNotificationSettings()
```

- [ ] **Step 5: Re-check permission on resume**

In the stateful `HabitFormScreen` composable, add a `LifecycleResumeEffect` to refresh permission state when the user returns from Settings:

```kotlin
LifecycleResumeEffect(viewModel) {
    viewModel.refreshNotificationPermission()
    onPauseOrDispose { }
}
```

Add in `HabitFormViewModel`:
```kotlin
fun refreshNotificationPermission() {
    _state.update {
        it.copy(isNotificationPermissionGranted = habitNotification.isNotificationPermissionGranted())
    }
}
```

- [ ] **Step 6: Build to verify**

Run: `./gradlew composeApp:compileDebugKotlinAndroid`

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormState.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormUiAction.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormViewModel.kt \
       composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreen.kt
git commit -m "feat: add tracking and permission state to form ViewModel and state"
```

---

### Task 10: Update Form UI — Screen Layout

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreen.kt`
- Create: `composeApp/src/commonMain/composeResources/values/strings_notifications.xml`

- [ ] **Step 1: Add string resources**

Create `composeApp/src/commonMain/composeResources/values/strings_notifications.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="habit_form_tracking_title">Track this habit</string>
    <string name="habit_form_tracking_subtitle">Show a persistent notification to track progress and complete from anywhere</string>
    <string name="habit_form_notification_permission_denied">Notifications are disabled. Tap to open Settings.</string>
    <string name="habit_form_both_notifications_hint">You\'ll receive both a reminder and a persistent tracking notification for this habit.</string>
</resources>
```

- [ ] **Step 2: Add contextual messages above the extras card**

In the stateless `HabitFormScreen` composable, find the section that starts with `// Reminder + Note card` (around line 457). Before the `Surface(...)` block, add the contextual messages:

```kotlin
// Contextual notification messages
if (!state.isNotificationPermissionGranted) {
    Text(
        text = stringResource(Res.string.habit_form_notification_permission_denied),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction(HabitFormUiAction.NotificationSettingsClicked) }
            .padding(bottom = 8.dp)
    )
} else if (state.showBothEnabledHint) {
    Text(
        text = stringResource(Res.string.habit_form_both_notifications_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}
```

- [ ] **Step 3: Add tracking row inside the extras card**

Inside the `Surface { Column { ... } }` block, after the Reminder `DetailRow` and before the Note `DetailRow`, add the tracking toggle:

```kotlin
DetailRow(
    icon = Icons.Outlined.Notifications,  // Use a pinned/tracking icon if available
    iconTint = if (state.isTrackingEnabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    },
    title = stringResource(Res.string.habit_form_tracking_title),
    subtitle = stringResource(Res.string.habit_form_tracking_subtitle),
    onClick = null,
    showTopDivider = false,
    trailingContent = {
        Switch(
            checked = state.isTrackingEnabled,
            onCheckedChange = { checked: Boolean ->
                onAction(HabitFormUiAction.IsTrackingEnabledChanged(checked))
            },
            enabled = state.areNotificationTogglesEnabled
        )
    },
    modifier = Modifier.padding(horizontal = 12.dp)
)
```

- [ ] **Step 4: Disable reminder toggle when permission denied**

Find the existing Reminder `Switch` and add the `enabled` parameter:

```kotlin
Switch(
    checked = state.hasReminder,
    onCheckedChange = { checked: Boolean ->
        onAction(HabitFormUiAction.HasReminderChanged(checked))
        if (checked) isTimePickerVisible = true
    },
    enabled = state.areNotificationTogglesEnabled
)
```

- [ ] **Step 5: Add new string resource imports**

Add to the imports in `HabitFormScreen.kt`:

```kotlin
import habitlock.composeapp.generated.resources.habit_form_tracking_title
import habitlock.composeapp.generated.resources.habit_form_tracking_subtitle
import habitlock.composeapp.generated.resources.habit_form_notification_permission_denied
import habitlock.composeapp.generated.resources.habit_form_both_notifications_hint
```

- [ ] **Step 6: Build to verify**

Run: `./gradlew composeApp:compileDebugKotlinAndroid`

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreen.kt \
       composeApp/src/commonMain/composeResources/values/strings_notifications.xml
git commit -m "feat: add tracking toggle and permission UI to habit form screen"
```

---

### Task 11: Update Screenshot Tests

**Files:**
- Modify: `composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreenshotTest.kt`

- [ ] **Step 1: Update existing screenshot test state objects**

The `HabitFormScreenshotTest` creates `HabitFormState` objects for previews. Update them to include the new fields with their defaults (`isTrackingEnabled = false`, `isNotificationPermissionGranted = true`). Since both have sensible defaults, existing test states should continue to work without changes.

- [ ] **Step 2: Add new screenshot tests for the tracking toggle states**

Add test cases for:
- Form with tracking enabled (shows toggle on)
- Form with both reminder and tracking enabled (shows hint above card)
- Form with notification permission denied (shows disabled toggles and warning message)

Follow the existing screenshot test pattern in the file.

- [ ] **Step 3: Run screenshot tests and update reference images**

Run: `./gradlew composeApp:updateDebugScreenshotTest`

Expected: Reference images updated. Review the new screenshots to verify the UI looks correct.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/androidUnitTest/kotlin/com/ricardocosteira/habitlock/presentation/ui/habit/HabitFormScreenshotTest.kt \
       composeApp/src/androidUnitTest/snapshots/
git commit -m "test: update screenshot tests for tracking toggle and permission states"
```

---

### Task 12: Manual Testing Verification

- [ ] **Step 1: Build and install on device/emulator**

Run: `./gradlew composeApp:installDebug`

- [ ] **Step 2: Test reminder fix**

1. Create a new binary habit with reminder enabled at a time 2 minutes from now
2. Wait — notification should fire at the scheduled time
3. Verify notification has "Complete", "Snooze", "Skip" actions
4. Tap "Complete" — notification dismissed, habit marked complete in app

- [ ] **Step 3: Test tracking notification**

1. Create a quantitative habit with "Track this habit" enabled
2. Verify persistent notification appears in notification shade
3. Tap "+N" on notification — progress updates in both notification and app
4. Tap "Undo" — last increment reverted in both notification and app
5. Complete the habit — its child notification is removed from group
6. Create a second tracked habit — verify it appears in the group
7. Complete all tracked habits — entire notification dismissed

- [ ] **Step 4: Test permission handling**

1. Revoke notification permission in Settings > Apps > HabitLock > Notifications
2. Open create habit screen — verify both toggles are greyed out
3. Verify "Notifications are disabled. Tap to open Settings." message appears above card
4. Tap the message — should open app notification settings
5. Grant permission — return to app — toggles should be active

- [ ] **Step 5: Test both enabled hint**

1. Enable both reminder and tracking on a habit
2. Verify hint text "You'll receive both a reminder and a persistent tracking notification" appears above the card
3. Disable one — hint disappears

- [ ] **Step 6: Test boot receiver**

1. Create habits with reminders and tracking enabled
2. Reboot device
3. Verify tracking notification reappears
4. Verify reminders still fire at scheduled times

- [ ] **Step 7: Test edit flow**

1. Edit a habit — change reminder time
2. Verify old reminder cancelled, new one fires at new time
3. Edit a habit — disable reminder
4. Verify no notification fires
5. Edit a habit — enable/disable tracking
6. Verify tracking notification updates accordingly
