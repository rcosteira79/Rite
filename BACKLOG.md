# Follow-up Work

## Design system

- [ ] HabitDetailAction stepper visual fidelity — match /tmp/rite-design/rite/project/styles/rite.css:994-1027 (64dp buttons, 8dp corner, 6dp outer padding, 30sp value, 0.18em unit letter-spacing). Currently ported as-is from pre-v2 inline helpers.
- [ ] HabitDetail timezone consistency — screen uses `TimeZone.currentSystemDefault()` for the week-range label; VM uses `user.timezone` for heatmap data. They can disagree at ISO-week boundaries for users in non-local timezones. Cleanest fix: expose `today: LocalDate` on `HabitDetailUiModel`, computed in the VM with `user.timezone`, and drive the screen off it (also removes the `remember { today = ... }` staleness risk when the app is open past midnight).
- [ ] Tapestry test instability — `Tapestry` reads `Clock.System.todayIn(...)` directly, so `TapestryScreenshotTest` and `HabitDetailScreenScreenshotTest` goldens drift forward as wall-clock time advances and need periodic re-recording. Lift `today: LocalDate` to a parameter so the heatmap window is fixture-driven (overlaps with the timezone-consistency item above; do them together).
- [ ] `TodayScreenScreenshotTest` non-empty cases fail with `IllegalStateException: LocalSharedTransitionScope not provided` since `cce69ad` added the FAB shared-element transform. The test composes `TodayScreen` standalone without a `SharedTransitionLayout` / `LocalNavAnimatedContentScope` provider. Wrap with both in the test (or expose a test-friendly entry point that skips the shared-bounds modifier when the scope is absent).

## Post-MVP Polish

