# Phase 2.3: Leave/Suspension Mode - Completion Summary

**Date:** January 18, 2026  
**Status:** ✅ COMPLETE

## Overview

Phase 2.3 implemented the Leave/Suspension Mode feature, allowing users to temporarily suspend habits during planned breaks (vacations, illness, etc.). Suspended habits don't generate notifications, affect streaks, or count as failures.

## Implementation Details

### 1. Domain Model Updates

**File:** `domain/models/HabitStatus.kt`

Added SUSPENDED status to the enum:
```kotlin
enum class HabitStatus {
    PENDING,
    COMPLETED,
    SKIPPED,
    FAILED,
    SUSPENDED  // NEW
}
```

### 2. Use Cases

#### SuspendHabitUseCase (NEW)

**File:** `domain/usecases/SuspendHabitUseCase.kt`

Creates leave periods for habits with validation:
- Verifies habit exists
- Validates date ranges (end date must be after start date)
- Checks for overlapping leave periods
- Creates LeavePeriod with UUID and timestamp

```kotlin
suspend fun execute(
    habitId: String,
    startDate: LocalDate,
    endDate: LocalDate?,
    reason: String? = null
): Result<LeavePeriod>
```

**Features:**
- Prevents overlapping suspensions for the same habit
- Supports indefinite suspensions (null end date)
- Optional reason field for tracking why habit was suspended

#### UnsuspendHabitUseCase (NEW)

**File:** `domain/usecases/UnsuspendHabitUseCase.kt`

Ends suspensions early or removes future suspensions:
- If leave period hasn't started: deletes it entirely
- If leave period is active: sets end date to today
- If leave period has ended: returns error

```kotlin
suspend fun execute(leavePeriodId: String): Result<LeavePeriod?>
suspend fun delete(leavePeriodId: String): Result<Unit>
```

**Features:**
- Smart handling based on leave period status
- Separate delete method for complete removal
- Validates leave period exists before operating

#### GenerateDailyHabitsUseCase (UPDATED)

**File:** `domain/usecases/GenerateDailyHabitsUseCase.kt`

Updated to handle suspended habits:
- Checks for active leave periods before creating instances
- Creates SUSPENDED instances instead of PENDING when on leave
- Does NOT increment expectedCompletions for suspended instances
- Suspended instances don't track consecutive skips

**Key Changes:**
```kotlin
// Check if habit is on leave (suspended)
val activeLeavePeriod = leavePeriodRepository.getActiveLeavePeriod(habit.id, today)
val isSuspended = activeLeavePeriod != null

val instance = if (isSuspended) {
    createSuspendedDailyInstance(...)  // Status = SUSPENDED
} else {
    createDailyInstance(...)  // Status = PENDING
}

// Only increment expected completions for non-suspended habits
if (!isSuspended) {
    habitRepository.incrementHabitExpectedCompletions(habit.id, amount = 1)
}
```

**New Helper Methods:**
- `createSuspendedDailyInstance()` - Creates daily SUSPENDED instance
- `createSuspendedWeeklyInstance()` - Creates weekly SUSPENDED instance

#### CompleteHabitUseCase (UPDATED)

**File:** `domain/usecases/CompleteHabitUseCase.kt`

Added validation to reject SUSPENDED instances:
```kotlin
if (instance.status == HabitStatus.SUSPENDED) {
    return Result.failure(IllegalStateException("Cannot complete suspended habit"))
}
```

Applied to both `executeBinary()` and `executeQuantitative()` methods.

#### SkipHabitUseCase (UPDATED)

**File:** `domain/usecases/SkipHabitUseCase.kt`

Added validation to reject SUSPENDED instances:
```kotlin
if (instance.status == HabitStatus.SUSPENDED) {
    return Result.failure(IllegalStateException("Cannot skip suspended habit"))
}
```

#### ProcessEndOfDayUseCase (UPDATED)

**File:** `domain/usecases/ProcessEndOfDayUseCase.kt`

Updated to skip SUSPENDED instances during failure processing:

**Daily Habits:**
- Changed from `getPendingInstancesForDate()` to `getInstancesForDate()`
- Explicitly filters for PENDING status only
- SUSPENDED instances are not marked as FAILED

**Weekly Habits:**
- Only processes instances with PENDING status
- SUSPENDED instances are ignored during end-of-week processing

```kotlin
// Only process PENDING instances (skip SUSPENDED)
if (lastWeekInstance != null && lastWeekInstance.status == HabitStatus.PENDING) {
    // Process failure logic
}
```

### 3. Dependency Injection Updates

**File:** `di/AppModule.kt`

