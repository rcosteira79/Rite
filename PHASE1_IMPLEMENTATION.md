# Phase 1: Core Foundation Hardening - Implementation Summary

**Phase Goal:** Ensure core data layer and business logic is solid

**Status:** 🚧 IN PROGRESS (2/4 tasks completed)

**Duration:** 1-2 weeks

**Date Started:** January 18, 2026

---

## Overview

Phase 1 focuses on hardening the foundation of HabitLock by completing the core domain models and ensuring the data layer is robust and ready for business logic implementation. This phase includes domain model enhancements, database schema updates, and establishing proper patterns for data validation and transformation.

---

## Section 1.1: Fix and Complete Domain Models

### ✅ Task 1: Add `HabitSchedule` domain model with proper cadence (DAILY/WEEKLY) support

**Status:** COMPLETED

**Date Completed:** January 18, 2026

#### Changes Made

##### 1. Domain Model Enhancement
**File:** `domain/models/HabitSchedule.kt`

**Changes:**
- Extended `ScheduleType` enum to include `WEEKLY` (previously only had `DAILY`)
- Added `quota: Int` field with default value of 1 (completions required per cadence window)
- Added `weekStartDay: DayOfWeek` field with default `MONDAY` (configurable week start)
- Added `specificDays: Set<DayOfWeek>?` field for weekly schedules (null = all days)
- Added validation in `init` block to ensure:
  - Quota is always > 0
  - Specific days are not empty when provided for weekly schedules
- Added `isActiveOn(date: LocalDate)` utility method to check if schedule is active on a given date

**Benefits:**
- Full support for weekly habits with flexible configuration
- Type-safe week start day using kotlinx.datetime.DayOfWeek
- Flexible weekly scheduling (all days or specific days only)
- Default values ensure backward compatibility

##### 2. Database Schema Update
**File:** `data/database/HabitLock.sq`

**Changes to `HabitSchedule` table:**
- Added `quota INTEGER NOT NULL DEFAULT 1`
- Added `weekStartDay TEXT NOT NULL DEFAULT 'MONDAY'`
- Added `specificDays TEXT` (nullable, comma-separated day names)

**Changes to SQL queries:**
- `insertSchedule`: Updated to include quota, weekStartDay, and specificDays
- `updateSchedule`: Updated to include quota, weekStartDay, and specificDays

**Data Format:**
- `specificDays` stored as comma-separated string (e.g., "MONDAY,WEDNESDAY,FRIDAY")
- `weekStartDay` stored as enum name string (e.g., "MONDAY")

##### 3. Data Mapper Update
**File:** `data/mappers/EntityMappers.kt`

**Changes:**
- Updated `DbHabitSchedule.toDomain()` to map new fields:
  - `quota`: Convert Long to Int
  - `weekStartDay`: Parse string to DayOfWeek enum
  - `specificDays`: Parse comma-separated string to Set<DayOfWeek>

**Parsing Logic:**
```kotlin
specificDays = specificDays?.split(",")
    ?.filter { it.isNotBlank() }
    ?.map { kotlinx.datetime.DayOfWeek.valueOf(it.trim()) }
    ?.toSet()
```

##### 4. Repository Implementation Update
**File:** `data/repositories/HabitRepositoryImpl.kt`

**Changes:**
- Updated `createHabit()` method to serialize new fields:
  - `quota`: Convert Int to Long
  - `weekStartDay`: Convert DayOfWeek enum to name string
  - `specificDays`: Join Set<DayOfWeek> with comma separator

##### 5. Use Case Enhancement
**File:** `domain/usecases/CreateHabitUseCase.kt`

**Changes to `CreateHabitParams`:**
- Added `scheduleType: ScheduleType = ScheduleType.DAILY`
- Added `quota: Int = 1`
- Added `weekStartDay: DayOfWeek = DayOfWeek.MONDAY`
- Added `specificDays: Set<DayOfWeek>? = null`

**New Validations:**
- Quota must be > 0
- Specific days cannot be empty for weekly schedules

#### Backward Compatibility

✅ **All existing code continues to work without modification**

The implementation maintains full backward compatibility:
- All new fields have default values
- Existing `CreateHabitParams` usages work without changes (defaults to DAILY with quota=1)
- Database schema has defaults for new columns
- Existing DAILY habits will have quota=1 and weekStartDay=MONDAY

