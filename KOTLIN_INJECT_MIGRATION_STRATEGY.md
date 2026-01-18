# kotlin-inject Migration Strategy - Roadmap Update

**Date:** January 18, 2026  
**Context:** Adding kotlin-inject migration task to development roadmap

---

## Decision: Add kotlin-inject Migration in Phase 3.0

After analyzing the roadmap and project evolution, I've added **Phase 3.0: Migrate to kotlin-inject DI** as the recommended migration point.

---

## Why Phase 3, Not Earlier?

### Phase 1 Status (Current - Manual DI ✅)
- **Dependencies:** 15 repositories, 10 use cases, 6 ViewModels = ~31 components
- **Complexity:** Low to medium
- **Manual DI Status:** Working perfectly, clean, maintainable
- **Decision:** Manual DI appropriate ✅

### Phase 2 Growth (Business Logic)
- **New Components:** ~5 use cases (CalculateHabitScore, Suspend, Unsuspend, Snooze, etc.)
- **Total Dependencies:** ~15 use cases, 15 repositories, 6 ViewModels = ~36 components
- **Complexity:** Medium
- **Manual DI Status:** Still manageable ✅
- **Decision:** Manual DI still sufficient

### Phase 3 Inflection Point (Background Processing) 🎯
- **New Components:**
  - 2-3 Workers (DailyHabitGeneration, EndOfDay, Notification)
  - NotificationManager
  - NotificationScheduler
  - 1-2 BroadcastReceivers (NotificationAction, BootComplete)
  - Multiple Android-specific services
- **Total Dependencies:** ~45+ components
- **Complexity:** High - multiple Android components, scoped injection needed
- **Manual DI Status:** Becomes burdensome ⚠️
- **Worker Integration:** Workers benefit significantly from DI framework
- **Multiple Scopes:** Need app-scope AND worker-scope
- **Decision:** **kotlin-inject migration recommended** 🎯

---

## Key Reasons for Phase 3 Migration

### 1. Worker Dependency Injection
Workers in WorkManager need constructor injection:
```kotlin
// With kotlin-inject - clean and automatic
@Inject
class DailyHabitGenerationWorker(
    context: Context,
    params: WorkerParameters,
    private val generateDailyHabitsUseCase: GenerateDailyHabitsUseCase,
    private val processEndOfDayUseCase: ProcessEndOfDayUseCase
) : CoroutineWorker(context, params)

// With manual DI - requires custom WorkerFactory and boilerplate
class CustomWorkerFactory(
    private val generateUseCase: GenerateDailyHabitsUseCase,
    private val processUseCase: ProcessEndOfDayUseCase
) : WorkerFactory() {
    override fun createWorker(...): ListenableWorker? {
        return when (workerClassName) {
            DailyHabitGenerationWorker::class.java.name -> 
                DailyHabitGenerationWorker(context, params, generateUseCase, processUseCase)
            // ... more workers
        }
    }
}
```

### 2. BroadcastReceiver Injection
Notification action handlers need dependency injection:
```kotlin
@Inject
class NotificationActionReceiver(
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val snoozeHabitUseCase: SnoozeHabitUseCase,
    private val skipHabitUseCase: SkipHabitUseCase
) : BroadcastReceiver()
```

### 3. Multiple Scopes Required
- **App Scope:** Repositories, database (singleton across app lifecycle)
- **Worker Scope:** Worker-specific dependencies (created per worker execution)
- **Activity Scope:** ViewModels (created per screen)

Manual DI becomes complex managing multiple scopes.

### 4. Dependency Count Threshold
- Phase 1-2: ~36 components (manual DI threshold)
- Phase 3+: ~45+ components (DI framework recommended)

### 5. Compile-Time Safety
Phase 3 complexity makes circular dependencies more likely. kotlin-inject catches these at compile-time.

---

## Alternative: Defer to Phase 6

If time-constrained in Phase 3, migration can be deferred to Phase 6 (Polish & Production Readiness).

**Tradeoff:**
- ✅ **Pro:** Focus on features first
- ⚠️ **Con:** More manual wiring boilerplate in Phase 3-5
- ⚠️ **Con:** Worker integration requires custom WorkerFactory
- ⚠️ **Con:** More complex refactoring later with more code

**Recommendation:** Migrate in Phase 3.0 unless severely time-constrained.

---

## Migration Effort Estimate

**Time:** 3-4 hours

**Steps:**
1. Add dependencies (15 min)
2. Convert component to @Component (30 min)
3. Add @Inject to constructors (1 hour)
4. Remove manual wiring (30 min)
5. Create WorkerComponent (45 min)
6. Test and verify (30 min)

**Impact:** Minimal disruption, mostly mechanical changes

---

## Roadmap Updates Made

### 1. Added Phase 3.0
- Full task breakdown for kotlin-inject migration
- Rationale section explaining why at this phase
- Migration time estimate
- Benefits list
- Reference to KOTLIN_INJECT_ASSESSMENT.md

### 2. Updated Phase 3.1
- Added task: "Integrate Workers with DI (if kotlin-inject adopted in 3.0)"

### 3. Added Phase 6 Note
- Reminder about migration if deferred from Phase 3
- Recommendation to complete before production

---

## References

- **Full Analysis:** `KOTLIN_INJECT_ASSESSMENT.md` (400+ lines)
- **Current DI:** `di/HabitLockAppComponent.kt`, `di/AppModule.kt`
- **kotlin-inject Docs:** https://github.com/evant/kotlin-inject

---

## Summary

**Phase 3 is the optimal migration point** because:
1. ✅ Workers need dependency injection
2. ✅ BroadcastReceivers need dependency injection
3. ✅ Multiple scopes become necessary
4. ✅ Dependencies exceed 40+ components
5. ✅ Complexity warrants framework benefits

**The migration is documented and ready to execute when Phase 3 begins.** 🚀
