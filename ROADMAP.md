# HabitLock Development Roadmap

## Overview

This roadmap outlines the development plan for HabitLock, a habit enforcing app built with Kotlin Multiplatform (KMP) targeting Android, iOS, and Desktop. The app follows clean architecture principles with MVVM/MVI patterns.

### Current State Assessment

**Existing Implementation:**
- ✅ Project structure with KMP setup (Android, iOS, JVM)
- ✅ SQLDelight database schema with all core tables
- ✅ Domain models (Habit, HabitInstance, User, etc.)
- ✅ Repository interfaces and implementations
- ✅ Core use cases (Complete, Skip, Undo, GenerateDailyHabits, ProcessEndOfDay)
- ✅ Basic navigation infrastructure
- ✅ Today screen with ViewModel and basic UI
- ✅ Onboarding screens (Philosophy, Strictness, FirstHabit)
- ✅ Calendar screen structure
- ✅ Settings screen structure
- ✅ Habit form for creation/editing
- ✅ Archived habits screen

**Missing/Incomplete:**
- ❌ Notifications system (Android-specific)
- ❌ Background job scheduling (WorkManager integration)
- ❌ Leave/Suspension mode
- ❌ Snooze functionality implementation
- ❌ Habit Score calculation and display
- ❌ Weekly habits support
- ❌ Over-completion handling
- ❌ Calendar day classification logic
- ❌ Comprehensive testing
- ❌ Metro dependency injection setup

---

## Phase 1: Core Foundation Hardening
**Duration: 1-2 weeks**
**Goal: Ensure core data layer and business logic is solid**

### 1.1 Fix and Complete Domain Models

**Tasks:**
- [x] Add `HabitSchedule` domain model with proper cadence (DAILY/WEEKLY) support
- [x] Add `HabitScore` computation model
- [x] Add `LeavePeriod` model for suspension tracking
- [x] Ensure `StrictnessPreset` enum properly maps to undo/skip/snooze limits

**Status:** ✅ COMPLETE (All tasks completed on January 18, 2026)

**Files to modify/create:**
- `domain/models/HabitSchedule.kt` - Add scheduleType field mapping
- `domain/models/HabitScore.kt` - New data class for score computation
- `domain/models/LeavePeriod.kt` - New data class for suspension periods

**Implementation Details:**
```kotlin
// HabitScore.kt
data class HabitScore(
    val totalCompletions: Int,
    val expectedCompletions: Int,
    val overCompletionCap: Int = 150
) {
    val percentage: Int
        get() = minOf(overCompletionCap, (totalCompletions * 100) / maxOf(1, expectedCompletions))
}

// LeavePeriod.kt
data class LeavePeriod(
    val habitId: String,
    val startDate: LocalDate,
    val endDate: LocalDate?
)
```

### 1.2 Database Schema Updates

**Tasks:**
- [x] Add `LeavePeriod` table for tracking suspension periods
- [x] Update HabitSchedule to support WEEKLY cadence properly
- [x] Add indexes for leave period queries

**Status:** ✅ COMPLETE (Completed on January 18, 2026)

**Files modified:**
- `HabitLock.sq` - Added LeavePeriod table, updated HabitSchedule table, added all necessary indexes and queries

### 1.3 Repository Layer Completion

**Tasks:**
- [x] Create `LeavePeriodRepository` interface and implementation
- [x] Add leave period queries to existing repositories
- [x] Update `HabitRepository` to support weekly habits filtering

**Status:** ✅ COMPLETE (Completed on January 18, 2026)

**Files created:**
- `domain/repositories/LeavePeriodRepository.kt`
- `data/repositories/LeavePeriodRepositoryImpl.kt`

**Files modified:**
- `HabitRepositoryImpl.kt` - Updated to handle HabitSchedule with new fields

### 1.4 Dependency Injection Setup (Manual DI)

**Tasks:**
- [x] Create clean DI container with `HabitLockAppComponent`
- [x] Create `AppModule` with lazy initialization for singletons
- [x] Migrate manual dependency creation in `App.kt` to use DI container
- [x] Add Factory pattern for HabitFormViewModel dynamic creation

**Status:** ✅ COMPLETE (Completed on January 18, 2026)