#### Testing Status
- ✅ Code compiles successfully
- ✅ SQLDelight schema generation successful
- ✅ No breaking changes to existing code
- ⚠️ Unit tests not yet written (planned for Phase 5)

#### Files Modified
1. `/composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/HabitSchedule.kt`
2. `/composeApp/src/commonMain/sqldelight/com/ricardocosteira/habitlock/data/database/HabitLock.sq`
3. `/composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/mappers/EntityMappers.kt`
4. `/composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/repositories/HabitRepositoryImpl.kt`
5. `/composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/usecases/CreateHabitUseCase.kt`

#### Usage Example

```kotlin
// Create a daily habit (existing behavior, uses defaults)
val dailyHabit = CreateHabitUseCase.CreateHabitParams(
    name = "Morning meditation",
    description = "10 minutes of mindfulness",
    type = HabitType.BINARY,
    targetValue = null,
    unit = null,
    reminder = someReminder
)

// Create a weekly habit with custom configuration
val weeklyHabit = CreateHabitUseCase.CreateHabitParams(
    name = "Gym workout",
    description = "Strength training",
    type = HabitType.QUANTITATIVE,
    targetValue = 3,
    unit = "sessions",
    scheduleType = ScheduleType.WEEKLY,
    quota = 3,  // 3 times per week
    weekStartDay = DayOfWeek.MONDAY,
    specificDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
    reminder = null
)
```

---

### ✅ Task 2: Add `HabitScore` computation model

**Status:** COMPLETED

**Date Completed:** January 18, 2026

#### Changes Made

##### 1. Domain Model Creation
**File:** `domain/models/HabitScore.kt`

**Core Features:**
- **Data class** with immutable properties:
  - `totalCompletions: Int` - Total number of completions across all time
  - `expectedCompletions: Int` - Total expected completions based on cadence
  - `overCompletionCap: Int` - Maximum score percentage (default: 150)
  - `lastCalculatedAt: LocalDate?` - Optional timestamp for caching

**Computed Properties:**
- `percentage: Int` - Score as percentage (0-150), capped at overCompletionCap
- `percentageFloat: Float` - Score as float for UI progress bars
- `isPerfect: Boolean` - True if score >= 100%
- `isOverCompleted: Boolean` - True if score > 100%
- `overCompletionCount: Int` - Number of extra completions beyond expected
- `missedCompletionCount: Int` - Number of missed completions

**Helper Methods:**
- `withIncrementedCompletion(amount: Int)` - Returns new score with added completions
- `withIncrementedExpected(amount: Int)` - Returns new score with added expected count
- `withDecrementedCompletion(amount: Int)` - Returns new score with subtracted completions (for undo)

**Companion Object:**
- `DEFAULT_OVER_COMPLETION_CAP = 150` - Default cap constant
- `initial(overCompletionCap: Int)` - Factory method for creating new score at zero

**Validation:**
- Total completions must be non-negative
- Expected completions must be non-negative
- Over-completion cap must be positive
- Handles edge case of zero expected completions (returns 0%)

##### 2. Score Calculation Algorithm

**Formula:**
```kotlin
percentage = min(overCompletionCap, (totalCompletions * 100) / expectedCompletions)
```

**Examples:**
- 5/10 completions = 50%
- 10/10 completions = 100% (perfect)
- 15/10 completions = 150% (capped at overCompletionCap)
- 100/10 completions = 150% (capped, not 1000%)
- 5/0 completions = 0% (special case handling)

**Benefits:**
- Encourages consistency (tracks missed completions)
- Rewards over-completion (up to 150%)
- Prevents score inflation beyond reasonable limits
- Simple, deterministic calculation

##### 3. Immutability Pattern

The model uses an **immutable data class** with copy methods:
```kotlin
// Instead of mutating:
score.totalCompletions = score.totalCompletions + 1 // ❌ Not allowed

// Use copy methods:
val updatedScore = score.withIncrementedCompletion(1) // ✅ Correct
```

**Benefits:**
- Thread-safe
- Easier to reason about
- Fits functional programming style
- Works well with Kotlin coroutines and flows

##### 4. Comprehensive Unit Tests
**File:** `commonTest/kotlin/.../domain/models/HabitScoreTest.kt`

