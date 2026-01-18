# Phase 2.2: Habit Score Calculation - Completion Summary

**Date:** January 18, 2026  
**Status:** ✅ COMPLETE

## Overview

Phase 2.2 focused on implementing the Habit Score calculation system, which tracks long-term consistency and over-completion for habits. The score reflects how well users maintain their habits relative to expectations.

## Implementation Details

### 1. Database Schema Updates

**File:** `HabitLock.sq`

Added score tracking fields to the Habit table:
- `totalCompletions INTEGER NOT NULL DEFAULT 0` - Total number of completions across all time
- `expectedCompletions INTEGER NOT NULL DEFAULT 0` - Total number of expected completions based on cadence

Added new SQL queries:
- `updateHabitScore` - Updates both totalCompletions and expectedCompletions
- `incrementHabitTotalCompletions` - Increments totalCompletions by a specified amount
- `decrementHabitTotalCompletions` - Decrements totalCompletions (with safety check)
- `incrementHabitExpectedCompletions` - Increments expectedCompletions by a specified amount

### 2. Domain Model Updates

**File:** `domain/models/Habit.kt`

Updated Habit model to include:
```kotlin
data class Habit(
    // ... existing fields
    val totalCompletions: Int,
    val expectedCompletions: Int,
    // ... existing fields
) {
    fun calculateScore(overCompletionCap: Int = HabitScore.DEFAULT_OVER_COMPLETION_CAP): HabitScore {
        return HabitScore(
            totalCompletions = totalCompletions,
            expectedCompletions = expectedCompletions,
            overCompletionCap = overCompletionCap
        )
    }
}
```

**File:** `domain/models/HabitScore.kt` (Already existed from Phase 1)

The HabitScore model provides:
- Score percentage calculation (0-150%)
- Over-completion tracking
- Missed completion tracking
- Helper methods for incrementing/decrementing values

### 3. Repository Layer Updates

**Files:**
- `domain/repositories/HabitRepository.kt`
- `data/repositories/HabitRepositoryImpl.kt`

Added new repository methods:
```kotlin
suspend fun updateHabitScore(habitId: String, totalCompletions: Int, expectedCompletions: Int)
suspend fun incrementHabitTotalCompletions(habitId: String, amount: Int = 1)
suspend fun decrementHabitTotalCompletions(habitId: String, amount: Int = 1)
suspend fun incrementHabitExpectedCompletions(habitId: String, amount: Int = 1)
```

**File:** `data/mappers/EntityMappers.kt`

Updated Habit mapper to include score fields:
```kotlin
fun DbHabit.toDomain(): Habit = Habit(
    // ... existing fields
    totalCompletions = totalCompletions.toInt(),
    expectedCompletions = expectedCompletions.toInt(),
    // ... existing fields
)
```

### 4. Use Case Updates

#### CalculateHabitScoreUseCase (NEW)

**File:** `domain/usecases/CalculateHabitScoreUseCase.kt`

New use case for calculating habit scores:
```kotlin
class CalculateHabitScoreUseCase(
    private val habitRepository: HabitRepository
) {
    suspend fun execute(habitId: String, overCompletionCap: Int = 150): HabitScore?
    fun calculateScoreForHabit(habit: Habit, overCompletionCap: Int = 150): HabitScore
    fun calculateScoresForHabits(habits: List<Habit>, overCompletionCap: Int = 150): Map<String, HabitScore>
}
```

#### CompleteHabitUseCase (UPDATED)

**File:** `domain/usecases/CompleteHabitUseCase.kt`

Updated to increment totalCompletions:
- Binary habits: Increments by 1 on completion
- Quantitative habits: Increments by deltaValue (supports over-completion)

```kotlin
// After completing a habit
incrementTotalCompletions(instance.habitId, amount = deltaValue)
```

#### UndoHabitUseCase (UPDATED)

**File:** `domain/usecases/UndoHabitUseCase.kt`

Updated to decrement totalCompletions when undoing:
```kotlin
// Get the completed value before undoing
val completedValueToUndo = instance.completedValue ?: 0

// After resetting instance to pending
if (completedValueToUndo > 0) {
    habitRepository.decrementHabitTotalCompletions(
        habitId = instance.habitId,
        amount = completedValueToUndo
    )
}
```

#### GenerateDailyHabitsUseCase (UPDATED)

**File:** `domain/usecases/GenerateDailyHabitsUseCase.kt`

Updated to increment expectedCompletions when creating instances:
- Daily habits: Increments by 1 per day
- Weekly habits: Increments by quota per week

