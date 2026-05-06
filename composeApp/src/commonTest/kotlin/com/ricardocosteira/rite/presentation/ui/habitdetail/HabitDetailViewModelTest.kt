package com.ricardocosteira.rite.presentation.ui.habitdetail

import app.cash.turbine.test
import com.ricardocosteira.rite.domain.models.CompletionSource
import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitCompletionEvent
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitReminder
import com.ricardocosteira.rite.domain.models.HabitSchedule
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.SnoozeState
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.UndoPolicy
import com.ricardocosteira.rite.domain.models.User
import com.ricardocosteira.rite.domain.repositories.HabitCompletionEventRepository
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.SnoozeRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import com.ricardocosteira.rite.domain.usecases.CompleteHabit
import com.ricardocosteira.rite.domain.usecases.SkipHabit
import com.ricardocosteira.rite.domain.usecases.UndoHabit
import com.ricardocosteira.rite.domain.usecases.UndoLastIncrement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

// ─────────────────────────────────────────────────────────────────────────────
// Pinned clock: Wednesday 2026-04-15 12:00 UTC (ISO week 16, Monday=2026-04-13)
// ─────────────────────────────────────────────────────────────────────────────
private val FIXED_NOW: Instant =
    Instant.fromEpochSeconds(1_776_254_400L) // 2026-04-15T12:00:00Z
private val FIXED_CLOCK = object : Clock {
    override fun now(): Instant = FIXED_NOW
}
private val USER_TZ: TimeZone = TimeZone.UTC

// Monday of ISO week 16 when today=2026-04-15 (Wednesday)
private val WEEK_MONDAY: LocalDate = LocalDate(2026, 4, 13)

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
private val BASE_INSTANT: Instant = Instant.fromEpochSeconds(1_700_000_000L)

private fun habitInstance(
    id: String,
    habitId: String = "h1",
    date: LocalDate,
    status: HabitStatus = HabitStatus.PENDING
): HabitInstance = HabitInstance(
    id = id,
    habitId = habitId,
    date = date,
    status = status,
    completedValue = null,
    targetValue = null,
    consecutiveSkipsAtCreation = 0,
    createdAt = BASE_INSTANT
)

private fun binaryHabit(id: String = "h1"): Habit = Habit(
    id = id,
    name = "Test Habit",
    description = null,
    type = HabitType.BINARY,
    targetValue = null,
    unit = null,
    defaultIncrement = 1,
    isTrackingEnabled = false,
    isActive = true,
    isArchived = false,
    currentStreak = 0,
    longestStreak = 0,
    totalCompletions = 0,
    expectedCompletions = 0,
    createdAt = BASE_INSTANT,
    archivedAt = null
)

private fun balancedUser(): User = User(
    id = "u1",
    timezone = USER_TZ,
    previousTimezone = null,
    undoPolicy = UndoPolicy.TODAY_ONLY,
    maxSnoozeDurationMinutes = 30,
    maxSnoozesPerHabitPerDay = 3,
    maxConsecutiveSkips = 2,
    isOnboardingCompleted = true,
    dailySummaryTime = null,
    createdAt = BASE_INSTANT
)

// ─────────────────────────────────────────────────────────────────────────────
// Minimal fakes
// ─────────────────────────────────────────────────────────────────────────────
private class FakeHabitRepository(private val habit: Habit? = binaryHabit()) : HabitRepository {
    override fun observeActiveHabits(): Flow<List<Habit>> = flowOf(listOfNotNull(habit))
    override fun observeArchivedHabits(): Flow<List<Habit>> = flowOf(emptyList())
    override suspend fun getActiveHabits(): List<Habit> = listOfNotNull(habit)
    override suspend fun getHabitsWithTrackingEnabled(): List<Habit> = emptyList()
    override suspend fun getHabitById(habitId: String): Habit? = habit?.takeIf { it.id == habitId }
    override suspend fun createHabit(
        habit: Habit,
        schedule: HabitSchedule,
        reminder: HabitReminder?
    ) = Unit
    override suspend fun updateHabit(habit: Habit) = Unit
    override suspend fun updateHabitStreak(
        habitId: String,
        currentStreak: Int,
        longestStreak: Int
    ) = Unit
    override suspend fun updateHabitScore(
        habitId: String,
        totalCompletions: Int,
        expectedCompletions: Int
    ) = Unit
    override suspend fun incrementHabitTotalCompletions(habitId: String, amount: Int) = Unit
    override suspend fun decrementHabitTotalCompletions(habitId: String, amount: Int) = Unit
    override suspend fun incrementHabitExpectedCompletions(habitId: String, amount: Int) = Unit
    override suspend fun archiveHabit(habitId: String) = Unit
    override suspend fun unarchiveHabit(habitId: String) = Unit
    override suspend fun deleteHabit(habitId: String) = Unit
    override suspend fun getScheduleForHabit(habitId: String): HabitSchedule? = null
    override suspend fun updateSchedule(schedule: HabitSchedule) = Unit
    override suspend fun createScheduleForHabit(schedule: HabitSchedule) = Unit
    override suspend fun getRemindersForHabit(habitId: String): List<HabitReminder> = emptyList()
    override suspend fun updateReminder(reminder: HabitReminder) = Unit
    override suspend fun deleteReminder(reminderId: String) = Unit
    override suspend fun createReminderForHabit(reminder: HabitReminder) = Unit
}