**Test Coverage:**
- ✅ Percentage calculation for various completion ratios
- ✅ Over-completion cap enforcement
- ✅ Edge case: zero expected completions
- ✅ Perfect score detection
- ✅ Over-completion detection
- ✅ Over-completion count calculation
- ✅ Missed completion count calculation
- ✅ Increment/decrement operations
- ✅ Coercion to zero (prevent negative completions)
- ✅ Factory method (initial score)
- ✅ Float percentage conversion

**All tests passing:** ✅ (20 test cases)

#### Design Decisions

**Why Cap at 150%?**
The 150% cap (allowing 50% over-completion) strikes a balance:
- **Too low (e.g., 100%)**: Discourages over-completion, no reward for extra effort
- **Too high (e.g., 300%)**: Score loses meaning, can inflate too easily
- **150%**: Recognizes extra effort without making the score meaningless

**Why Store `lastCalculatedAt`?**
Enables caching strategies:
- Can recalculate scores only when needed
- Supports incremental updates
- Avoids expensive full recalculations on every query

**Why Immutable?**
- **Concurrency**: Safe to share across threads/coroutines
- **Predictability**: State changes are explicit via copy methods
- **Testing**: Easier to test pure functions
- **Kotlin Style**: Idiomatic for data classes

#### Files Created
1. `/composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/HabitScore.kt`
2. `/composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/domain/models/HabitScoreTest.kt`

#### Usage Example

```kotlin
// Create initial score
val score = HabitScore.initial()

// Track progress over time
val afterDay1 = score
    .withIncrementedExpected(1)  // Day 1 expected
    .withIncrementedCompletion(1) // Completed day 1
    
val afterDay2 = afterDay1
    .withIncrementedExpected(1)  // Day 2 expected
    // Missed day 2 (no completion increment)
    
val afterDay3 = afterDay2
    .withIncrementedExpected(1)  // Day 3 expected
    .withIncrementedCompletion(1) // Completed day 3

println(afterDay3.percentage) // 66% (2 out of 3 days)
println(afterDay3.missedCompletionCount) // 1 (day 2 missed)

// Handle undo
val afterUndo = afterDay3.withDecrementedCompletion(1) // Undo day 3
println(afterUndo.percentage) // 33% (1 out of 3 days)

// Check status
if (score.isPerfect) {
    println("Perfect habit! 🎉")
}
if (score.isOverCompleted) {
    println("Going above and beyond! ⭐")
}
```

---

### ⏳ Task 3: Add `LeavePeriod` model for suspension tracking

**Status:** PENDING

**Estimated Time:** 2-3 hours

#### Planned Implementation

**File to create:** `domain/models/LeavePeriod.kt`

**Proposed Structure:**
```kotlin
data class LeavePeriod(
    val id: String,
    val habitId: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val reason: String?,
    val createdAt: Instant
) {
    init {
        if (endDate != null) {
            require(endDate >= startDate) { "End date must be after or equal to start date" }
        }
    }
    
    fun isActiveOn(date: LocalDate): Boolean {
        if (date < startDate) return false
        if (endDate != null && date > endDate) return false
        return true
    }
    
    fun hasEnded(currentDate: LocalDate): Boolean {
        return endDate != null && currentDate > endDate
    }
}
```

**Database Schema:**
```sql
CREATE TABLE LeavePeriod (
    id TEXT NOT NULL PRIMARY KEY,
    habitId TEXT NOT NULL,
    startDate TEXT NOT NULL,
    endDate TEXT,
    reason TEXT,
    createdAt TEXT NOT NULL,
    FOREIGN KEY (habitId) REFERENCES Habit(id) ON DELETE CASCADE
);

CREATE INDEX idx_leave_period_habit_id ON LeavePeriod(habitId);
CREATE INDEX idx_leave_period_dates ON LeavePeriod(startDate, endDate);
```

**Planned Changes:**
1. Create `LeavePeriod` domain model
2. Add database table and queries
3. Create `LeavePeriodRepository` interface and implementation
4. Add mapper for database entity conversion
5. Add unit tests for validation and date logic

---

### ⏳ Task 4: Ensure `StrictnessPreset` enum properly maps to undo/skip/snooze limits

**Status:** PENDING

**Estimated Time:** 2-3 hours

#### Current State Analysis

Need to verify current `StrictnessPreset` implementation and ensure proper mapping.

#### Planned Implementation

**File to check/modify:** `domain/models/StrictnessPreset.kt`