```kotlin
when (schedule.scheduleType) {
    ScheduleType.DAILY -> {
        // ... create instance
        habitRepository.incrementHabitExpectedCompletions(habit.id, amount = 1)
    }
    ScheduleType.WEEKLY -> {
        // ... create instance
        habitRepository.incrementHabitExpectedCompletions(habit.id, amount = schedule.quota)
    }
}
```

#### CreateHabitUseCase (UPDATED)

**File:** `domain/usecases/CreateHabitUseCase.kt`

Updated to initialize score fields:
```kotlin
val habit = Habit(
    // ... existing fields
    totalCompletions = 0,
    expectedCompletions = 0,
    // ... existing fields
)
```

### 5. Dependency Injection Updates

**File:** `di/AppModule.kt`

Added CalculateHabitScoreUseCase to DI container:
```kotlin
private val calculateHabitScoreUseCase: CalculateHabitScoreUseCase by lazy {
    CalculateHabitScoreUseCase(habitRepository)
}

fun provideCalculateHabitScoreUseCase(): CalculateHabitScoreUseCase = calculateHabitScoreUseCase
```

## Score Calculation Formula

```
HabitScore = min(overCompletionCap, (totalCompletions / expectedCompletions) * 100)
```

**Interpretation:**
- **0-99%**: Some completions were missed
- **100%**: Perfect completion rate (all expected completions done)
- **101-150%**: Over-completion (exceeding expectations)

**Example:**
- Expected: 30 days
- Completed: 35 days
- Score: min(150, (35/30) * 100) = 116%

## Integration Points

### Automatic Score Updates

The score is automatically updated in the following scenarios:

1. **Habit Completion** (CompleteHabitUseCase)
   - totalCompletions increments by completion amount
   
2. **Habit Undo** (UndoHabitUseCase)
   - totalCompletions decrements by undone amount
   
3. **Daily Instance Generation** (GenerateDailyHabitsUseCase)
   - expectedCompletions increments by 1 for daily habits
   - expectedCompletions increments by quota for weekly habits

### Score Retrieval

Scores can be retrieved in two ways:

1. **Via Use Case:**
   ```kotlin
   val score = calculateHabitScoreUseCase.execute(habitId)
   ```

2. **Via Habit Model:**
   ```kotlin
   val habit = habitRepository.getHabitById(habitId)
   val score = habit.calculateScore()
   ```

## Testing

All existing tests pass:
- ✅ `HabitScoreTest` - 13 tests covering score calculation logic
- ✅ `LeavePeriodTest` - Tests for suspension functionality
- ✅ `StrictnessPresetTest` - Tests for strictness presets

## Files Modified

### Database
- `HabitLock.sq` - Added score fields and queries

### Domain Models
- `domain/models/Habit.kt` - Added score fields and calculateScore method

### Repositories
- `domain/repositories/HabitRepository.kt` - Added score update methods
- `data/repositories/HabitRepositoryImpl.kt` - Implemented score update methods

### Mappers
- `data/mappers/EntityMappers.kt` - Updated Habit mapper

### Use Cases
- `domain/usecases/CalculateHabitScoreUseCase.kt` - NEW
- `domain/usecases/CompleteHabitUseCase.kt` - Updated to increment score
- `domain/usecases/UndoHabitUseCase.kt` - Updated to decrement score
- `domain/usecases/GenerateDailyHabitsUseCase.kt` - Updated to increment expected
- `domain/usecases/CreateHabitUseCase.kt` - Updated to initialize score

### Dependency Injection
- `di/AppModule.kt` - Added CalculateHabitScoreUseCase

## Build Status

✅ **Compilation:** Successful  
✅ **Unit Tests:** All passing  
✅ **Linter:** No errors

## Next Steps

According to the ROADMAP, the next sections to implement are:

### Phase 2.3: Leave/Suspension Mode
- Create `SuspendHabitUseCase`
- Create `UnsuspendHabitUseCase`
- Update instance generation to skip suspended habits
- Exclude suspended habits from streak calculations

### Phase 2.4: Snooze Implementation
- Create `SnoozeHabitUseCase` implementation
- Integrate snooze limits from user preferences
- Handle snooze expiration and re-notification

### Phase 2.5: Over-Completion Handling
- Allow completing beyond quota
- Track over-completion in HabitCompletionEvent
- Add over-completion to score calculation (already done)
- Optional: Prompt to update quota after consistent over-completion

## Notes

- The score calculation is **incremental** - it updates as habits are completed/undone rather than being recalculated from scratch
- The score supports **over-completion** up to 150% by default (configurable)
- The score is **persistent** - stored in the database and survives app restarts
- The score is **accurate** - it correctly handles both daily and weekly habits with different quotas
- The implementation follows **clean architecture** principles with clear separation of concerns