**Implementation Approach:**
- Manual DI with lazy initialization instead of code generation frameworks
- Clean, testable architecture with proper scoping
- Single source of truth for all dependencies
- Factory pattern for ViewModels that need dynamic parameters

**Files created:**
- `di/HabitLockAppComponent.kt` - Main DI container
- `di/AppModule.kt` - Dependency providers with lazy singletons

**Files modified:**
- `App.kt` - Migrated to use DI container
- `presentation/ui/habit/HabitFormViewModel.kt` - Added Factory interface
- `presentation/navigation/HabitLockNavHost.kt` - Updated to use factory with habitId parameter
- `build.gradle.kts` - Cleaned up unnecessary plugin dependencies

**DI Framework Research:**
- **Metro DI:** Evaluated but has limited KMP support with KSP
- **kotlin-inject:** Thoroughly researched - excellent KMP DI framework but not needed yet
  - Full analysis in `KOTLIN_INJECT_ASSESSMENT.md`
  - Mature, production-ready, used by Cash App
  - Compile-time safety, minimal overhead
  - Decision: Manual DI is sufficient for current project size (15 repos, 10 use cases)
  - Reconsider when: 30+ dependencies, team grows, or circular dependency issues arise
- **Manual DI:** Chosen for simplicity, full KMP support, and adequate for current scale

---

## Phase 2: Complete Business Logic
**Duration: 2-3 weeks**
**Goal: Implement all use cases and business rules**

### 2.1 Weekly Habits Support

**Tasks:**
- [x] Update `GenerateDailyHabitsUseCase` to handle weekly instances
- [x] Create `GetWeeklyInstancesUseCase`
- [x] Modify instance generation logic to respect cadence windows

**Status:** ✅ COMPLETE (Completed on January 18, 2026)

