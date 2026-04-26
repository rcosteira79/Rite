# Reactive Today Screen State Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate the "Today screen shows yesterday's habits after day rollover" bug by making the screen state derive from observable inputs (current date + database) instead of one-shot snapshots.

**Architecture:**
- New `AppForegroundObserver` (commonMain interface, platform impls) emits when the app comes to the foreground.
- New `CurrentDateProvider` (commonMain) exposes a `StateFlow<LocalDate>` driven by the foreground observer + a midnight tick + the user's timezone.
- `TodayViewModel.state` becomes a derived `StateFlow` composed from `currentDateProvider.today.flatMapLatest { observeTodayState(it) }`, where `observeTodayState` `combine`s a Flow-backed `observeInstancesInDateRange` query with the user observable.
- Side effects (`processEndOfDay.execute()`, `generateDailyHabits.execute()`) are launched in a separate `viewModelScope` collector tied to the date provider — they fire on each date change, never on subscription churn.
- The 4 redundant `loadTodayHabits()` calls in `RiteNavigation` are removed; the DB Flow now picks up form-saved habits automatically.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, kotlin-inject DI, SQLDelight + coroutines-extensions, kotlinx-coroutines Flow, kotlinx-datetime, AndroidX `lifecycle-process` (Android), `UIApplicationDidBecomeActiveNotification` (iOS), Turbine (Flow tests).

---

### Task 1: Add `lifecycle-process` Android dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `composeApp/build.gradle.kts`

- [ ] **Step 1: Add the library entry**

In `gradle/libs.versions.toml`, under `[libraries]`, add (alphabetical with other `androidx-lifecycle-*` entries near line 38):

```toml
androidx-lifecycle-process = { module = "androidx.lifecycle:lifecycle-process", version.ref = "androidx-lifecycle" }
```

- [ ] **Step 2: Use it in Android source set**

In `composeApp/build.gradle.kts`, inside `androidMain.dependencies { ... }` block (around line 39-44), add:

```kotlin
implementation(libs.androidx.lifecycle.process)
```

- [ ] **Step 3: Verify Gradle sync**

Run: `./gradlew :composeApp:dependencies --configuration androidDebugRuntimeClasspath | grep lifecycle-process`
Expected: line containing `androidx.lifecycle:lifecycle-process:2.9.6` (the resolved version).

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml composeApp/build.gradle.kts
git commit -m "build: add androidx.lifecycle:lifecycle-process for ProcessLifecycleOwner"
```

---

### Task 2: Introduce `AppCoroutineScope` typealias and DI provider

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/AppCoroutineScope.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/RiteAppComponent.kt`

- [ ] **Step 1: Create the typealias**

Create `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/AppCoroutineScope.kt`:

```kotlin
package com.ricardocosteira.rite.di

import kotlinx.coroutines.CoroutineScope

/**
 * A process-lifetime [CoroutineScope] for fire-and-forget work whose lifetime should
 * exceed any single ViewModel or screen — e.g., the [com.ricardocosteira.rite.domain.time.CurrentDateProvider]
 * background tickers.
 */
typealias AppCoroutineScope = CoroutineScope
```

- [ ] **Step 2: Provide it in `RiteAppComponent`**

In `RiteAppComponent.kt`, add the import block:

```kotlin
import kotlinx.coroutines.SupervisorJob
```

Add the provider after the existing `provideDefaultDispatcher` (around line 62):

```kotlin
// Application-lifetime scope (singleton)
@AppScope
@Provides
fun provideAppCoroutineScope(defaultDispatcher: DefaultDispatcher): AppCoroutineScope =
    CoroutineScope(SupervisorJob() + defaultDispatcher)
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/AppCoroutineScope.kt composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/RiteAppComponent.kt
git commit -m "di: introduce AppCoroutineScope typealias and provider"
```

---