**Expected Mapping:**

| Preset | Undo Policy | Max Snoozes | Max Skips | Snooze Duration |
|--------|-------------|-------------|-----------|-----------------|
| FLEXIBLE | ALL_HISTORY | Unlimited (null) | Unlimited (null) | 60 min |
| BALANCED | TODAY_ONLY | 3 per habit/day | 2 consecutive | 30 min |
| LOCKED | NONE | 1 per habit/day | 0 consecutive | 15 min |

**Proposed Structure:**
```kotlin
enum class StrictnessPreset {
    FLEXIBLE,
    BALANCED,
    LOCKED;
    
    fun toUserSettings(): UserStrictnessSettings {
        return when (this) {
            FLEXIBLE -> UserStrictnessSettings(
                undoPolicy = UndoPolicy.ALL_HISTORY,
                maxSnoozesPerHabitPerDay = null,
                maxConsecutiveSkips = null,
                maxSnoozeDurationMinutes = 60
            )
            BALANCED -> UserStrictnessSettings(
                undoPolicy = UndoPolicy.TODAY_ONLY,
                maxSnoozesPerHabitPerDay = 3,
                maxConsecutiveSkips = 2,
                maxSnoozeDurationMinutes = 30
            )
            LOCKED -> UserStrictnessSettings(
                undoPolicy = UndoPolicy.NONE,
                maxSnoozesPerHabitPerDay = 1,
                maxConsecutiveSkips = 0,
                maxSnoozeDurationMinutes = 15
            )
        }
    }
}

data class UserStrictnessSettings(
    val undoPolicy: UndoPolicy,
    val maxSnoozesPerHabitPerDay: Int?,
    val maxConsecutiveSkips: Int?,
    val maxSnoozeDurationMinutes: Int
)
```

**Planned Changes:**
1. Verify/create `StrictnessPreset` enum
2. Add `toUserSettings()` method for mapping
3. Update `ApplyStrictnessPresetUseCase` to use proper mappings
4. Add unit tests for each preset mapping
5. Update onboarding to properly apply selected preset

---

## Section 1.2: Database Schema Updates

### ✅ HabitSchedule Table Extended
- Added quota, weekStartDay, specificDays columns
- Updated insert/update queries

### ⏳ LeavePeriod Table Creation
- Pending implementation (Task 3)

---

## Section 1.3: Repository Layer Completion

### ✅ HabitRepositoryImpl Updated
- Schedule creation with new fields

### ⏳ LeavePeriodRepository
- Pending creation (Task 3)
- Will include:
  - `createLeavePeriod(leavePeriod: LeavePeriod)`
  - `getLeavePeriods(habitId: String): List<LeavePeriod>`
  - `getActiveLeavePeriod(habitId: String, date: LocalDate): LeavePeriod?`
  - `updateLeavePeriod(leavePeriod: LeavePeriod)`
  - `deleteLeavePeriod(id: String)`
  - `endLeavePeriod(id: String, endDate: LocalDate)`

---

## Section 1.4: Dependency Injection Setup (Metro)

**Status:** NOT STARTED

**Estimated Time:** 3-4 hours

**Note:** This task can be done in parallel with domain model work or deferred to later in Phase 1.

### Current State
- Manual dependency injection in `App.kt`
- All repositories and use cases created manually in `remember` blocks
- No DI framework in use

### Planned Implementation

1. **Add Metro Dependencies**
```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
}

dependencies {
    implementation(libs.metro.runtime)
    ksp(libs.metro.compiler)
}
```

2. **Create App Component**
```kotlin
@AppScope
@Component
interface HabitLockAppComponent {
    val database: HabitLockDatabase
    val userRepository: UserRepository
    val habitRepository: HabitRepository
    // ... all repositories
    
    val generateDailyHabitsUseCase: GenerateDailyHabitsUseCase
    // ... all use cases
    
    val onboardingViewModel: OnboardingViewModel
    val todayViewModel: TodayViewModel
    // ... all view models
}
```

3. **Migrate App.kt**
- Remove manual `remember` blocks
- Use Metro-generated component

**Benefits:**
- Cleaner, more maintainable code
- Proper scoping of dependencies
- Compile-time validation of dependency graph
- Easier testing with mock components

---

## Phase 1 Progress Summary