**Implementation Details:**
- Weekly instances are created at start of week (based on schedule's weekStartDay)
- Weekly habits track quota fulfillment across the week in single instance
- End-of-week processing marks pending weekly instances as FAILED if quota not met
- Daily habits continue to work as before with per-day instances

**Files Modified:**
- `GenerateDailyHabitsUseCase.kt` - Now handles both DAILY and WEEKLY schedules
- `ProcessEndOfDayUseCase.kt` - Extended to handle end-of-week processing
- `AppModule.kt` - Added GetWeeklyInstancesUseCase to DI

**Files Created:**
- `GetWeeklyInstancesUseCase.kt` - Retrieves instances for current/specific week

**Key Features:**
- Weekly instances created on weekStartDay (configurable per habit)
- Single instance per week tracks quota (e.g., "3 workouts per week")
- Weekly instances date = week start date
- End-of-week automatically marks incomplete as FAILED or COMPLETED based on quota
- GetWeeklyInstancesUseCase provides week-specific views for UI

### 2.2 Habit Score Calculation

**Tasks:**
- [x] Create `CalculateHabitScoreUseCase`
- [x] Integrate score calculation into completion/undo flows
- [x] Add score to Habit table with totalCompletions and expectedCompletions fields

**Status:** ✅ COMPLETE (Completed on January 18, 2026)

**Formula:**
```
HabitScore = min(150, (TotalCompletions / ExpectedCompletions) * 100)
```

**Implementation:**
- Score fields stored in Habit table (totalCompletions, expectedCompletions)
- Incremental updates via repository methods
- CompleteHabitUseCase increments totalCompletions
- UndoHabitUseCase decrements totalCompletions
- GenerateDailyHabitsUseCase increments expectedCompletions
- CalculateHabitScoreUseCase provides on-demand calculation
- Habit model has calculateScore() convenience method

**Files Modified:**
- `HabitLock.sq` - Added score fields and queries
- `domain/models/Habit.kt` - Added score fields
- `domain/repositories/HabitRepository.kt` - Added score methods
- `data/repositories/HabitRepositoryImpl.kt` - Implemented score methods
- `domain/usecases/CalculateHabitScoreUseCase.kt` - NEW
- `domain/usecases/CompleteHabitUseCase.kt` - Updated
- `domain/usecases/UndoHabitUseCase.kt` - Updated
- `domain/usecases/GenerateDailyHabitsUseCase.kt` - Updated
- `domain/usecases/CreateHabitUseCase.kt` - Updated
- `di/AppModule.kt` - Added CalculateHabitScoreUseCase

### 2.3 Leave/Suspension Mode

**Tasks:**
- [x] Create `SuspendHabitUseCase`
- [x] Create `UnsuspendHabitUseCase`
- [x] Update instance generation to skip suspended habits
- [x] Exclude suspended habits from streak calculations

**Status:** ✅ COMPLETE (Completed on January 18, 2026)

**Business Rules:**
- Suspended habits generate SUSPENDED status instances
- No notifications during suspension
- Suspension end date auto-unsuspends

**Implementation:**
- Added SUSPENDED status to HabitStatus enum
- SuspendHabitUseCase creates leave periods with overlap validation
- UnsuspendHabitUseCase ends leave periods early or deletes future ones
- GenerateDailyHabitsUseCase creates SUSPENDED instances during leave periods
- SUSPENDED instances don't increment expectedCompletions (no score impact)
- CompleteHabitUseCase, SkipHabitUseCase reject SUSPENDED instances
- ProcessEndOfDayUseCase skips SUSPENDED instances (no failures)
- Updated TodayScreen UI to display SUSPENDED status

**Files Modified:**
- `domain/models/HabitStatus.kt` - Added SUSPENDED status
- `domain/usecases/SuspendHabitUseCase.kt` - NEW
- `domain/usecases/UnsuspendHabitUseCase.kt` - NEW
- `domain/usecases/GenerateDailyHabitsUseCase.kt` - Updated
- `domain/usecases/CompleteHabitUseCase.kt` - Updated
- `domain/usecases/SkipHabitUseCase.kt` - Updated
- `domain/usecases/ProcessEndOfDayUseCase.kt` - Updated
- `di/AppModule.kt` - Added new use cases
- `presentation/ui/today/TodayScreen.kt` - Updated UI

### 2.4 Snooze Implementation

**Tasks:**
- [x] Create `SnoozeHabitUseCase` implementation
- [x] Integrate snooze limits from user preferences
- [x] Create `ClearSnoozeStateUseCase` for cleanup

**Status:** ✅ COMPLETE (Completed on January 18, 2026)

**Current State:**
- `SnoozeState` table exists (from Phase 1)
- `SnoozeRepository` exists (from Phase 1)
- Use cases now implemented

**Implementation:**
- Completed SnoozeHabitUseCase with validation
- Only PENDING instances can be snoozed
- Enforces user's max snoozes per day limit
- Caps duration to user's max snooze duration
- Tracks snooze count per instance
- Created ClearSnoozeStateUseCase for cleanup
- Added both use cases to DI container

**Files Modified:**
- `domain/usecases/SnoozeHabitUseCase.kt` - Completed implementation
- `domain/usecases/ClearSnoozeStateUseCase.kt` - NEW
- `di/AppModule.kt` - Added snooze use cases

**Note:** Snooze expiration and re-notification will be handled in Phase 3 (Background Processing & Notifications) when WorkManager and notification scheduling are implemented.

**Tests Created:**
- `CalculateHabitScoreUseCaseTest` - 7 tests
- `SuspendHabitUseCaseTest` - 7 tests
- `UnsuspendHabitUseCaseTest` - 8 tests
- `SnoozeHabitUseCaseTest` - 9 tests
- `ClearSnoozeStateUseCaseTest` - 7 tests
- Total: 38 new tests, all passing

### 2.5 Over-Completion Handling

**Tasks:**
- [x] Allow completing beyond quota
- [x] Track over-completion in HabitCompletionEvent
- [x] Add over-completion to score calculation
- [ ] Optional: Prompt to update quota after consistent over-completion (UI feature for Phase 4)

**Status:** ✅ COMPLETE (Core functionality implemented in Phase 2.2)

**Implementation:**
- `executeQuantitative` allows adding progress beyond quota
- All completions tracked as HabitCompletionEvents
- HabitScore supports over-completion up to 150% (configurable)
- HabitScore provides `overCompletionCount` property
- Score calculation automatically handles over-completion
- UI prompt for quota adjustment deferred to Phase 4

**Note:** The core over-completion functionality was already implemented as part of Phase 2.2 (Habit Score Calculation). The only remaining task is the optional UI prompt to suggest quota updates, which will be implemented in Phase 4 (UI Completion).

---

## Phase 3: Background Processing & Notifications (Android)
**Duration: 2-3 weeks**
**Goal: Implement notification system and background jobs**
**Status:** ✅ COMPLETE (Completed on January 18, 2026)

### 3.0 Migrate to kotlin-inject DI (Recommended)

**Rationale:**
Phase 3 introduces significant complexity that makes DI framework beneficial:
- Workers need dependency injection (WorkManager + kotlin-inject integration)
- BroadcastReceivers need dependency injection
- Multiple Android-specific components (NotificationManager, schedulers)
- Project will exceed 25+ dependencies (threshold for DI framework value)
- Scoped injection becomes important (app-scope vs worker-scope)

**Tasks:**
- [ ] Add kotlin-inject dependencies and KSP plugin to build.gradle.kts
- [ ] Convert `HabitLockAppComponent` to `@Component` annotation
- [ ] Add `@Inject` to repository, use case, and ViewModel constructors
- [ ] Remove manual wiring from `AppModule` (let kotlin-inject generate)
- [ ] Create `WorkerComponent` for Worker-scoped dependencies
- [ ] Update `App.kt` to use generated component factory
- [ ] Add compile-time circular dependency detection

**Benefits at this stage:**
- Workers integrate cleanly with dependency injection
- Android-specific components (BroadcastReceivers) can be injected
- Reduced boilerplate for 25+ dependencies
- Compile-time validation of dependency graph
- Multiple scopes for different lifecycle requirements

**Migration time:** 3-4 hours

**Reference:** See `KOTLIN_INJECT_ASSESSMENT.md` for detailed implementation guide

**Alternative:** Can defer to Phase 6 if time-constrained, but will require more manual wiring in Phase 3-5

---

### 3.1 WorkManager Integration

**Tasks:**
- [x] Create `DailyHabitGenerationWorker`
- [x] Create `EndOfDayProcessingWorker`
- [x] Create `NotificationScheduler` service
- [x] Schedule workers on app startup
- [x] Integrate Workers with DI (manual DI approach used)

**Status:** ✅ COMPLETE (Completed on January 18, 2026)

**Implementation Details:**
- Created `DailyHabitGenerationWorker` that runs at start of day to mark pending habits as failed and generate new instances
- Created `EndOfDayProcessingWorker` that runs at 10 PM to send grace period notifications
- Implemented `NotificationScheduler` service using AlarmManager for precise notification timing
- Created `WorkManagerInitializer` to schedule periodic workers on app startup
- Workers instantiate AppModule manually (no kotlin-inject migration needed yet)
- Periodic workers run every 24 hours with calculated initial delays

**Files to create:**
- `androidMain/.../workers/DailyHabitGenerationWorker.kt`
- `androidMain/.../workers/EndOfDayProcessingWorker.kt`
- `androidMain/.../notifications/NotificationScheduler.kt`

**Implementation Details:**
```kotlin
class DailyHabitGenerationWorker(
    context: Context,
    params: WorkerParameters,
    private val generateDailyHabitsUseCase: GenerateDailyHabitsUseCase,
    private val processEndOfDayUseCase: ProcessEndOfDayUseCase
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        processEndOfDayUseCase.execute() // Mark yesterday's pending as failed
        generateDailyHabitsUseCase.execute() // Generate today's instances
        return Result.success()
    }
}
```

### 3.2 Notification System

**Tasks:**
- [x] Create notification channels (habits, reminders, grace period)
- [x] Implement `HabitNotificationManager`
- [x] Add notification action buttons (+1, Snooze, Skip)
- [x] Handle notification actions via BroadcastReceiver

**Status:** ✅ COMPLETE (Completed on January 18, 2026)

**Implementation Details:**
- Created 3 notification channels: Habit Reminders (HIGH), Grace Period (HIGH), Daily Summary (LOW)
- Implemented `HabitNotificationManager` with methods for habit reminders, grace period, and daily summary
- Action buttons vary by habit type:
  - Binary habits: "Complete", "Snooze", "Skip"
  - Quantitative habits: "+1", "Snooze", "Skip"
  - Grace period: "Complete"/"+ 1", "Skip" (no snooze)
- Created `NotificationActionReceiver` to handle button clicks
- Notifications auto-cancel after action is taken
- Snooze action reschedules notification for 15 minutes later

**Files to create:**
- `androidMain/.../notifications/NotificationChannels.kt`
- `androidMain/.../notifications/HabitNotificationManager.kt`
- `androidMain/.../notifications/NotificationActionReceiver.kt`

**Notification Types:**
1. **Habit Reminder** - User-defined time, shows progress
2. **Grace Period** - After cadence window ends, before failure
3. **Daily Summary** (optional) - End of day summary

**Action Buttons:**
```kotlin
// For daily/quantitative habits
PendingIntent for "+1" -> CompleteHabitUseCase.executeQuantitative(1)
PendingIntent for "Snooze" -> SnoozeHabitUseCase.execute()
PendingIntent for "Skip" -> SkipHabitUseCase.execute()
```

### 3.3 Notification Scheduling

**Tasks:**
- [x] Schedule notifications based on HabitReminder settings
- [x] Handle timezone changes (re-schedule all)
- [x] Cancel notifications for suspended/completed habits
- [ ] Support multiple reminder times per habit (deferred to Phase 4)

**Status:** ✅ COMPLETE (Core functionality implemented on January 18, 2026)

**Implementation Details:**
- Implemented `NotificationScheduler` using AlarmManager for precise timing
- Uses exact alarms (SCHEDULE_EXACT_ALARM permission) with fallback to inexact alarms
- Created `TimezoneChangeReceiver` to handle timezone changes and reschedule all notifications
- Created `BootReceiver` to reschedule after device restart
- Notifications cancelled when habits are completed, skipped, or suspended
- Support for grace period notifications (sent at 10 PM)
- Support for snooze reminders (user-configurable duration)
- Multiple reminder times per habit deferred to Phase 4 UI work

**Implementation Considerations:**
- Use AlarmManager for precise timing
- Consider exact alarms for API 31+
- Persist scheduled alarm IDs for cancellation

### 3.4 Update AndroidManifest

**Tasks:**
- [x] Add notification permissions (POST_NOTIFICATIONS for API 33+)
- [x] Add SCHEDULE_EXACT_ALARM permission
- [x] Register BroadcastReceivers
- [x] Register Workers with WorkManager

**Status:** ✅ COMPLETE (Completed on January 18, 2026)

**Implementation Details:**
- Added permissions: POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM, RECEIVE_BOOT_COMPLETED, WAKE_LOCK
- Registered 4 BroadcastReceivers:
  - NotificationReceiver (handles scheduled notifications from AlarmManager)
  - NotificationActionReceiver (handles notification action button clicks)
  - BootReceiver (reschedules on device boot)
  - TimezoneChangeReceiver (reschedules on timezone changes)
- WorkManager automatically registered by androidx.work library
- All receivers properly configured with intent filters

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<receiver android:name=".notifications.NotificationActionReceiver" />
<receiver android:name=".notifications.BootReceiver">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

---

## Phase 4: UI Completion
**Duration: 2-3 weeks**
**Goal: Complete all screens and UI interactions**
**Status:** ✅ CORE COMPLETE (Core features completed on January 18, 2026, polish items remain)

### 4.1 Today Screen Enhancements

**Tasks:**
- [x] Display Habit Score alongside streak
- [x] Show daily vs. weekly habits in separate sections
- [x] Implement suspended habits section with early unsuspend
- [ ] Add swipe actions (complete, skip, snooze) - DEFERRED (nice-to-have)
- [x] Show progress bars for quantitative habits

**Status:** ✅ COMPLETE (Completed on January 18, 2026, swipe actions deferred)

**UI Components to create/modify:**
- `HabitCard.kt` - Enhanced card with score display
- `HabitProgressBar.kt` - Visual quota progress
- `SwipeableHabitCard.kt` - Swipe actions wrapper

### 4.2 Calendar Screen Implementation

**Tasks:**
- [x] Implement day classification logic
- [x] Color-code days (PERFECT, BEST_EFFORT, PARTIAL, ROUGH_DAY, FAILED, FUTURE)
- [ ] Show day detail on tap (list of habits for that day) - DEFERRED (requires new screen)
- [x] Month navigation

**Status:** ✅ COMPLETE (Core features completed on January 18, 2026, day detail deferred)

**Day Classification Logic:**
```kotlin
enum class DayClassification {
    PERFECT,     // All completed, no failures
    BEST_EFFORT, // All completed with suspended habits
    PARTIAL,     // Some completed/skipped, at least one incomplete
    ROUGH_DAY,   // Some completed/skipped, at least one failure
    FAILED,      // All failed
    FUTURE       // Not yet reached
}

fun classifyDay(instances: List<HabitInstance>): DayClassification {
    val nonSuspended = instances.filter { it.status != HabitStatus.SUSPENDED }
    if (nonSuspended.isEmpty()) return DayClassification.BEST_EFFORT
    
    val completed = nonSuspended.count { it.status == HabitStatus.COMPLETED }
    val failed = nonSuspended.count { it.status == HabitStatus.FAILED }
    val pending = nonSuspended.count { it.status == HabitStatus.PENDING }
    
    return when {
        failed == nonSuspended.size -> DayClassification.FAILED
        completed == nonSuspended.size -> {
            if (instances.any { it.status == HabitStatus.SUSPENDED })
                DayClassification.BEST_EFFORT
            else DayClassification.PERFECT
        }
        failed > 0 -> DayClassification.ROUGH_DAY
        pending > 0 -> DayClassification.PARTIAL
        else -> DayClassification.PARTIAL
    }
}
```

### 4.3 Settings Screen Completion

**Tasks:**
- [ ] Implement strictness preset switching - DEFERRED (backend ready, needs UI)
- [ ] Add undo policy configuration - DEFERRED (backend ready, needs UI)
- [ ] Add snooze settings (max count, duration) - DEFERRED (backend ready, needs UI)
- [ ] Add skip settings (max consecutive) - DEFERRED (backend ready, needs UI)
- [ ] Add notification toggle per habit - DEFERRED (future enhancement)

**Status:** ⏸ DEFERRED (Backend logic complete, UI enhancements can be added later)

**Sections:**
1. Strictness Level (Flexible/Balanced/Locked)
2. Undo Policy
3. Snooze Settings
4. Skip Settings
5. Notification Preferences
6. App Info/About

### 4.4 Habit Form Improvements

**Tasks:**
- [x] Add cadence selection (Daily/Weekly)
- [x] Add quota input for quantitative habits
- [ ] Add notification time picker - DEFERRED (uses default 9 AM)
- [ ] Add leave mode scheduling - DEFERRED (can use separate leave UI)
- [ ] Validation and error states - PARTIALLY (basic validation exists)

**Status:** ✅ CORE COMPLETE (Cadence and quota implemented on January 18, 2026)

### 4.5 Leave Mode UI

**Tasks:**
- [ ] Add "Set Leave" action in habit menu - DEFERRED (backend ready, needs UI integration)
- [ ] Date picker for start/end dates - DEFERRED (backend ready, needs date picker component)
- [ ] Early unsuspend via swipe action - DEFERRED (manual unsuspend can be added)
- [x] Visual indicator in habit list - COMPLETE (suspended habits shown in separate section)

**Status:** ⏸ DEFERRED (Backend logic complete, visual indicators done, full UI can be added later)

---

## Phase 5: Testing
**Duration: 2 weeks**
**Goal: Comprehensive test coverage**

### 5.1 Unit Tests

**Use Cases to test:**
- [ ] `CompleteHabitUseCase` - binary and quantitative completion
- [ ] `SkipHabitUseCase` - skip limits enforcement
- [ ] `UndoHabitUseCase` - undo policy enforcement
- [ ] `SnoozeHabitUseCase` - snooze limits
- [ ] `GenerateDailyHabitsUseCase` - daily and weekly generation
- [ ] `ProcessEndOfDayUseCase` - failure marking
- [ ] `CalculateHabitScoreUseCase` - score calculation
- [ ] `SuspendHabitUseCase` / `UnsuspendHabitUseCase`

**Test Structure:**
```kotlin
class CompleteHabitUseCaseTest {
    // Given-When-Then convention
    
    @Test
    fun `given binary habit when completing then status becomes COMPLETED`() {
        // Given
        val mockHabitInstanceRepository = ...
        val inputInstance = createPendingInstance()
        
        // When
        val actualResult = useCase.executeBinary(inputInstance.id, CompletionSource.IN_APP)
        
        // Then
        assertTrue(actualResult.isSuccess)
        assertEquals(HabitStatus.COMPLETED, actualResult.getOrNull()?.status)
    }
}
```

### 5.2 Integration Tests

**ViewModel Tests:**
- [ ] `TodayViewModelTest` - state management, event emission
- [ ] `OnboardingViewModelTest` - flow completion
- [ ] `CalendarViewModelTest` - date range loading
- [ ] `SettingsViewModelTest` - preference updates

**Repository Tests:**
- [ ] Database read/write operations
- [ ] Mapper correctness

### 5.3 Test Infrastructure

**Tasks:**
- [ ] Create test fakes for repositories
- [ ] Create test builders for domain models
- [ ] Add Turbine for Flow testing (already in dependencies)

---

## Phase 6: Polish & Production Readiness
**Duration: 1-2 weeks**
**Goal: App ready for release**

**Note:** If kotlin-inject migration was deferred from Phase 3.0, consider implementing it now before production release for better maintainability and testability of the codebase going forward.

### 6.1 Error Handling

**Tasks:**
- [ ] Global error handling strategy
- [ ] User-friendly error messages
- [ ] Crash reporting integration (optional)

### 6.2 Performance Optimization

**Tasks:**
- [ ] Lazy loading for large habit lists
- [ ] Pagination for calendar history
- [ ] Database query optimization
- [ ] Memory profiling

### 6.3 UI/UX Polish

**Tasks:**
- [ ] Loading states and skeletons
- [ ] Empty states with illustrations
- [ ] Animations and transitions
- [ ] Accessibility (content descriptions, contrast)
- [ ] Dark mode support

### 6.4 Platform-Specific Polish

**Android:**
- [ ] Adaptive icons
- [ ] Predictive back gesture support
- [ ] Edge-to-edge UI
- [ ] Material 3 dynamic color

**iOS (if targeting):**
- [ ] iOS-specific notification handling
- [ ] Background app refresh
- [ ] Widget support (stretch goal)

---

## Phase 7: Future Enhancements (Post-MVP)
**Goal: Features for future versions**

### Potential Features:
- Cloud sync / backup
- Habit templates/presets
- Statistics dashboard
- Export/import data
- Apple Watch / Wear OS companion
- Widgets
- AI-based insights

---

## Appendix: Technical Debt & Notes

### Current Issues to Address:
1. **Typo in package name**: `repositoriHamkdir` in domain folder should be cleaned
2. **Missing Metro integration**: Currently using manual DI in App.kt
3. **Incomplete test coverage**: Only one test file exists
4. **No error boundary**: App may crash on unhandled exceptions

### Dependencies to Add:
```toml
# For notifications (Android)
androidx-core = { module = "androidx.core:core-ktx" }  # Already present

# For testing
mockk = { module = "io.mockk:mockk", version = "1.13.9" }
robolectric = { module = "org.robolectric:robolectric", version = "4.11.1" }
```

### Architecture Decisions:
- **State Management**: Single state class per screen (MVVM)
- **Navigation**: Custom Route sealed interface (consider migrating to Navigation 3)
- **Data Flow**: Compose → ViewModel → UseCase → Repository → SQLDelight
- **Platform Code**: Use expect/actual for platform-specific implementations

---

## Summary Timeline

| Phase | Duration | Focus |
|-------|----------|-------|
| 1 | 1-2 weeks | Foundation hardening |
| 2 | 2-3 weeks | Business logic completion |
| 3 | 2-3 weeks | Notifications & background jobs |
| 4 | 2-3 weeks | UI completion |
| 5 | 2 weeks | Testing |
| 6 | 1-2 weeks | Polish & production |

**Total Estimated Time: 10-15 weeks**

---

## MVP Acceptance Checklist

- [ ] User can define habits (daily/weekly, binary/quantitative)
- [ ] Habits generate daily/weekly instances
- [ ] Notifications fire and actions work
- [ ] Snooze/skip/undo behave correctly
- [ ] Streaks and HabitScore update accurately
- [ ] Calendar reflects history truthfully
- [ ] Timezone changes do not corrupt data
- [ ] Settings can be modified after onboarding
- [ ] Leave mode suspends habits correctly
- [ ] Over-completion is tracked and reflected in score