### Task 3: Add `Clock` injection point

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/RiteAppComponent.kt`

- [ ] **Step 1: Add the provider**

In `RiteAppComponent.kt`, add the import:

```kotlin
import kotlin.time.Clock
```

Add the provider after `provideAppCoroutineScope`:

```kotlin
// System clock — injected so tests can substitute a virtual clock
@AppScope
@Provides
fun provideClock(): Clock = Clock.System
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/RiteAppComponent.kt
git commit -m "di: provide Clock for testability"
```

---

### Task 4 (revised): Add regression test for existing `UserRepository.observeUser()`

> **Note:** The original Task 4 added a duplicate `observeUser:` SQLDelight query that was reverted (commit `337a0a3` reverts `db17760`). Code review found that `observeUser()` already exists on the interface (`UserRepository.kt:15`) and impl (`UserRepositoryImpl.kt:28-33`), wired through the existing `getUser` query via `.asFlow().mapToOneOrNull(...)` — the project's idiomatic SQLDelight pattern (one query, two consumption strategies — same precedent as `observeInstancesForDate` in `HabitInstanceRepositoryImpl`). This revised task only adds missing test coverage for the existing implementation.

**Files:**
- Test: `composeApp/src/jvmTest/kotlin/com/ricardocosteira/rite/data/repositories/UserRepositoryObserveTest.kt`

- [ ] **Step 1: Add the integration test**

Create `composeApp/src/jvmTest/kotlin/com/ricardocosteira/rite/data/repositories/UserRepositoryObserveTest.kt`:

```kotlin
package com.ricardocosteira.rite.data.repositories

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.ricardocosteira.rite.data.database.RiteDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryObserveTest {

    @Test
    fun `observeUser emits null when no user exists, then emits user after insert`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
            RiteDatabase.Schema.create(it)
        }
        val database = RiteDatabase(driver)
        val repository = UserRepositoryImpl(database = database, ioDispatcher = testDispatcher)

        repository.observeUser().test {
            assertNull(awaitItem(), "Expected null before any user is created")
            repository.createDefaultUser(timezone = TimeZone.UTC)
            val emitted = awaitItem()
            assertEquals(TimeZone.UTC, emitted?.timezone, "Expected emission with new user's timezone")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Run test, confirm it passes (no impl change needed)**

Run: `./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.data.repositories.UserRepositoryObserveTest"`
Expected: PASS — `observeUser()` is already implemented; this test just locks in the contract.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/jvmTest/kotlin/com/ricardocosteira/rite/data/repositories/UserRepositoryObserveTest.kt
git commit -m "test(user): add regression test for observeUser() emission contract"
```

---

### Task 5 (revised): Add `observeInstancesInDateRange()` to `HabitInstanceRepository`

> **Note:** The original Tasks 6 and 7 split this work across "add a duplicate `observeInstancesInDateRange:` SQLDelight query" (Task 6) and "implement against the new query" (Task 7). Same anti-pattern as the reverted Task 4: project convention (see `observeInstancesForDate` in `HabitInstanceRepositoryImpl`) is to call `.asFlow()` directly on the existing one-shot query. This revised task is the consolidation: one task, no duplicate SQL.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/domain/repositories/HabitInstanceRepository.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/data/repositories/HabitInstanceRepositoryImpl.kt`
- Test: `composeApp/src/jvmTest/kotlin/com/ricardocosteira/rite/data/repositories/HabitInstanceRepositoryObserveRangeTest.kt`

- [ ] **Step 1: Write the failing test**

Create `composeApp/src/jvmTest/kotlin/com/ricardocosteira/rite/data/repositories/HabitInstanceRepositoryObserveRangeTest.kt`:

```kotlin
package com.ricardocosteira.rite.data.repositories

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitSchedule
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitInstanceRepositoryObserveRangeTest {

    @Test
    fun `observeInstancesInDateRange re-emits when an instance is inserted in range`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
            RiteDatabase.Schema.create(it)
        }
        val database = RiteDatabase(driver)
        val habitRepository = HabitRepositoryImpl(database = database, ioDispatcher = testDispatcher)
        val instanceRepository = HabitInstanceRepositoryImpl(database = database, ioDispatcher = testDispatcher)

        val day = LocalDate(2026, 4, 26)
        val habit = Habit(
            id = "habit-1",
            name = "Read",
            description = null,
            type = HabitType.BINARY,
            targetValue = null,
            unit = null,
            defaultIncrement = 1,
            isActive = true,
            isArchived = false,
            currentStreak = 0,
            longestStreak = 0,
            totalCompletions = 0,
            expectedCompletions = 0,
            createdAt = Clock.System.now(),
            archivedAt = null
        )
        val schedule = HabitSchedule(
            id = "sched-1",
            habitId = "habit-1",
            scheduleType = ScheduleType.DAILY,
            startDate = day,
            endDate = null,
            quota = 1
        )
        habitRepository.createHabit(habit = habit, schedule = schedule, reminder = null)

        instanceRepository.observeInstancesInDateRange(day, day).test {
            assertTrue(awaitItem().isEmpty(), "Expected empty on initial subscribe")

            val instance = HabitInstance(
                id = "inst-1",
                habitId = "habit-1",
                date = day,
                status = HabitStatus.PENDING,
                completedValue = null,
                targetValue = null,
                consecutiveSkipsAtCreation = 0,
                createdAt = Clock.System.now()
            )
            instanceRepository.createInstance(instance)

            val next = awaitItem()
            assertEquals(1, next.size, "Expected one instance after insert")
            assertEquals("inst-1", next.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.data.repositories.HabitInstanceRepositoryObserveRangeTest"`
Expected: FAIL — `observeInstancesInDateRange` does not exist.

- [ ] **Step 3: Add to interface**

In `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/domain/repositories/HabitInstanceRepository.kt`, add (near the existing `getInstancesInDateRange` declaration):

```kotlin
/**
 * Observe instances in a date range. Re-emits whenever any instance in the
 * range is inserted, updated, or deleted.
 */
fun observeInstancesInDateRange(
    startDate: LocalDate,
    endDate: LocalDate
): Flow<List<HabitInstance>>
```

- [ ] **Step 4: Implement in `HabitInstanceRepositoryImpl`**

Important: use the existing `getInstancesInDateRange` SQLDelight query directly. NO new SQL query is added. This mirrors the existing `observeInstancesForDate` precedent at lines 25-29 of the same file.

In `HabitInstanceRepositoryImpl.kt`, add the new method near the existing `observeInstancesForDate` method (around line 25):

```kotlin
override fun observeInstancesInDateRange(
    startDate: LocalDate,
    endDate: LocalDate
): Flow<List<HabitInstance>> = queries
    .getInstancesInDateRange(startDate.toString(), endDate.toString())
    .asFlow()
    .mapToList(ioDispatcher)
    .map { list -> list.map { it.toDomain() } }
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.data.repositories.HabitInstanceRepositoryObserveRangeTest"`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/domain/repositories/HabitInstanceRepository.kt composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/data/repositories/HabitInstanceRepositoryImpl.kt composeApp/src/jvmTest/kotlin/com/ricardocosteira/rite/data/repositories/HabitInstanceRepositoryObserveRangeTest.kt
git commit -m "data(instances): add observeInstancesInDateRange() Flow"
```

---

### Tasks 6 and 7: ~~Add `observeInstancesInDateRange` SQLDelight query~~ / ~~Expose Flow on repository~~

**Status: DELETED (consolidated into Task 5 above).** Project convention is to call `.asFlow()` on the existing one-shot query rather than duplicate the SQL. See the note on Task 5.

---

### Task 8: Define `AppForegroundObserver` interface

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/domain/time/AppForegroundObserver.kt`

- [ ] **Step 1: Create the interface**

```kotlin
package com.ricardocosteira.rite.domain.time

import kotlinx.coroutines.flow.SharedFlow

/**
 * Platform-agnostic signal that the app has come to the foreground.
 *
 * - Android: backed by [androidx.lifecycle.ProcessLifecycleOwner] ON_RESUME.
 * - iOS: backed by `UIApplicationDidBecomeActiveNotification`.
 * - JVM (desktop): emits once at construction; desktop has no equivalent app-level
 *   foreground signal worth wiring for this app's needs.
 *
 * Consumers should treat each emission as a hint to re-evaluate time-sensitive state
 * (e.g. "is today still today?"). Idempotent re-evaluation is the implementor's
 * responsibility, not this interface's.
 */
interface AppForegroundObserver {
    val onForeground: SharedFlow<Unit>
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/domain/time/AppForegroundObserver.kt
git commit -m "domain(time): introduce AppForegroundObserver interface"
```

---

### Task 9: Implement `AndroidAppForegroundObserver`

**Files:**
- Create: `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/time/AndroidAppForegroundObserver.kt`

- [ ] **Step 1: Create the Android implementation**

```kotlin
package com.ricardocosteira.rite.time

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Emits when the application process returns to the foreground.
 *
 * Must be constructed on the main thread (Application.onCreate is on main, so
 * constructing this observer there is safe). The first ON_RESUME the
 * [ProcessLifecycleOwner] sees after construction also produces an emission.
 */
class AndroidAppForegroundObserver : AppForegroundObserver, DefaultLifecycleObserver {
    private val _onForeground = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val onForeground: SharedFlow<Unit> = _onForeground.asSharedFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        _onForeground.tryEmit(Unit)
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/time/AndroidAppForegroundObserver.kt
git commit -m "android(time): implement AndroidAppForegroundObserver via ProcessLifecycleOwner"
```

---

### Task 10: Implement `IosAppForegroundObserver`

**Files:**
- Create: `composeApp/src/iosMain/kotlin/com/ricardocosteira/rite/time/IosAppForegroundObserver.kt`

- [ ] **Step 1: Create the iOS implementation**

```kotlin
package com.ricardocosteira.rite.time

import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification

/**
 * Emits when the app receives `UIApplicationDidBecomeActiveNotification`.
 *
 * The observer holds an [NSObjectProtocol] token from `addObserverForName`.
 * No `removeObserver` call is wired because instances live for the entire
 * process lifetime (created by AppComponentFactory on app launch).
 */
class IosAppForegroundObserver : AppForegroundObserver {
    private val _onForeground = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val onForeground: SharedFlow<Unit> = _onForeground.asSharedFlow()

    init {
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            _onForeground.tryEmit(Unit)
        }
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/iosMain/kotlin/com/ricardocosteira/rite/time/IosAppForegroundObserver.kt
git commit -m "ios(time): implement IosAppForegroundObserver via NSNotificationCenter"
```

---

### Task 11: Implement `JvmAppForegroundObserver` (no-op for desktop)

**Files:**
- Create: `composeApp/src/jvmMain/kotlin/com/ricardocosteira/rite/time/JvmAppForegroundObserver.kt`

- [ ] **Step 1: Create the JVM implementation**

```kotlin
package com.ricardocosteira.rite.time

import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Desktop has no app-level foreground signal worth wiring up — the JVM build is
 * primarily for hot-reload during development, not production. Emits once at
 * construction so the [com.ricardocosteira.rite.domain.time.CurrentDateProvider]
 * still gets a kick to recompute on app start.
 */
class JvmAppForegroundObserver : AppForegroundObserver {
    private val _onForeground = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
    override val onForeground: SharedFlow<Unit> = _onForeground.asSharedFlow()

    init {
        _onForeground.tryEmit(Unit)
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/jvmMain/kotlin/com/ricardocosteira/rite/time/JvmAppForegroundObserver.kt
git commit -m "jvm(time): implement no-op JvmAppForegroundObserver for desktop"
```

---

### Task 12: Add `CurrentDateProvider` interface and default implementation

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/domain/time/CurrentDateProvider.kt`

- [ ] **Step 1: Create the interface and impl**

```kotlin
package com.ricardocosteira.rite.domain.time

import com.ricardocosteira.rite.di.AppCoroutineScope
import com.ricardocosteira.rite.di.AppScope
import com.ricardocosteira.rite.domain.repositories.UserRepository
import kotlin.coroutines.coroutineContext
import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import me.tatarka.inject.annotations.Inject

/**
 * Source of truth for "what is today" in the user's local timezone. Re-emits when:
 * - The app comes to the foreground (via [AppForegroundObserver]).
 * - The wall clock crosses midnight (self-scheduled `delay` loop).
 *
 * Consumers (e.g. `TodayViewModel`) collect this and re-derive their state on each
 * change. The provider de-duplicates emissions via [StateFlow] semantics — only
 * actual date changes propagate.
 */
interface CurrentDateProvider {
    val today: StateFlow<LocalDate>
}

@AppScope
@Inject
class DefaultCurrentDateProvider(
    private val userRepository: UserRepository,
    private val foregroundObserver: AppForegroundObserver,
    private val applicationScope: AppCoroutineScope,
    private val clock: Clock
) : CurrentDateProvider {

    private val _today = MutableStateFlow(initialDate())
    override val today: StateFlow<LocalDate> = _today.asStateFlow()

    init {
        applicationScope.launch { refineInitialDate() }
        applicationScope.launch { observeForegroundChanges() }
        applicationScope.launch { tickAtMidnight() }
    }

    private fun initialDate(): LocalDate =
        clock.todayIn(TimeZone.currentSystemDefault())

    private suspend fun refineInitialDate() {
        _today.value = computeToday()
    }

    private suspend fun observeForegroundChanges() {
        foregroundObserver.onForeground.collect {
            _today.value = computeToday()
        }
    }

    private suspend fun tickAtMidnight() {
        while (coroutineContext.isActive) {
            val tz = currentTimezone()
            val current = clock.todayIn(tz)
            val nextMidnight = current.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)
            val nowMs = clock.now().toEpochMilliseconds()
            val delayMs = (nextMidnight.toEpochMilliseconds() - nowMs).coerceAtLeast(MIN_TICK_DELAY_MS)
            delay(delayMs)
            _today.value = computeToday()
        }
    }

    private suspend fun computeToday(): LocalDate = clock.todayIn(currentTimezone())

    private suspend fun currentTimezone(): TimeZone =
        userRepository.getUser()?.timezone ?: TimeZone.currentSystemDefault()

    private companion object {
        // Floor for the midnight delay so a clock-skew bug can't busy-loop.
        const val MIN_TICK_DELAY_MS: Long = 60_000L
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/domain/time/CurrentDateProvider.kt
git commit -m "domain(time): add CurrentDateProvider with foreground + midnight triggers"
```

---

### Task 13: Wire `AppForegroundObserver` and `CurrentDateProvider` into DI

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/AppComponentFactory.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/di/AppComponentFactory.android.kt`
- Modify: `composeApp/src/iosMain/kotlin/com/ricardocosteira/rite/di/AppComponentFactory.ios.kt`
- Modify: `composeApp/src/jvmMain/kotlin/com/ricardocosteira/rite/di/AppComponentFactory.jvm.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/RiteAppComponent.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/RiteApplication.kt`

- [ ] **Step 1: Update the `expect` factory signature**

In `AppComponentFactory.kt`, replace the file contents with:

```kotlin
package com.ricardocosteira.rite.di

import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import com.ricardocosteira.rite.notifications.HabitNotification

/**
 * Platform-specific factory for [RiteAppComponent].
 * Each platform instantiates the kotlin-inject generated [InjectRiteAppComponent].
 */
expect fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification,
    appForegroundObserver: AppForegroundObserver
): RiteAppComponent
```

- [ ] **Step 2: Update `RiteAppComponent` constructor and bindings**

In `RiteAppComponent.kt`:

Add imports:
```kotlin
import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import com.ricardocosteira.rite.domain.time.CurrentDateProvider
import com.ricardocosteira.rite.domain.time.DefaultCurrentDateProvider
```

Replace the constructor (around line 50) with:
```kotlin
abstract class RiteAppComponent(
    @get:Provides val databaseDriverFactory: DatabaseDriverFactory,
    @get:Provides val habitNotification: HabitNotification,
    @get:Provides val appForegroundObserver: AppForegroundObserver
) {
```

Add the binding for the date provider (after the existing repository bindings, around line 103):
```kotlin
@AppScope
@Provides
fun provideCurrentDateProvider(impl: DefaultCurrentDateProvider): CurrentDateProvider = impl
```

Add the public accessor (alongside the other `abstract val` declarations):
```kotlin
abstract val currentDateProvider: CurrentDateProvider
```

- [ ] **Step 3: Update each platform's `actual fun createAppComponent`**

In `AppComponentFactory.android.kt`, replace contents with:

```kotlin
package com.ricardocosteira.rite.di

import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import com.ricardocosteira.rite.notifications.HabitNotification

actual fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification,
    appForegroundObserver: AppForegroundObserver
): RiteAppComponent =
    RiteAppComponent::class.create(driverFactory, habitNotification, appForegroundObserver)
```

In `AppComponentFactory.ios.kt`, replace contents with:

```kotlin
package com.ricardocosteira.rite.di

import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import com.ricardocosteira.rite.notifications.HabitNotification

actual fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification,
    appForegroundObserver: AppForegroundObserver
): RiteAppComponent =
    RiteAppComponent::class.create(driverFactory, habitNotification, appForegroundObserver)
```

In `AppComponentFactory.jvm.kt`, replace contents with:

```kotlin
package com.ricardocosteira.rite.di

import com.ricardocosteira.rite.data.DatabaseDriverFactory
import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import com.ricardocosteira.rite.notifications.HabitNotification

actual fun createAppComponent(
    driverFactory: DatabaseDriverFactory,
    habitNotification: HabitNotification,
    appForegroundObserver: AppForegroundObserver
): RiteAppComponent =
    RiteAppComponent::class.create(driverFactory, habitNotification, appForegroundObserver)
```

- [ ] **Step 4: Wire the Android observer in `RiteApplication`**

In `RiteApplication.kt`, add the import:

```kotlin
import com.ricardocosteira.rite.time.AndroidAppForegroundObserver
```

Replace the `onCreate` body to construct + pass the observer:

```kotlin
override fun onCreate() {
    super.onCreate()
    val driverFactory = DatabaseDriverFactory(this)
    val habitNotification = HabitNotification(this)
    val appForegroundObserver = AndroidAppForegroundObserver()
    appComponent = createAppComponent(driverFactory, habitNotification, appForegroundObserver)
    NotificationChannels.createChannels(this)
    WorkManagerInitializer.initialize(this)
}
```

- [ ] **Step 5: Update iOS entry point**

Find the iOS entry point that constructs the app component (search the project for `createAppComponent` calls). Update the call site to pass `IosAppForegroundObserver()`. Common location:

```bash
grep -rn "createAppComponent" composeApp/src/iosMain/ iosApp/
```

Update the discovered call site, e.g.:

```kotlin
val appForegroundObserver = IosAppForegroundObserver()
val appComponent = createAppComponent(driverFactory, habitNotification, appForegroundObserver)
```

- [ ] **Step 6: Update JVM entry point**

Same search for desktop:

```bash
grep -rn "createAppComponent" composeApp/src/jvmMain/
```

Update to pass `JvmAppForegroundObserver()`.

- [ ] **Step 7: Build all targets**

Run: `./gradlew :composeApp:compileKotlinJvm :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/di/ composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/di/AppComponentFactory.android.kt composeApp/src/androidMain/kotlin/com/ricardocosteira/rite/RiteApplication.kt composeApp/src/iosMain/kotlin/com/ricardocosteira/rite/di/AppComponentFactory.ios.kt composeApp/src/jvmMain/kotlin/com/ricardocosteira/rite/di/AppComponentFactory.jvm.kt
# plus any iOS / JVM entry points discovered in steps 5-6
git commit -m "di: wire AppForegroundObserver and CurrentDateProvider into DI graph"
```

---

### Task 14: Add `FakeCurrentDateProvider` test helper

**Files:**
- Create: `composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/domain/time/FakeCurrentDateProvider.kt`

- [ ] **Step 1: Create the fake**

```kotlin
package com.ricardocosteira.rite.domain.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

/**
 * Test double for [CurrentDateProvider] that lets tests imperatively drive the
 * "current date" to simulate day rollovers without touching the wall clock.
 */
class FakeCurrentDateProvider(initial: LocalDate) : CurrentDateProvider {
    private val _today = MutableStateFlow(initial)
    override val today: StateFlow<LocalDate> = _today.asStateFlow()

    fun setToday(date: LocalDate) {
        _today.value = date
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileTestKotlinJvm`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonTest/kotlin/com/ricardocosteira/rite/domain/time/FakeCurrentDateProvider.kt
git commit -m "test: add FakeCurrentDateProvider helper for VM tests"
```

---

### Task 15: Write the failing date-rollover integration test

**Files:**
- Create: `composeApp/src/jvmTest/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayViewModelDateRolloverTest.kt`

- [ ] **Step 1: Write the failing test**

This test captures the original bug: the screen state must update when `CurrentDateProvider` emits a new date.

```kotlin
package com.ricardocosteira.rite.presentation.ui.today

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.data.repositories.HabitCompletionEventRepositoryImpl
import com.ricardocosteira.rite.data.repositories.HabitInstanceRepositoryImpl
import com.ricardocosteira.rite.data.repositories.HabitRepositoryImpl
import com.ricardocosteira.rite.data.repositories.LeavePeriodRepositoryImpl
import com.ricardocosteira.rite.data.repositories.UserRepositoryImpl
import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitSchedule
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.domain.time.FakeCurrentDateProvider
import com.ricardocosteira.rite.domain.usecases.CompleteHabit
import com.ricardocosteira.rite.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.rite.domain.usecases.ProcessEndOfDay
import com.ricardocosteira.rite.domain.usecases.SkipHabit
import com.ricardocosteira.rite.domain.usecases.UndoHabit
import com.ricardocosteira.rite.domain.usecases.UndoLastIncrement
import com.ricardocosteira.rite.domain.usecases.UuidProvider
import com.ricardocosteira.rite.notifications.HabitNotification
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelDateRolloverTest {

    @Test
    fun `state recomposes for the new date when CurrentDateProvider emits day rollover`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            // Given: yesterday has 1 daily habit (PENDING), today has none yet
            val yesterday = LocalDate(2026, 4, 25)
            val today = LocalDate(2026, 4, 26)

            val deps = TestDependencies(testDispatcher)
            deps.seedUser()
            deps.seedDailyHabit(habitId = "h1", date = yesterday, instanceId = "i-yesterday")

            val dateProvider = FakeCurrentDateProvider(initial = yesterday)
            val viewModel = deps.buildViewModel(dateProvider, testDispatcher)
            advanceUntilIdle()

            // Sanity: yesterday's pending instance is shown.
            val yesterdayState = viewModel.state.value
            assertTrue(
                yesterdayState.pendingDaily.any { it.instanceId == "i-yesterday" },
                "Expected yesterday's PENDING instance in pendingDaily before rollover"
            )

            // When: CurrentDateProvider emits a new date (simulates midnight or app foreground after rollover)
            // and a fresh today instance has been created in DB by GenerateDailyHabits.
            deps.seedTodayInstance(habitId = "h1", date = today, instanceId = "i-today")
            dateProvider.setToday(today)
            advanceUntilIdle()

            // Then: state filters yesterday's daily instance out, shows today's instance instead.
            val newState = viewModel.state.value
            assertTrue(
                newState.pendingDaily.none { it.instanceId == "i-yesterday" },
                "Expected yesterday's daily instance to disappear after rollover"
            )
            assertEquals(
                "i-today",
                newState.pendingDaily.firstOrNull()?.instanceId,
                "Expected today's instance in pendingDaily after rollover"
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class TestDependencies(testDispatcher: kotlinx.coroutines.CoroutineDispatcher) {
        private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
            RiteDatabase.Schema.create(it)
        }
        private val database = RiteDatabase(driver)

        val userRepository = UserRepositoryImpl(database, testDispatcher)
        val habitRepository = HabitRepositoryImpl(database, testDispatcher)
        val habitInstanceRepository = HabitInstanceRepositoryImpl(database, testDispatcher)
        private val completionEventRepository = HabitCompletionEventRepositoryImpl(database, testDispatcher)
        private val leavePeriodRepository = LeavePeriodRepositoryImpl(database, testDispatcher)

        private val uuidProvider = object : UuidProvider {
            private var counter = 0
            override fun generate(): String = "uuid-${++counter}"
        }

        suspend fun seedUser() {
            if (userRepository.getUser() == null) {
                userRepository.createDefaultUser(timezone = TimeZone.UTC)
            }
        }

        suspend fun seedDailyHabit(habitId: String, date: LocalDate, instanceId: String) {
            habitRepository.createHabit(
                habit = Habit(
                    id = habitId,
                    name = "Habit $habitId",
                    description = null,
                    type = HabitType.BINARY,
                    targetValue = null,
                    unit = null,
                    defaultIncrement = 1,
                    isActive = true,
                    isArchived = false,
                    currentStreak = 0,
                    longestStreak = 0,
                    totalCompletions = 0,
                    expectedCompletions = 0,
                    createdAt = Clock.System.now(),
                    archivedAt = null
                ),
                schedule = HabitSchedule(
                    id = "sched-$habitId",
                    habitId = habitId,
                    scheduleType = ScheduleType.DAILY,
                    startDate = date,
                    endDate = null,
                    quota = 1
                ),
                reminder = null
            )
            habitInstanceRepository.createInstance(
                HabitInstance(
                    id = instanceId,
                    habitId = habitId,
                    date = date,
                    status = HabitStatus.PENDING,
                    completedValue = null,
                    targetValue = null,
                    consecutiveSkipsAtCreation = 0,
                    createdAt = Clock.System.now()
                )
            )
        }

        suspend fun seedTodayInstance(habitId: String, date: LocalDate, instanceId: String) {
            habitInstanceRepository.createInstance(
                HabitInstance(
                    id = instanceId,
                    habitId = habitId,
                    date = date,
                    status = HabitStatus.PENDING,
                    completedValue = null,
                    targetValue = null,
                    consecutiveSkipsAtCreation = 0,
                    createdAt = Clock.System.now()
                )
            )
        }

        fun buildViewModel(
            dateProvider: FakeCurrentDateProvider,
            testDispatcher: kotlinx.coroutines.CoroutineDispatcher
        ): TodayViewModel = TodayViewModel(
            userRepository = userRepository,
            habitRepository = habitRepository,
            habitInstanceRepository = habitInstanceRepository,
            generateDailyHabits = GenerateDailyHabits(
                userRepository = userRepository,
                habitRepository = habitRepository,
                habitInstanceRepository = habitInstanceRepository,
                leavePeriodRepository = leavePeriodRepository,
                uuidProvider = uuidProvider
            ),
            processEndOfDay = ProcessEndOfDay(
                userRepository = userRepository,
                habitInstanceRepository = habitInstanceRepository,
                habitRepository = habitRepository
            ),
            completeHabit = CompleteHabit(
                habitInstanceRepository = habitInstanceRepository,
                habitRepository = habitRepository,
                habitCompletionEventRepository = completionEventRepository
            ),
            skipHabit = SkipHabit(
                habitInstanceRepository = habitInstanceRepository,
                userRepository = userRepository
            ),
            undoHabit = UndoHabit(
                habitInstanceRepository = habitInstanceRepository,
                habitCompletionEventRepository = completionEventRepository,
                habitRepository = habitRepository,
                userRepository = userRepository
            ),
            undoLastIncrement = UndoLastIncrement(
                habitInstanceRepository = habitInstanceRepository,
                habitCompletionEventRepository = completionEventRepository,
                habitRepository = habitRepository
            ),
            habitNotification = HabitNotification(),
            currentDateProvider = dateProvider,
            defaultDispatcher = testDispatcher
        )
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.presentation.ui.today.TodayViewModelDateRolloverTest"`
Expected: FAIL — `TodayViewModel` constructor doesn't accept `currentDateProvider`.

- [ ] **Step 3: Commit the failing test**

```bash
git add composeApp/src/jvmTest/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayViewModelDateRolloverTest.kt
git commit -m "test(today): add failing test for date-rollover state refresh"
```

---

### Task 16: Refactor `TodayViewModel` to derive state reactively

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayViewModel.kt`

This is the largest change. The new design:
- Constructor accepts `currentDateProvider: CurrentDateProvider`.
- `state: StateFlow<TodayState>` derives from `currentDateProvider.today.flatMapLatest { observeTodayState(it) }`, combined with a local `MutableStateFlow<PendingDelete?>` for the deferred-delete UX.
- A separate `init { viewModelScope.launch { ... } }` collector observes date changes and runs `processEndOfDay.execute()` then `generateDailyHabits.execute()` as side effects.
- `loadTodayHabits()` is removed (state is reactive). Action handlers no longer call it after success — DB Flow re-emits automatically.

- [ ] **Step 1: Replace the entire `TodayViewModel` body**

Open `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayViewModel.kt` and replace its contents with:

```kotlin
package com.ricardocosteira.rite.presentation.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ricardocosteira.rite.di.AppScope
import com.ricardocosteira.rite.di.DefaultDispatcher
import com.ricardocosteira.rite.domain.models.CompletionSource
import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitReminder
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.User
import com.ricardocosteira.rite.domain.models.UserStrictnessSettings
import com.ricardocosteira.rite.domain.models.motivationalTitleIndexForDate
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import com.ricardocosteira.rite.domain.time.CurrentDateProvider
import com.ricardocosteira.rite.domain.usecases.CompleteHabit
import com.ricardocosteira.rite.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.rite.domain.usecases.ProcessEndOfDay
import com.ricardocosteira.rite.domain.usecases.SkipHabit
import com.ricardocosteira.rite.domain.usecases.SkipLockedException
import com.ricardocosteira.rite.domain.usecases.UndoHabit
import com.ricardocosteira.rite.domain.usecases.UndoLastIncrement
import com.ricardocosteira.rite.notifications.HabitNotification
import com.ricardocosteira.rite.notifications.TrackedHabitInfo
import com.ricardocosteira.rite.presentation.mappers.motivationalTitleResource
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.models.mapToTodayHabitUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import me.tatarka.inject.annotations.Inject

/**
 * Scoped to the application lifetime via [AppScope] rather than a
 * [androidx.lifecycle.ViewModelStoreOwner] because this app uses a single-activity
 * architecture and all ViewModels are obtained directly from the DI component.
 *
 * State derives reactively from [currentDateProvider] (current local date) and the
 * [HabitInstanceRepository] / [UserRepository] flows. The screen automatically
 * re-renders when the date changes (app foreground after midnight, midnight tick) or
 * when the database is updated by any actor (action handlers, workers, the
 * notification action receiver, the create/edit habit screen).
 */
@OptIn(ExperimentalCoroutinesApi::class)
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
    private val habitNotification: HabitNotification,
    private val currentDateProvider: CurrentDateProvider,
    private val defaultDispatcher: DefaultDispatcher
) : ViewModel() {

    private val _events = MutableSharedFlow<TodayEvent>()
    val events: SharedFlow<TodayEvent> = _events.asSharedFlow()

    private val _pendingDelete = MutableStateFlow<PendingDelete?>(null)
    private val _quantitativeInputFor = MutableStateFlow<String?>(null)
    private val _timezoneWarningDismissed = MutableStateFlow(false)

    private var undoJob: Job? = null

    val state: StateFlow<TodayState> = currentDateProvider.today
        .flatMapLatest { today -> observeTodayState(today) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = TodayState(isLoading = true)
        )

    init {
        // Side effects: process previous-day failures and generate today's instances on
        // every date change. Both use cases are idempotent.
        viewModelScope.launch {
            currentDateProvider.today.collect {
                try {
                    processEndOfDay.execute()
                    generateDailyHabits.execute()
                } catch (e: Exception) {
                    _events.emit(TodayEvent.ShowError(e.message ?: "Failed to refresh today"))
                }
            }
        }
    }

    private fun observeTodayState(today: LocalDate) = combine(
        userRepository.observeUser(),
        habitInstanceRepository.observeInstancesInDateRange(
            startDate = today.minus(DAY_RANGE, DateTimeUnit.DAY),
            endDate = today
        ),
        _pendingDelete,
        _quantitativeInputFor,
        _timezoneWarningDismissed
    ) { user, instances, pendingDelete, quantitativeInputFor, timezoneWarningDismissed ->
        buildState(
            user = user,
            instances = instances,
            today = today,
            pendingDelete = pendingDelete,
            quantitativeInputFor = quantitativeInputFor,
            timezoneWarningDismissed = timezoneWarningDismissed
        )
    }

    private suspend fun buildState(
        user: User?,
        instances: List<HabitInstance>,
        today: LocalDate,
        pendingDelete: PendingDelete?,
        quantitativeInputFor: String?,
        timezoneWarningDismissed: Boolean
    ): TodayState {
        val userTimezone: TimeZone = user?.timezone ?: TimeZone.currentSystemDefault()
        val strictnessPreset: StrictnessPreset? = user?.let {
            StrictnessPreset.fromSettings(
                UserStrictnessSettings(
                    undoPolicy = it.undoPolicy,
                    maxSnoozesPerHabitPerDay = it.maxSnoozesPerHabitPerDay,
                    maxConsecutiveSkips = it.maxConsecutiveSkips,
                    maxSnoozeDurationMinutes = it.maxSnoozeDurationMinutes
                )
            )
        }

        val motivationalTitleRes = motivationalTitleResource(
            motivationalTitleIndexForDate(today)
        )

        val habits: ImmutableList<TodayHabitUiModel> = withContext(defaultDispatcher) {
            coroutineScope {
                instances.mapNotNull { instance ->
                    val habitDeferred = async { habitRepository.getHabitById(instance.habitId) }
                    val scheduleDeferred = async { habitRepository.getScheduleForHabit(instance.habitId) }
                    val habit = habitDeferred.await() ?: return@mapNotNull null
                    val schedule = scheduleDeferred.await() ?: return@mapNotNull null

                    if (schedule.scheduleType == ScheduleType.DAILY && instance.date != today) {
                        return@mapNotNull null
                    }

                    mapToTodayHabitUiModel(
                        instance = instance,
                        habit = habit,
                        schedule = schedule,
                        maxConsecutiveSkips = user?.maxConsecutiveSkips,
                        userTimezone = userTimezone
                    )
                }
                    .filterNot { it.habitId == pendingDelete?.habitId }
                    .toImmutableList()
            }
        }

        val counts: TodayCounts = habits.computeCounts()
        val resolvedStatuses: Set<HabitStatus> = setOf(
            HabitStatus.COMPLETED,
            HabitStatus.SKIPPED,
            HabitStatus.FAILED
        )

        val dailyHabits: List<TodayHabitUiModel> = habits.filter {
            (it.isDaily || it.isFixedWeekly) && !it.isSuspended
        }
        val weeklyHabits: List<TodayHabitUiModel> = habits.filter {
            it.isFlexibleWeekly && !it.isSuspended
        }

        val (pendingDaily, resolvedDaily) =
            dailyHabits.partition { it.status !in resolvedStatuses }
        val (pendingWeekly, resolvedWeekly) =
            weeklyHabits.partition { it.status !in resolvedStatuses }

        refreshTrackingNotification(today)

        return TodayState(
            habits = habits,
            pendingDaily = pendingDaily.toImmutableList(),
            resolvedDaily = resolvedDaily.toImmutableList(),
            pendingWeekly = pendingWeekly.toImmutableList(),
            resolvedWeekly = resolvedWeekly.toImmutableList(),
            isLoading = false,
            pendingCount = counts.pendingCount,
            dailyProgressDisplay = counts.dailyProgressDisplay,
            dailyProgressExact = counts.dailyProgressExact,
            dailyTotal = counts.dailyTotal,
            motivationalTitleRes = motivationalTitleRes,
            strictnessPreset = strictnessPreset,
            showTimezoneWarning = user?.previousTimezone != null && !timezoneWarningDismissed,
            previousTimezone = user?.previousTimezone?.id,
            pendingDelete = pendingDelete,
            showQuantitativeInputFor = quantitativeInputFor
        )
    }

    private suspend fun cancelReminderForHabit(instanceId: String, habitId: String) {
        val reminder: HabitReminder? = habitRepository.getRemindersForHabit(habitId).firstOrNull()
        habitNotification.cancelReminder(instanceId, reminder)
    }

    fun completeHabit(instanceId: String) {
        viewModelScope.launch {
            val habit = state.value.habits.find { it.instanceId == instanceId } ?: return@launch
            if (habit.type == HabitType.QUANTITATIVE) {
                _quantitativeInputFor.value = instanceId
                return@launch
            }
            completeHabit.executeBinary(instanceId, CompletionSource.IN_APP)
                .onSuccess { cancelReminderForHabit(instanceId, habit.habitId) }
                .onFailure { _events.emit(TodayEvent.ShowError(it.message ?: "Something went wrong")) }
        }
    }

    fun completeQuantitativeHabit(instanceId: String, value: Int) {
        viewModelScope.launch {
            val habit = state.value.habits.find { it.instanceId == instanceId }
            _quantitativeInputFor.value = null
            completeHabit.executeQuantitative(
                instanceId = instanceId,
                deltaValue = value,
                source = CompletionSource.IN_APP
            )
                .onSuccess { updatedInstance ->
                    if (updatedInstance.isQuantitativeComplete() && habit != null) {
                        cancelReminderForHabit(instanceId, habit.habitId)
                    }
                }
                .onFailure { _events.emit(TodayEvent.ShowError(it.message ?: "Something went wrong")) }
        }
    }

    fun incrementHabitProgress(instanceId: String) {
        viewModelScope.launch {
            val habit = state.value.habits.find { it.instanceId == instanceId } ?: return@launch
            completeHabit.executeQuantitative(
                instanceId = instanceId,
                deltaValue = habit.defaultIncrement,
                source = CompletionSource.IN_APP
            )
                .onSuccess { updated ->
                    if (updated.isQuantitativeComplete()) cancelReminderForHabit(instanceId, habit.habitId)
                }
                .onFailure { _events.emit(TodayEvent.ShowError(it.message ?: "Something went wrong")) }
        }
    }

    fun showQuantitativeInput(instanceId: String) { _quantitativeInputFor.value = instanceId }
    fun dismissQuantitativeInput() { _quantitativeInputFor.value = null }

    fun skipHabit(instanceId: String) {
        viewModelScope.launch {
            val habit = state.value.habits.find { it.instanceId == instanceId }
            skipHabit.execute(instanceId)
                .onSuccess { if (habit != null) cancelReminderForHabit(instanceId, habit.habitId) }
                .onFailure { error ->
                    if (error is SkipLockedException) {
                        _events.emit(TodayEvent.SkipLimitReached)
                    } else {
                        _events.emit(TodayEvent.ShowError(error.message ?: "Something went wrong"))
                    }
                }
        }
    }

    fun undoHabit(instanceId: String) {
        viewModelScope.launch {
            undoHabit.execute(instanceId)
                .onFailure { _events.emit(TodayEvent.ShowError(it.message ?: "Something went wrong")) }
        }
    }

    fun undoLastIncrement(instanceId: String) {
        viewModelScope.launch {
            undoLastIncrement.execute(instanceId)
                .onFailure { _events.emit(TodayEvent.ShowError(it.message ?: "Something went wrong")) }
        }
    }

    fun deleteHabit(habitId: String) {
        val habit: TodayHabitUiModel = state.value.habits.find { it.habitId == habitId } ?: return

        commitPendingDeleteAndCancelJob()
        _pendingDelete.value = PendingDelete(habitId, habit.name)
        viewModelScope.launch { _events.emit(TodayEvent.HabitDeleted(habit.name)) }

        undoJob = viewModelScope.launch {
            delay(UNDO_TIMEOUT_MS)
            try {
                habitRepository.deleteHabit(habitId)
                _pendingDelete.value = null
            } catch (e: Exception) {
                _events.emit(TodayEvent.ShowError(e.message ?: "Failed to delete habit"))
                _pendingDelete.value = null
            }
        }
    }

    fun undoDelete() {
        undoJob?.cancel()
        undoJob = null
        _pendingDelete.value = null
        viewModelScope.launch { _events.emit(TodayEvent.UndoCompleted) }
    }

    private fun commitPendingDeleteAndCancelJob() {
        val previousDelete: PendingDelete? = _pendingDelete.value
        undoJob?.cancel()
        undoJob = null

        if (previousDelete == null) return

        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(previousDelete.habitId)
            } catch (e: Exception) {
                _events.emit(TodayEvent.ShowError(e.message ?: "Failed to delete habit"))
            }
        }
    }

    fun archiveHabit(habitId: String) {
        viewModelScope.launch {
            try {
                val today: LocalDate = currentDateProvider.today.value
                val instance: HabitInstance? =
                    habitInstanceRepository.getInstanceForHabitAndDate(habitId, today)
                if (instance != null) {
                    habitNotification.cancelAllForHabit(habitId, listOf(instance.id))
                }
                habitRepository.archiveHabit(habitId)
            } catch (e: Exception) {
                _events.emit(TodayEvent.ShowError(e.message ?: "Something went wrong"))
            }
        }
    }

    fun dismissTimezoneWarning() {
        // Local-only dismissal — matches the original behavior where the warning re-appears
        // on the next load if the user.previousTimezone is still set in DB. Resets to false
        // when the user repository emits a new previousTimezone (handled in buildState).
        _timezoneWarningDismissed.value = true
    }

    fun navigateToHabitDetail(instanceId: String) {
        viewModelScope.launch { _events.emit(TodayEvent.NavigateToHabitDetail(instanceId)) }
    }

    fun navigateToCreateHabit() {
        viewModelScope.launch { _events.emit(TodayEvent.NavigateToCreateHabit) }
    }

    private suspend fun refreshTrackingNotification(today: LocalDate) {
        val trackedHabits: List<Habit> = habitRepository.getHabitsWithTrackingEnabled()
        if (trackedHabits.isEmpty()) {
            habitNotification.hideTrackingNotification()
            return
        }

        val trackedInfoList: List<TrackedHabitInfo> = trackedHabits.mapNotNull { habit ->
            val instance: HabitInstance = habitInstanceRepository.getInstanceForHabitAndDate(
                habit.id,
                today
            ) ?: return@mapNotNull null

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

    private companion object {
        const val UNDO_TIMEOUT_MS: Long = 5_000L
        const val DAY_RANGE: Int = 6
        const val STATE_SUBSCRIPTION_TIMEOUT_MS: Long = 5_000L
    }
}
```

(Note: `clearError()` is removed — there is no mutable error state any more, errors propagate via `events`. Confirmed by grep: `clearError` has no external callers in `composeApp/src/`. The `error: String?` field on `TodayState` is unused by the UI and gets dropped in the next task.)

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Run the failing date-rollover test**

Run: `./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.presentation.ui.today.TodayViewModelDateRolloverTest"`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayViewModel.kt
git commit -m "today: derive state reactively from CurrentDateProvider + DB flows"
```

---

### Task 16b: Drop unused `error` field from `TodayState`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayState.kt`

The previous VM set `state.error = e.message` on load failure; the new VM emits errors via the `events` SharedFlow. The `error: String?` field has no UI references (`grep "state.error"` in the today package returns 0 hits) and `clearError()` has no external callers.

- [ ] **Step 1: Remove the `error` field**

In `TodayState.kt`, delete the line:

```kotlin
val error: String? = null,
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayState.kt
git commit -m "today: drop unused TodayState.error field (errors flow via events SharedFlow)"
```

---

### Task 17: Update existing `TodayViewModelSwipeTest` to wire the date provider

**Files:**
- Modify: `composeApp/src/jvmTest/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayViewModelSwipeTest.kt`

The existing swipe-delete tests construct `TodayViewModel` directly and need a `currentDateProvider` parameter now. Use `FakeCurrentDateProvider` seeded with the same date the test seeds instances for.

- [ ] **Step 1: Add the date provider to `buildViewModel`**

In `TodayViewModelSwipeTest.kt`, replace `buildViewModel` with:

```kotlin
private fun buildViewModel(
    deps: TestDependencies,
    testDispatcher: kotlinx.coroutines.CoroutineDispatcher
): TodayViewModel {
    val today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
    val dateProvider = com.ricardocosteira.rite.domain.time.FakeCurrentDateProvider(initial = today)
    return TodayViewModel(
        userRepository = deps.userRepository,
        habitRepository = deps.habitRepository,
        habitInstanceRepository = deps.habitInstanceRepository,
        generateDailyHabits = deps.generateDailyHabits,
        processEndOfDay = deps.processEndOfDay,
        completeHabit = deps.completeHabit,
        skipHabit = deps.skipHabit,
        undoHabit = deps.undoHabit,
        undoLastIncrement = deps.undoLastIncrement,
        habitNotification = HabitNotification(),
        currentDateProvider = dateProvider,
        defaultDispatcher = testDispatcher
    )
}
```

- [ ] **Step 2: Run the swipe tests to verify they pass**

Run: `./gradlew :composeApp:jvmTest --tests "com.ricardocosteira.rite.presentation.ui.today.TodayViewModelSwipeTest"`
Expected: PASS for all 6 tests.

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/jvmTest/kotlin/com/ricardocosteira/rite/presentation/ui/today/TodayViewModelSwipeTest.kt
git commit -m "test(today): inject FakeCurrentDateProvider into swipe tests"
```

---

### Task 18: Remove `loadTodayHabits()` calls from `RiteNavigation`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/RiteNavigation.kt`

The 4 call sites that manually invoke `appComponent.todayViewModel.loadTodayHabits()` are no longer needed — the DB flow re-emits when `HabitFormScreen` saves a habit. Remove them.

- [ ] **Step 1: Remove the call inside the `Onboarding` entry's `onFinished`**

In `RiteNavigation.kt`, find:

```kotlin
onFinished = {
    backStack.clear()
    backStack.add(Today)
    appComponent.todayViewModel.loadTodayHabits()
}
```

Replace with:

```kotlin
onFinished = {
    backStack.clear()
    backStack.add(Today)
}
```

- [ ] **Step 2: Remove the captured `todayViewModel` and call in the `CreateHabit` entry**

Find:

```kotlin
entry<CreateHabit> {
    val todayViewModel = LocalAppComponent.current.todayViewModel
    HabitFormScreen(
        habitIdToEdit = null,
        onNavigateBack = {
            backStack.removeLastOrNull()
            todayViewModel.loadTodayHabits()
        },
        snackbarHostState = snackbarHostState
    )
}
```

Replace with:

```kotlin
entry<CreateHabit> {
    HabitFormScreen(
        habitIdToEdit = null,
        onNavigateBack = { backStack.removeLastOrNull() },
        snackbarHostState = snackbarHostState
    )
}
```

- [ ] **Step 3: Same for `EditHabit`**

```kotlin
entry<EditHabit> { route ->
    HabitFormScreen(
        habitIdToEdit = route.habitId,
        onNavigateBack = { backStack.removeLastOrNull() },
        snackbarHostState = snackbarHostState
    )
}
```

- [ ] **Step 4: Same for `HabitDetail`**

```kotlin
entry<HabitDetail> { route ->
    HabitDetailRoute(
        instanceId = route.instanceId,
        onNavigateBack = { backStack.removeLastOrNull() },
        onEditHabit = { habitId -> backStack.add(EditHabit(habitId)) }
    )
}
```

- [ ] **Step 5: Build to verify**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/com/ricardocosteira/rite/presentation/navigation/RiteNavigation.kt
git commit -m "nav: drop manual loadTodayHabits() calls — DB flow now drives refresh"
```

---

### Task 19: Update `CLAUDE.md` to reflect the resolved coupling

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: Remove the resolved pain point**

In `CLAUDE.md`, delete the entire `### HabitFormScreen → TodayViewModel coupling in nav host` section under "Known Pain Points" (the cross-coupling is gone now).

If the section is the only content under "Known Pain Points", you may also remove the "## Known Pain Points" header itself.

- [ ] **Step 2: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: remove resolved HabitFormScreen → TodayViewModel coupling note"
```

---

### Task 20: Full test + build verification

- [ ] **Step 1: Run full test suite**

Run: `./gradlew :composeApp:jvmTest`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Compile all platform targets**

Run: `./gradlew :composeApp:compileKotlinJvm :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Manual smoke test on Android**

1. Install the app on a device/emulator.
2. Open the app, complete one habit, leave another pending.
3. Background the app.
4. Change the device date to tomorrow (Settings → System → Date & time).
5. Foreground the app.
6. Expected: yesterday's habits are gone; today's habits show up freshly PENDING.
7. Re-open after no date change — confirm no flicker, no spurious processing.

- [ ] **Step 4: Push the branch**

```bash
git push -u origin fix/today-screen-reactive-date
```

---

## Self-Review Checklist (run after writing the plan)

- [x] Spec covered: every layer touched (data Flow methods, foreground observer, date provider, VM refactor, nav cleanup) maps to a numbered task.
- [x] No placeholders: every step contains the code or exact command needed.
- [x] Type consistency: `CurrentDateProvider`, `AppForegroundObserver`, `DefaultCurrentDateProvider`, `AppCoroutineScope` names match across tasks.
- [x] Follows `feedback_no_trailing_commas.md`: no trailing commas in any code blocks.
- [x] Follows `feedback_pr_base_branch.md`: branch is off `develop`, PR will target `develop`.

## Open Risks Worth Watching During Execution

- **kotlin-inject + multiple constructor `@get:Provides`**: adding a third constructor parameter to `RiteAppComponent` should "just work" but rerun KSP after the change (`./gradlew :composeApp:kspCommonMainKotlinMetadata`). If the generated `InjectRiteAppComponent::class.create(...)` signature mismatches, run a clean build.
- **iOS entry point**: I haven't read the iOS app's Swift bootstrap; Task 13 step 5 says to grep for it. The factory call signature change must be propagated there too or iOS builds break.
- **`SharingStarted.WhileSubscribed(5_000)`**: state tears down 5s after the screen leaves composition. The next subscription rebuilds it from scratch (re-runs `processEndOfDay` + `generateDailyHabits` because the `init { }` collector is `viewModelScope`-scoped, which lives forever for an `@AppScope` VM). That's correct, but if the side-effect collector ever moves into the `state` flow itself, this becomes wasteful.
- **`refreshTrackingNotification`** is called from inside `buildState`, which runs on every state recomputation. If that's too frequent (e.g., DB churn during writes), debounce it. Acceptable for v1.