### Completed Tasks (2/4)
- ✅ Task 1.1.1: HabitSchedule domain model with WEEKLY support
- ✅ Task 1.1.2: HabitScore computation model

### Pending Tasks (2/4)
- ⏳ Task 1.1.3: LeavePeriod model for suspension tracking
- ⏳ Task 1.1.4: StrictnessPreset mapping verification

### Optional Task
- ⏳ Task 1.4: Metro dependency injection setup

---

## Overall Testing Status

### Unit Tests Written
- ✅ HabitScoreTest (20 test cases, all passing)

### Unit Tests Needed
- ⏳ HabitScheduleTest (validation, isActiveOn logic)
- ⏳ LeavePeriodTest (date validation, isActiveOn logic)
- ⏳ StrictnessPresetTest (mapping verification)

---

## What's Next

### Immediate Next Steps (Complete Phase 1.1)
1. Implement LeavePeriod model (Task 3)
2. Verify/fix StrictnessPreset mappings (Task 4)
3. Write unit tests for HabitSchedule
4. Write unit tests for LeavePeriod
5. Write unit tests for StrictnessPreset

### After Phase 1.1 Completion
- Decide on Metro DI implementation timing
- Move to Phase 1.2 (Database Schema Updates) if needed
- Move to Phase 2 (Complete Business Logic)

---

## Integration Notes

### For Phase 2.1 (Weekly Habits Support)
The HabitSchedule enhancements are ready for:
- `GenerateDailyHabitsUseCase` updates
- Weekly instance generation logic
- Cadence window tracking

### For Phase 2.2 (Habit Score Calculation)
The HabitScore model is ready for:
- `CalculateHabitScoreUseCase` implementation
- Score persistence strategy decision
- UI integration

### For Phase 2.3 (Leave/Suspension Mode)
Will require:
- LeavePeriod model (Task 3 - pending)
- Use cases for suspension management
- Instance generation filtering

---

## Files Created/Modified Summary

### Created Files (3)
1. `/composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/HabitScore.kt`
2. `/composeApp/src/commonTest/kotlin/com/ricardocosteira/habitlock/domain/models/HabitScoreTest.kt`
3. This implementation document

### Modified Files (5)
1. `/composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/models/HabitSchedule.kt`
2. `/composeApp/src/commonMain/sqldelight/com/ricardocosteira/habitlock/data/database/HabitLock.sq`
3. `/composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/mappers/EntityMappers.kt`
4. `/composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/data/repositories/HabitRepositoryImpl.kt`
5. `/composeApp/src/commonMain/kotlin/com/ricardocosteira/habitlock/domain/usecases/CreateHabitUseCase.kt`

---

## Commit Messages

### Task 1 Commit
```
feat(domain): add WEEKLY cadence support to HabitSchedule

- Add WEEKLY to ScheduleType enum alongside existing DAILY
- Add quota field for completions per cadence window (default: 1)
- Add weekStartDay field for configurable week boundaries (default: MONDAY)
- Add specificDays field for flexible weekly scheduling (optional)
- Add isActiveOn() utility method for date-based schedule filtering
- Update database schema with new HabitSchedule columns
- Update SQL queries (insertSchedule, updateSchedule) with new fields
- Update EntityMappers to handle new field serialization/deserialization
- Update HabitRepositoryImpl to persist new schedule fields
- Extend CreateHabitUseCase.CreateHabitParams with schedule configuration
- Add validation: quota > 0, specificDays non-empty for weekly schedules

All changes maintain backward compatibility with default values.
```

### Task 2 Commit
```
feat(domain): add HabitScore computation model

Add immutable data class for tracking cumulative habit performance
with support for over-completion tracking and score calculation.

- Formula: min(150, (totalCompletions / expectedCompletions) * 100)
- Caps at 150% to recognize 50% over-completion
- Includes computed properties: percentage, isPerfect, isOverCompleted
- Includes helper methods: withIncremented/DecrementedCompletion
- Handles edge cases: zero expected, negative prevention
- 20 comprehensive unit tests (all passing)
- Immutable with defensive validation

Ready for integration with CalculateHabitScoreUseCase in Phase 2.2
```

---

## Notes

- All implementations follow clean architecture principles
- Domain models are pure and framework-independent
- Backward compatibility maintained throughout
- Comprehensive documentation for future team members
- Test coverage being built incrementally
- Ready for Phase 2 business logic implementation
