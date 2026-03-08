# AGENTS.md — HabitLock Project Knowledge Base

This file captures standing decisions, open questions and their answers, so AI agents resuming work in a new session can immediately understand the project context without re-reading all the spec files.

---

## Project Overview

**HabitLock** is a KMP (Kotlin Multiplatform) app targeting Android (primary), iOS, and JVM desktop.  
Tech stack: Compose Multiplatform · SQLDelight · kotlin-inject · WorkManager · Kotlin Coroutines/Flow.

Architecture: Clean Architecture · MVVM/MVI · Repository pattern · Feature-package layout.

---

## Current State (as of 2026-03-07)

| Phase | Status |
|-------|--------|
| Phase 1 – Core Foundation | ✅ Complete |
| Phase 2 – Business Logic | ✅ Complete |
| Phase 3 – Notifications & WorkManager (Android) | ✅ Complete |
| Phase 4 – UI Completion (core) | ✅ Complete |
| Phase 5 – Testing | ❌ Barely started (8 test files out of 13+ use cases) |
| Phase 6 – Polish & Production | ❌ Not started |

**Deferred UI items still pending:**
- Settings screen: strictness preset switching, undo/snooze/skip settings UI (backend ready)
- Calendar: day-detail tap screen
- Habit form: notification time picker, leave mode scheduling
- Leave Mode: full scheduling UI (backend ready; visual indicators done)

---

## Open Questions

> These questions were asked during the 2026-03-07 code-quality session.
> Fill in the answers below; the answers are then binding for future sessions.

### Q1 — Platform Targets

**Question:** iOS and JVM are listed as KMP targets, but:
- `MainViewController.kt` (iOS) has its entire body commented out.
- `main.kt` (JVM) calls `App()` without the required `appComponent` parameter (broken).

**Are iOS and JVM active targets, or is Android the sole priority for the MVP?**

**Answer:** _[TODO: fill in]_

---

### Q2 — Navigation Component 3

**Question:** The navigation system is a hand-rolled `Route` sealed interface + `var currentRoute by remember` in `HabitLockNavHost.kt`. The roadmap mentions eventually migrating to "Navigation 3 NavHost".

**Is there a plan/timeline to adopt Navigation Component 3, or should manual navigation be maintained and improved instead?**

**Answer:** _[TODO: fill in]_

---

### Q3 — Worker / BroadcastReceiver DB Strategy

**Question:** `DailyHabitGenerationWorker` and `NotificationActionReceiver` both create a fresh `AppModule(driverFactory)` on every execution, which means a new SQLite connection per invocation. This is separate from the `HabitLockAppComponent` singleton used by the app process.

**Is this intentional (simple, works for MVP), or should workers share the app's DI component (e.g. via Application-level component storage)?**

**Answer:** _[TODO: fill in]_

---

### Q4 — Phase 5 vs Remaining Deferred UI

**Question:** Phase 5 (testing) is the documented next step, but several useful UI features are still deferred (Settings screen controls, Leave Mode UI, Calendar day-detail). 

**What is the priority order: (a) testing first, then deferred UI; (b) complete deferred UI first; (c) parallel?**

**Answer:** _[TODO: fill in]_

---

### Q5 — Test Double Strategy

**Question:** The coding guidelines say "use test doubles/fakes to simulate dependencies", but the existing tests use MockK (`mockk<>()`, `coEvery`). 

**Should future tests continue using MockK, or migrate to hand-written fakes?**

**Answer:** _[TODO: fill in]_

---

## Standing Decisions (Already Made)

| Topic | Decision | Rationale |
|-------|----------|-----------|
| DI Framework | kotlin-inject (compile-time KSP) | Phase 3.0; replaces manual `AppModule` for main app. Workers/Receivers still use manual `AppModule`. |
| Database | SQLDelight 2.x | Type-safe, KMP-native. README incorrectly says "Room" — ignore README. |
| Navigation | Manual `Route` sealed interface + `var currentRoute` | Navigation 3 deferred; see Q2 above. |
| State | `StateFlow` + `SharedFlow` (events) per ViewModel | MVVM/MVI hybrid. Single state class per screen. |
| UUID generation | `expect/actual fun generateUuid()` | Two separate declarations exist: one in `com.ricardocosteira.habitlock` (App.kt) and one `internal` in `com.ricardocosteira.habitlock.data.repositories` (UserRepositoryImpl.kt). Use cases receive a `UuidProvider` via DI instead of calling the raw function. |
| KSP version | `ksp = "2.3.4"` with `kotlin = "2.3.0"` | Verify compatibility; KSP version should align with Kotlin version. |