Added new use cases and updated dependencies:
```kotlin
// Updated GenerateDailyHabitsUseCase to include leavePeriodRepository
private val generateDailyHabitsUseCase: GenerateDailyHabitsUseCase by lazy {
    GenerateDailyHabitsUseCase(
        userRepository,
        habitRepository,
        habitInstanceRepository,
        leavePeriodRepository,  // NEW
        uuidProvider
    )
}

// Added new use cases
private val suspendHabitUseCase: SuspendHabitUseCase by lazy {
    SuspendHabitUseCase(habitRepository, leavePeriodRepository, uuidProvider)
}

private val unsuspendHabitUseCase: UnsuspendHabitUseCase by lazy {
    UnsuspendHabitUseCase(leavePeriodRepository, userRepository)
}

// Public providers
fun provideSuspendHabitUseCase(): SuspendHabitUseCase
fun provideUnsuspendHabitUseCase(): UnsuspendHabitUseCase
fun provideLeavePeriodRepository(): LeavePeriodRepository
```

### 4. UI Updates

**File:** `presentation/ui/today/TodayScreen.kt`

Updated to display SUSPENDED status:

**Card Color:**
```kotlin
val cardColor = when (habit.status) {
    HabitStatus.SUSPENDED -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    // ... other statuses
}
```

**Action Buttons:**
```kotlin
when (habit.status) {
    HabitStatus.SUSPENDED -> {
        Text(
            text = "Suspended",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
    // ... other statuses
}
```

## Business Rules Implementation

### Suspended Habits Behavior

1. **Instance Generation:**
   - SUSPENDED instances are created during leave periods
   - Status is set to SUSPENDED (not PENDING)
   - consecutiveSkipsAtCreation is always 0

2. **Score Impact:**
   - SUSPENDED instances DO NOT increment expectedCompletions
   - No impact on habit score during suspension
   - Score remains accurate when comparing completions vs expectations

3. **Streak Preservation:**
   - SUSPENDED instances are not marked as FAILED
   - Streaks are preserved during suspension
   - No streak reset for suspended days

4. **User Actions:**
   - Cannot complete SUSPENDED instances
   - Cannot skip SUSPENDED instances
   - Can end suspension early via UnsuspendHabitUseCase

5. **Notifications:**
   - SUSPENDED instances should not trigger notifications (Phase 3 implementation)

### Leave Period Management

1. **Overlap Prevention:**
   - Cannot create overlapping leave periods for the same habit
   - Validation occurs in SuspendHabitUseCase

2. **Early Termination:**
   - Active leave periods can be ended early
   - End date is set to current date
   - Future leave periods can be deleted entirely

3. **Automatic Unsuspension:**
   - Leave periods with end dates automatically expire
   - Habit returns to normal on the day after endDate
   - No manual unsuspension needed for dated leave periods

## Infrastructure Reuse

Phase 2.3 leveraged infrastructure created in Phase 1:
- LeavePeriod domain model (already existed)
- LeavePeriodRepository interface and implementation (already existed)
- Database schema with LeavePeriod table (already existed)
- Entity mappers for LeavePeriod (already existed)

Only needed to add:
- SUSPENDED status to HabitStatus enum
- Two new use cases (Suspend and Unsuspend)
- Integration into existing use cases
- UI updates

## Files Modified

### Domain Layer
- `domain/models/HabitStatus.kt` - Added SUSPENDED status
- `domain/usecases/SuspendHabitUseCase.kt` - NEW (84 lines)
- `domain/usecases/UnsuspendHabitUseCase.kt` - NEW (75 lines)
- `domain/usecases/GenerateDailyHabitsUseCase.kt` - Updated
- `domain/usecases/CompleteHabitUseCase.kt` - Updated
- `domain/usecases/SkipHabitUseCase.kt` - Updated
- `domain/usecases/ProcessEndOfDayUseCase.kt` - Updated

### Dependency Injection
- `di/AppModule.kt` - Added new use cases and updated dependencies

### Presentation Layer
- `presentation/ui/today/TodayScreen.kt` - Updated UI for SUSPENDED status

## Build Status

✅ **Compilation:** Successful  
✅ **Unit Tests:** All passing  
✅ **Linter:** No errors

## Testing

All existing tests pass:
- ✅ HabitScoreTest (13 tests)
- ✅ LeavePeriodTest (tests for LeavePeriod model)
- ✅ StrictnessPresetTest

## Next Steps

According to the ROADMAP, the next section to implement is:

### Phase 2.4: Snooze Implementation
- Create `SnoozeHabitUseCase` implementation
- Integrate snooze limits from user preferences
- Handle snooze expiration and re-notification

Note: SnoozeState table and SnoozeRepository already exist from Phase 1.

## Notes

- **Score Accuracy:** SUSPENDED instances don't affect habit scores because they don't increment expectedCompletions
- **Streak Preservation:** Critical for user trust - suspensions don't break streaks
- **UI Clarity:** SUSPENDED status is visually distinct with muted colors
- **Validation:** Overlap prevention ensures data integrity
- **Flexibility:** Supports both dated and indefinite suspensions
- **Early Exit:** Users can end suspensions early if plans change

The suspension system is fully functional and ready for UI integration in Phase 4!