private class FakeHabitInstanceRepository(
    private val instance: HabitInstance,
    private val allInstances: List<HabitInstance> = listOf(instance)
) : HabitInstanceRepository {
    override fun observeInstancesForDate(date: LocalDate): Flow<List<HabitInstance>> =
        flowOf(allInstances.filter { it.date == date })
    override suspend fun getInstancesForDate(date: LocalDate): List<HabitInstance> =
        allInstances.filter { it.date == date }
    override suspend fun getPendingInstancesForDate(date: LocalDate): List<HabitInstance> =
        allInstances.filter { it.date == date && it.status == HabitStatus.PENDING }
    override suspend fun getInstanceById(instanceId: String): HabitInstance? =
        allInstances.firstOrNull { it.id == instanceId }
    override suspend fun getInstanceForHabitAndDate(
        habitId: String,
        date: LocalDate
    ): HabitInstance? = allInstances.firstOrNull { it.habitId == habitId && it.date == date }
    override suspend fun getInstancesForHabit(habitId: String): List<HabitInstance> =
        allInstances.filter { it.habitId == habitId }
    override suspend fun getInstancesInDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitInstance> = allInstances.filter { it.date >= startDate && it.date <= endDate }
    override suspend fun createInstance(instance: HabitInstance) = Unit
    override suspend fun updateInstanceStatus(
        instanceId: String,
        status: HabitStatus,
        completedValue: Int?,
        completedAt: Instant?
    ) = Unit
    override suspend fun updateInstanceCompletedValue(instanceId: String, completedValue: Int) =
        Unit
    override suspend fun updateInstanceTargetValue(instanceId: String, targetValue: Int?) = Unit
}

private class FakeUserRepository(private val user: User? = balancedUser()) : UserRepository {
    override fun observeUser(): Flow<User?> = flowOf(user)
    override suspend fun getUser(): User? = user
    override suspend fun createDefaultUser(timezone: TimeZone): User =
        user ?: error("No user in fake")
    override suspend fun updateUser(user: User) = Unit
    override suspend fun updateTimezone(
        userId: String,
        newTimezone: TimeZone,
        previousTimezone: TimeZone
    ) = Unit
    override suspend fun setOnboardingCompleted(userId: String, isCompleted: Boolean) = Unit
}

private class FakeSnoozeRepository(
    private val stateByInstance: Map<String, SnoozeState> = emptyMap()
) : SnoozeRepository {
    override suspend fun getSnoozeState(instanceId: String): SnoozeState? =
        stateByInstance[instanceId]
    override suspend fun saveSnoozeState(
        instanceId: String,
        scheduledTime: Instant,
        snoozeCount: Int
    ) = Unit
    override suspend fun clearSnoozeState(instanceId: String) = Unit
    override suspend fun getAllSnoozeStates(): List<SnoozeState> = stateByInstance.values.toList()
}

private class FakeCompletionEventRepository : HabitCompletionEventRepository {
    override suspend fun getEventsForInstance(instanceId: String): List<HabitCompletionEvent> =
        emptyList()
    override suspend fun recordEvent(
        instanceId: String,
        deltaValue: Int,
        source: CompletionSource
    ): HabitCompletionEvent = error("Not used in these tests")
    override suspend fun deleteEvent(eventId: String) = Unit
    override suspend fun deleteEventsForInstance(instanceId: String) = Unit
    override suspend fun calculateCompletedValue(instanceId: String): Int = 0
}