---

## Known Technical Debt

1. **Duplicate `toLocalDate`/`todayIn` helper** — private extension function copied into `TodayViewModel`, `CalendarViewModel`, `ProcessEndOfDayUseCase`, `HabitFormViewModel`, `OnboardingViewModel`, and `GenerateDailyHabitsUseCase`. **Fixed 2026-03-07**: extracted to `util/DateExtensions.kt`.
2. **`Greeting.kt` / `Platform.kt` dead code** — KMP template boilerplate, not used anywhere. **Fixed 2026-03-07**: deleted.
3. **`TodayHabitUiModel.cadence: String`** — raw string comparison for `isDaily`/`isWeekly`. **Fixed 2026-03-07**: changed to `ScheduleType`.
4. **`ComposeAppCommonTest.kt` placeholder test** — `assertEquals(3, 1+2)` assertion should be replaced with real tests. **Fixed 2026-03-07**: removed placeholder.
5. **JVM `main.kt` broken** — `App()` called without required `appComponent` parameter; `actual fun rememberDatabaseDriverFactory()` without matching `expect`. **Fixed 2026-03-07**: wired App() with proper appComponent, removed orphan actual fun.
6. **iOS `MainViewController`** — body commented out. Status depends on answer to Q1.
7. **`App.kt` called `appComponent()` lambda 7 times** — each `remember {}` block re-invoked the factory lambda. **Fixed 2026-03-07**: resolved component once with `val component = remember { appComponent() }`.
8. **`AppModule.provideXxxUseCase()` non-singleton** — creates new use-case instances on every call. Acceptable for Workers (short-lived), but should be noted.
9. **`NotificationActionReceiver` scope leaks** — `CoroutineScope(SupervisorJob())` field on a `BroadcastReceiver` is never cancelled. Low risk since `goAsync()` + `pendingResult.finish()` bound the work.
10. **Phase 5 testing nearly absent** — `CompleteHabitUseCase`, `SkipHabitUseCase`, `UndoHabitUseCase`, `GenerateDailyHabitsUseCase`, `ProcessEndOfDayUseCase` have zero test coverage.
11. **Settings screen UI incomplete** — strictness switching, undo/snooze/skip settings deferred. Backend is 100% ready.
12. **`UnsuspendHabitUseCase` tests use hardcoded future dates** — `Clock.System` is not injected, so tests that depend on "today" used `LocalDate(2099, ...)` as a workaround. Proper fix: inject `Clock` into the use case (and all other use cases that call `Clock.System.now()`) for deterministic tests.
13. **`README.md` tech stack info** — was incorrect (Room → SQLDelight, Metro → kotlin-inject). **Fixed 2026-03-07**.

---

## File Map (key files quick-reference)

```
commonMain
  App.kt                          — Entry point; wires DI component to composable tree
  di/HabitLockAppComponent.kt     — kotlin-inject @Component (main process DI)
  domain/models/                  — Pure data classes & enums (Habit, HabitInstance, …)
  domain/repositories/            — Repository interfaces (contracts only)
  domain/usecases/                — Business logic; each class = one use case
  data/repositories/              — SQLDelight implementations
  data/mappers/EntityMappers.kt   — DB entity ↔ domain model mappings
  presentation/models/            — UI models (TodayHabitUiModel, CalendarDayUiModel)
  presentation/navigation/        — Route.kt + HabitLockNavHost.kt
  presentation/ui/                — Composable screens + ViewModels per feature
  util/DateExtensions.kt          — Shared Instant.toLocalDate() / Clock.System.todayIn()

androidMain
  MainActivity.kt                 — Android entry point
  di/AppModule.kt                 — Manual DI for Workers/Receivers (separate from main DI)
  notifications/                  — HabitNotificationManager, channels, receivers
  workers/                        — DailyHabitGenerationWorker, EndOfDayProcessingWorker

commonTest
  domain/usecases/                — Use case unit tests (MockK-based)
  domain/models/                  — Domain model tests
```


