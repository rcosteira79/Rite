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
- [ ] Update `GenerateDailyHabitsUseCase` to handle weekly instances
- [ ] Create `GetWeeklyInstancesUseCase`
- [ ] Modify instance generation logic to respect cadence windows

**Implementation Details:**
- Weekly instances should be created at start of week (configurable: Sunday/Monday)
- Track weekly quota fulfillment across the week
- End-of-week processing similar to end-of-day

### 2.2 Habit Score Calculation

**Tasks:**
- [ ] Create `CalculateHabitScoreUseCase`
- [ ] Integrate score calculation into completion/undo flows
- [ ] Add score to HabitInstance or create separate score tracking table

**Formula:**
```
HabitScore = min(150, (TotalCompletions / ExpectedCompletions) * 100)
```

**Considerations:**
- Calculate incrementally vs. on-demand
- Cache scores for performance
- Handle archived habits in historical score

### 2.3 Leave/Suspension Mode

**Tasks:**
- [ ] Create `SuspendHabitUseCase`
- [ ] Create `UnsuspendHabitUseCase`
- [ ] Update instance generation to skip suspended habits
- [ ] Exclude suspended habits from streak calculations

**Business Rules:**
- Suspended habits generate SUSPENDED status instances
- No notifications during suspension
- Suspension end date auto-unsuspends

### 2.4 Snooze Implementation

**Tasks:**
- [ ] Create `SnoozeHabitUseCase` implementation
- [ ] Integrate snooze limits from user preferences
- [ ] Handle snooze expiration and re-notification

**Current State:**
- `SnoozeState` table exists
- `SnoozeRepository` exists
- Missing use case implementation

**Implementation:**
```kotlin
class SnoozeHabitUseCase(
    private val snoozeRepository: SnoozeRepository,
    private val userRepository: UserRepository
) {
    suspend fun execute(instanceId: String, durationMinutes: Int): Result<SnoozeState> {
        val user = userRepository.getUser() ?: return Result.failure(...)
        val currentSnooze = snoozeRepository.getSnoozeState(instanceId)
        
        // Check snooze limits
        if (user.maxSnoozesPerHabitPerDay != null && 
            currentSnooze?.snoozeCount ?: 0 >= user.maxSnoozesPerHabitPerDay) {
            return Result.failure(SnoozeLimitReachedException())
        }
        
        // Create/update snooze state
        // Schedule re-notification
    }
}
```

### 2.5 Over-Completion Handling

**Tasks:**
- [ ] Allow completing beyond quota
- [ ] Track over-completion in HabitCompletionEvent
- [ ] Add over-completion to score calculation
- [ ] Optional: Prompt to update quota after consistent over-completion

---

## Phase 3: Background Processing & Notifications (Android)
**Duration: 2-3 weeks**
**Goal: Implement notification system and background jobs**

### 3.1 WorkManager Integration

**Tasks:**
- [ ] Create `DailyHabitGenerationWorker`
- [ ] Create `EndOfDayProcessingWorker`
- [ ] Create `NotificationScheduler` service
- [ ] Schedule workers on app startup

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
- [ ] Create notification channels (habits, reminders, grace period)
- [ ] Implement `HabitNotificationManager`
- [ ] Add notification action buttons (+1, Snooze, Skip)
- [ ] Handle notification actions via BroadcastReceiver

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
- [ ] Schedule notifications based on HabitReminder settings
- [ ] Handle timezone changes (re-schedule all)
- [ ] Cancel notifications for suspended/completed habits
- [ ] Support multiple reminder times per habit

**Implementation Considerations:**
- Use AlarmManager for precise timing
- Consider exact alarms for API 31+
- Persist scheduled alarm IDs for cancellation

### 3.4 Update AndroidManifest

**Tasks:**
- [ ] Add notification permissions (POST_NOTIFICATIONS for API 33+)
- [ ] Add SCHEDULE_EXACT_ALARM permission
- [ ] Register BroadcastReceivers
- [ ] Register Workers with WorkManager

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

### 4.1 Today Screen Enhancements

**Tasks:**
- [ ] Display Habit Score alongside streak
- [ ] Show daily vs. weekly habits in separate sections
- [ ] Implement suspended habits section with early unsuspend
- [ ] Add swipe actions (complete, skip, snooze)
- [ ] Show progress bars for quantitative habits

**UI Components to create/modify:**
- `HabitCard.kt` - Enhanced card with score display
- `HabitProgressBar.kt` - Visual quota progress
- `SwipeableHabitCard.kt` - Swipe actions wrapper

### 4.2 Calendar Screen Implementation

**Tasks:**
- [ ] Implement day classification logic
- [ ] Color-code days (PERFECT, BEST_EFFORT, PARTIAL, ROUGH_DAY, FAILED, FUTURE)
- [ ] Show day detail on tap (list of habits for that day)
- [ ] Month navigation

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
- [ ] Implement strictness preset switching
- [ ] Add undo policy configuration
- [ ] Add snooze settings (max count, duration)
- [ ] Add skip settings (max consecutive)
- [ ] Add notification toggle per habit

**Sections:**
1. Strictness Level (Flexible/Balanced/Locked)
2. Undo Policy
3. Snooze Settings
4. Skip Settings
5. Notification Preferences
6. App Info/About

### 4.4 Habit Form Improvements

**Tasks:**
- [ ] Add cadence selection (Daily/Weekly)
- [ ] Add quota input for quantitative habits
- [ ] Add notification time picker
- [ ] Add leave mode scheduling
- [ ] Validation and error states

### 4.5 Leave Mode UI

**Tasks:**
- [ ] Add "Set Leave" action in habit menu
- [ ] Date picker for start/end dates
- [ ] Early unsuspend via swipe action
- [ ] Visual indicator in habit list

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