// ─────────────────────────────────────────────────────────────────────────────
// Factory helper — avoids repeating all params in each test
// ─────────────────────────────────────────────────────────────────────────────
private fun buildViewModel(
    habitRepository: HabitRepository = FakeHabitRepository(),
    habitInstanceRepository: HabitInstanceRepository,
    userRepository: UserRepository? = null,
    user: User? = balancedUser(),
    snoozeRepository: SnoozeRepository = FakeSnoozeRepository(),
    clock: Clock = FIXED_CLOCK,
    instanceId: String
): HabitDetailViewModel {
    val resolvedUserRepository = userRepository ?: FakeUserRepository(user)
    val completionEventRepo = FakeCompletionEventRepository()
    return HabitDetailViewModel(
        habitRepository = habitRepository,
        habitInstanceRepository = habitInstanceRepository,
        userRepository = resolvedUserRepository,
        snoozeRepository = snoozeRepository,
        completeHabit = CompleteHabit(
            habitInstanceRepository = habitInstanceRepository,
            habitRepository = habitRepository,
            habitCompletionEventRepository = completionEventRepo
        ),
        skipHabit = SkipHabit(
            habitInstanceRepository = habitInstanceRepository,
            userRepository = resolvedUserRepository
        ),
        undoHabit = UndoHabit(
            habitInstanceRepository = habitInstanceRepository,
            habitCompletionEventRepository = completionEventRepo,
            habitRepository = habitRepository,
            userRepository = resolvedUserRepository
        ),
        undoLastIncrement = UndoLastIncrement(
            habitInstanceRepository = habitInstanceRepository,
            habitCompletionEventRepository = completionEventRepo,
            habitRepository = habitRepository
        ),
        clock = clock,
        instanceId = instanceId
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Tests
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailViewModelTest {

    // ── Test A: strictness preset + undoPolicy ────────────────────────────────

    @Test
    fun `given balanced user, state exposes BALANCED preset and TODAY_ONLY undo policy`() =
        runTest(UnconfinedTestDispatcher()) {
            val instance = habitInstance(id = "i1", date = LocalDate(2026, 4, 15))
            val vm = buildViewModel(
                habitInstanceRepository = FakeHabitInstanceRepository(instance),
                userRepository = FakeUserRepository(balancedUser()),
                instanceId = "i1"
            )

            vm.state.test {
                val state = awaitItem()
                // Skip loading states if any
                val finalState = if (state.isLoading || state.habit == null) awaitItem() else state

                assertEquals(StrictnessPreset.BALANCED, finalState.habit!!.strictnessPreset)
                assertEquals(UndoPolicy.TODAY_ONLY, finalState.habit.undoPolicy)
                cancelAndIgnoreRemainingEvents()
            }
        }

    // ── Test B: snoozesUsedToday ──────────────────────────────────────────────

    @Test
    fun `given snooze state with count 2, snoozesUsedToday is 2`() =
        runTest(UnconfinedTestDispatcher()) {
            val instance = habitInstance(id = "i2", date = LocalDate(2026, 4, 15))
            val snoozeRepo = FakeSnoozeRepository(
                stateByInstance = mapOf(
                    "i2" to SnoozeState(
                        habitInstanceId = "i2",
                        scheduledTime = FIXED_NOW,
                        snoozeCount = 2
                    )
                )
            )
            val vm = buildViewModel(
                habitInstanceRepository = FakeHabitInstanceRepository(instance),
                snoozeRepository = snoozeRepo,
                instanceId = "i2"
            )

            vm.state.test {
                val state = awaitItem()
                val finalState = if (state.isLoading || state.habit == null) awaitItem() else state

                assertEquals(2, finalState.habit!!.snoozesUsedToday)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given no snooze state, snoozesUsedToday is 0`() = runTest(UnconfinedTestDispatcher()) {
        val instance = habitInstance(id = "i3", date = LocalDate(2026, 4, 15))
        val vm = buildViewModel(
            habitInstanceRepository = FakeHabitInstanceRepository(instance),
            snoozeRepository = FakeSnoozeRepository(emptyMap()),
            instanceId = "i3"
        )

        vm.state.test {
            val state = awaitItem()
            val finalState = if (state.isLoading || state.habit == null) awaitItem() else state

            assertEquals(0, finalState.habit!!.snoozesUsedToday)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Test C: skipsThisWeek ─────────────────────────────────────────────────

    @Test
    fun `given 3 skipped instances in current ISO week and 1 skipped outside, skipsThisWeek is 3`() =
        runTest(UnconfinedTestDispatcher()) {
            // Current week: Mon 2026-04-13 .. Sun 2026-04-19
            // "today" pinned to Wed 2026-04-15 via FIXED_CLOCK
            val targetInstance = habitInstance(id = "i4", date = LocalDate(2026, 4, 15))
            val allInstances = listOf(
                // 3 SKIPPED within current ISO week
                habitInstance(id = "iA", date = WEEK_MONDAY, status = HabitStatus.SKIPPED), // Mon
                habitInstance(
                    id = "iB",
                    date = LocalDate(2026, 4, 14),
                    status = HabitStatus.SKIPPED
                ), // Tue
                habitInstance(
                    id = "iC",
                    date = LocalDate(2026, 4, 15),
                    status = HabitStatus.SKIPPED
                ), // Wed (today)
                // 1 SKIPPED one week earlier — outside current ISO week
                habitInstance(
                    id = "iD",
                    date = LocalDate(2026, 4, 6),
                    status = HabitStatus.SKIPPED
                ), // Mon week 15
                // target instance (PENDING)
                targetInstance
            )

            val vm = buildViewModel(
                habitInstanceRepository = FakeHabitInstanceRepository(
                    instance = targetInstance,
                    allInstances = allInstances
                ),
                clock = FIXED_CLOCK,
                instanceId = "i4"
            )

            vm.state.test {
                val state = awaitItem()
                val finalState = if (state.isLoading || state.habit == null) awaitItem() else state

                assertEquals(3, finalState.habit!!.skipsThisWeek)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given null user, state finishes loading with null habit`() =
        runTest(UnconfinedTestDispatcher()) {
            val instance = habitInstance(id = "i5", date = LocalDate(2026, 4, 15))
            val vm = buildViewModel(
                habitInstanceRepository = FakeHabitInstanceRepository(instance),
                user = null,
                instanceId = "i5"
            )
            vm.state.test {
                val state = awaitItem()
                val final = if (state.isLoading || state.habit != null) awaitItem() else state
                assertEquals(null, final.habit)
                assertEquals(false, final.isLoading)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