- [ ] Flexible weekly habits — complete N times per week on any day, no specific days assigned
- [ ] Day-detail calendar view (tap a day to see that day's habits)
- [x] Habit detail screen (streaks, score, heatmap, actions)
- [ ] Leave/suspension mode UI (domain logic complete, needs UI entry point)
- [x] Notification permission onboarding screen
- [x] Habit form IME handling (keyboard occludes bottom buttons)
- [ ] Fix weekly habits changing daily target to times/week instead of keeping it times/day
- [ ] Animated collapsing toolbar transition on Today screen — ring + percentage move to collapsed position, ring arc unfurls into horizontal line that slides off-screen right, "Done" fades in beneath percentage
- [ ] Animate title in new/edit habit screens
- [ ] Check spec_notifications for missing notification features
- [ ] Discard confirmation on create habit screen; on edit screen, only prompt if there are unsaved changes
- [ ] Persist daily target value when switching between binary and quantitative habit types
- [ ] Animate complete button and metrics in habit detail screen
- [ ] Rethink Habit Score — should reach 100% after completing daily for the recommended habit-forming period (~66 days?). Currently overshoots well past 100%. Research and redesign the formula
- [ ] Wrap HabitDetailViewModel repository dependencies behind use cases returning Result

## Code Quality — from assessment (2026-04-08)

### Critical
- [ ] Wrap streak/completion updates in a database transaction in CompleteHabit, UndoHabit, UndoLastIncrement to prevent race conditions
- [ ] Clear SnoozeState on completion/skip — inject ClearSnoozeState into CompleteHabit and SkipHabit
- [ ] Compute consecutive skips dynamically at skip-time instead of relying on the stale consecutiveSkipsAtCreation snapshot

### High
- [ ] Inject Clock into all time-dependent use cases (UnsuspendHabit, GenerateDailyHabits, ProcessEndOfDay, SnoozeHabit) for deterministic testing
- [ ] Add error state to HabitDetailState — currently stays loading forever if data is missing
- [ ] Add fallback for enum deserialization in EntityMappers (valueOf crashes on unexpected DB values)
- [ ] Add tests for DailyHabitGenerationWorker and EndOfDayProcessingWorker

### Medium
- [ ] JVM DatabaseDriverFactory uses IN_MEMORY with manual Schema.create() — skips migrations, desktop users lose data on schema changes
- [ ] Standardise ViewModel DI pattern — Factory for parameterised VMs, direct @Inject for others, but AppScope lifetime is semantically wrong for all
- [ ] Add composite (habitId, date) index on HabitInstance for the most common query pattern
- [ ] Add init-block validation to HabitReminder (FIXED requires time, PERIODIC requires intervalMinutes)
- [ ] Standardise event/error patterns in presentation — some use resource IDs, others raw strings
- [ ] Verify ON DELETE CASCADE is present for all FKs in Rite.sq to guarantee cascade deletion

### Low
- [ ] Add unit tests for CompleteHabit, UndoHabit, UndoLastIncrement, CreateHabit, GenerateDailyHabits, ProcessEndOfDay, GetWeeklyInstances, SkipHabit
- [ ] Locale-aware time formatting in TodayHabitUiModel (currently hardcoded 12h AM/PM)
- [ ] Extract magic numbers to named constants (undo timeout, heatmap window, weekly lookback, snooze range, ring dimensions)
- [ ] Add @Preview functions for Compose screens alongside Roborazzi screenshot tests
- [ ] Add deep linking support to Routes for notification tap-to-screen
- [ ] Use date-range query in HabitDetailViewModel instead of fetching all instances then filtering

## Code Quality — from reactive Today refactor (2026-04-26)

### Important
- [x] Wrap `tickAtMidnight` and `observeForegroundChanges` in `DefaultCurrentDateProvider` with try/catch + log + retry-on-delay-floor. Currently a single throw kills the loop for the rest of the process lifetime.
- [ ] Wire `Intent.ACTION_TIMEZONE_CHANGED` (Android) / `NSSystemTimeZoneDidChangeNotification` (iOS) into `CurrentDateProvider` so a mid-flow TZ change recomputes `today` immediately. Right now the user has to background+foreground for the new TZ to take effect.

### Medium
- [ ] Cache `(habit, schedule)` lookups in `TodayViewModel.buildState` keyed on instance.id between emissions of the instances flow — avoids ~2N DB hits per state tick. Becomes visible at ~100 habits.
- [ ] Move `refreshTrackingNotification` out of `buildState` — currently re-fires on every UI-only state change (`_pendingDelete`, `_quantitativeInputFor`, `_timezoneWarningDismissed` toggles). Should be in its own collector watching the instances flow.
- [ ] Drop `generateDailyHabits.execute()` from `OnboardingViewModel.completeOnboarding` — `TodayViewModel.init` now covers it on the first navigation to Today.

### Low
- [ ] Add "user update re-emits" test to `UserRepositoryObserveTest` (timezone change, onboarding flag flip).
- [ ] Add "instance update/delete re-emits" test to `HabitInstanceRepositoryObserveRangeTest` (currently only insert).
- [ ] Add minimal `println` (or future logger) in `DefaultCurrentDateProvider` on midnight-tick startup + delay duration — needed for "Today still shows yesterday" debugging.

## Future Features

- [ ] Weekly Reflection / insights card (exploring on-device Gemma 4 E2B for natural language summaries)
- [x] Periodic reminder scheduling (interval-based within a time window)
- [ ] Create/edit habit UI for custom increment values
- [ ] Active vs silent tracking notification toggle in Settings
- [ ] Settings option to define start of week (Sunday vs Monday)
- [ ] iOS activation
- [ ] Comprehensive unit test coverage
- [ ] Ascension mode — visual treatment for quantitative habits that go beyond 100% (ring colour shift, celebratory UI)
- [ ] Block distracting apps when there are pending habits (needs brainstorming)
- [ ] Bag-of-words matching for habit creation — auto-suggest icon based on habit name
- [ ] Update app icon
- [ ] Fix flaky TodayViewModelSwipeTest — inject test dispatchers into repositories instead of using real Dispatchers.IO
- [ ] Inject Clock into ProcessEndOfDay so tests can control "today" — needed for proper assertions on WEEKLY/FLEXIBLE_WEEKLY evaluation timing
