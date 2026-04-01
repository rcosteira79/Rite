package com.ricardocosteira.habitlock.presentation.ui.today

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitInstance
import com.ricardocosteira.habitlock.domain.models.HabitSchedule
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import com.ricardocosteira.habitlock.domain.usecases.CompleteHabit
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.habitlock.domain.usecases.ProcessEndOfDay
import com.ricardocosteira.habitlock.domain.usecases.SkipHabit
import com.ricardocosteira.habitlock.domain.usecases.UndoHabit
import com.ricardocosteira.habitlock.domain.usecases.UndoLastIncrement
import com.ricardocosteira.habitlock.domain.usecases.UuidProvider
import com.ricardocosteira.habitlock.fakes.FakeHabitCompletionEventRepository
import com.ricardocosteira.habitlock.fakes.FakeHabitInstanceRepository
import com.ricardocosteira.habitlock.fakes.FakeHabitRepository
import com.ricardocosteira.habitlock.fakes.FakeLeavePeriodRepository
import com.ricardocosteira.habitlock.fakes.FakeUserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Integration tests for TodayViewModel swipe-to-delete with deferred undo.
 *
 * CONCERN: These tests use fake repository interfaces rather than real
 * SQLDelight-backed repositories. This is a pragmatic trade-off: the
 * SQLDelight layer requires platform-specific drivers (JdbcSqliteDriver
 * for JVM) which adds significant wiring complexity. The fakes cover
 * the essential contract. A follow-up task could set up a real
 * in-memory SQLite driver for full integration coverage.
 *
 * NOTE ON DISPATCHER: `loadTodayHabits()` uses `withContext(Dispatchers.Default)`
 * for the habit mapping step. Since `StandardTestDispatcher` does not replace
 * `Dispatchers.Default`, `advanceUntilIdle()` cannot guarantee the real-thread work
 * is done. Tests use `awaitStateCondition` (a polling helper) to wait for state
 * to settle rather than relying on virtual-time advancement alone.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelSwipeTest {

    /**
     * Polls the ViewModel state until [condition] is true, using short coroutine delays
     * to yield control and allow pending scheduler tasks to run between polls.
     *
     * Background: `loadTodayHabits` dispatches a mapping step to `Dispatchers.Default`
     * (a real thread pool). After `advanceUntilIdle()`, the test scheduler may be
     * temporarily empty while that real-thread work is in-flight. Short `delay` calls
     * here re-enter the coroutine machinery and process the resumed continuation once
     * the real thread posts it back to the Main/test dispatcher.
     *
     * Uses 1ms virtual-time steps to stay well within the 5-second undo timeout budget.
     */
    private suspend fun TestScope.awaitState(
        viewModel: TodayViewModel,
        maxIterations: Int = 100,
        condition: (TodayState) -> Boolean
    ): Boolean {
        repeat(maxIterations) {
            if (condition(viewModel.state.value)) return true
            kotlinx.coroutines.delay(1)
            advanceUntilIdle()
        }
        return condition(viewModel.state.value)
    }

    @Test
    fun `deleteHabit removes habit from state and sets pendingDelete`() = runTest {
        // Given
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val deps = buildDependencies()
            val inputHabitId = "habit-1"
            val inputHabitName = "Morning Meditation"
            deps.seedHabitWithTodayInstance(habitId = inputHabitId, habitName = inputHabitName)

            val viewModel = buildViewModelWithScheduler(deps)
            advanceUntilIdle()

            // Wait for loadTodayHabits to complete (including Dispatchers.Default step)
            val isLoaded: Boolean = awaitState(viewModel) {
                it.pendingDaily.any { habit -> habit.habitId == inputHabitId }
            }
            assertTrue(isLoaded, "Expected habit to be in pendingDaily before delete")

            // When — deleteHabit is synchronous: state updates happen immediately before returning
            viewModel.deleteHabit(inputHabitId)
            // Do NOT call advanceUntilIdle() here — it would advance virtual time past the 5-second
            // undo timeout, committing the delete and resetting pendingDelete.

            // Then
            val actualState = viewModel.state.value
            assertTrue(
                actualState.pendingDaily.none { it.habitId == inputHabitId },
                "Expected habit to be removed from pendingDaily after delete"
            )
            assertNotNull(
                actualState.pendingDelete,
                "Expected pendingDelete to be set after delete"
            )
            val actualPendingDelete: PendingDelete = actualState.pendingDelete
            assertEquals(
                inputHabitId,
                actualPendingDelete.habitId,
                "Expected pendingDelete.habitId to match deleted habit"
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `deleteHabit defers actual repository delete until timeout`() = runTest {
        // Given
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val deps = buildDependencies()
            val inputHabitId = "habit-1"
            deps.seedHabitWithTodayInstance(habitId = inputHabitId)

            val viewModel = buildViewModelWithScheduler(deps)
            awaitState(viewModel) { it.pendingDaily.any { habit -> habit.habitId == inputHabitId } }

            // When — delete. Do NOT call advanceUntilIdle() — it would drain the 5s delay.
            viewModel.deleteHabit(inputHabitId)

            // Then — habit still in fake repository (delete is deferred)
            val actualHabit = deps.fakeHabitRepository.getHabitOrNull(inputHabitId)
            assertNotNull(
                actualHabit,
                "Expected habit to still exist in repository before undo timeout"
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `deleteHabit commits to repository after undo timeout`() = runTest {
        // Given
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val deps = buildDependencies()
            val inputHabitId = "habit-1"
            deps.seedHabitWithTodayInstance(habitId = inputHabitId)

            val viewModel = buildViewModelWithScheduler(deps)
            awaitState(viewModel) { it.pendingDaily.any { habit -> habit.habitId == inputHabitId } }

            // When — delete, then advance past the undo timeout (5 seconds)
            viewModel.deleteHabit(inputHabitId)
            advanceTimeBy(6_000L)
            advanceUntilIdle()

            // Then — habit deleted from repository after timeout
            val actualHabit = deps.fakeHabitRepository.getHabitOrNull(inputHabitId)
            assertNull(
                actualHabit,
                "Expected habit to be deleted from repository after undo timeout"
            )
            assertNull(
                viewModel.state.value.pendingDelete,
                "Expected pendingDelete to be null after delete committed"
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `undoDelete cancels pending delete and restores habit to state`() = runTest {
        // Given
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val deps = buildDependencies()
            val inputHabitId = "habit-1"
            deps.seedHabitWithTodayInstance(habitId = inputHabitId)

            val viewModel = buildViewModelWithScheduler(deps)
            awaitState(viewModel) { it.pendingDaily.any { habit -> habit.habitId == inputHabitId } }

            viewModel.deleteHabit(inputHabitId)
            // Verify habit was removed from state synchronously
            assertTrue(
                viewModel.state.value.pendingDaily.none { it.habitId == inputHabitId },
                "Expected habit to be removed from state after deleteHabit"
            )

            // When — undo before timeout. undoDelete() is synchronous (clears pendingDelete)
            // and then triggers loadTodayHabits() via viewModelScope.launch.
            viewModel.undoDelete()
            // Wait for loadTodayHabits triggered by undoDelete to complete
            val isRestored: Boolean = awaitState(viewModel) {
                it.pendingDelete == null &&
                    it.pendingDaily.any { habit -> habit.habitId == inputHabitId }
            }

            // Then
            assertTrue(isRestored, "Expected habit to be restored to pendingDaily after undoDelete")
            assertNull(
                viewModel.state.value.pendingDelete,
                "Expected pendingDelete to be null after undoDelete"
            )
            // Habit still exists in repository (delete was cancelled)
            val actualHabit = deps.fakeHabitRepository.getHabitOrNull(inputHabitId)
            assertNotNull(
                actualHabit,
                "Expected habit to still exist in repository after undoDelete"
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `undoDelete does not commit to repository even after timeout elapses`() = runTest {
        // Given
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val deps = buildDependencies()
            val inputHabitId = "habit-1"
            deps.seedHabitWithTodayInstance(habitId = inputHabitId)

            val viewModel = buildViewModelWithScheduler(deps)
            awaitState(viewModel) { it.pendingDaily.any { habit -> habit.habitId == inputHabitId } }

            viewModel.deleteHabit(inputHabitId)

            // When — undo immediately, then advance past timeout
            viewModel.undoDelete()
            advanceTimeBy(6_000L)
            advanceUntilIdle()

            // Then — habit still in repository (undo cancelled the deferred delete)
            val actualHabit = deps.fakeHabitRepository.getHabitOrNull(inputHabitId)
            assertNotNull(
                actualHabit,
                "Expected habit to remain after undo, even after timeout elapsed"
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `second delete cancels first pending delete and commits it before starting new undo`() =
        runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            try {
                val deps = buildDependencies()
                val inputFirstHabitId = "habit-1"
                val inputSecondHabitId = "habit-2"
                deps.seedHabitWithTodayInstance(
                    habitId = inputFirstHabitId,
                    instanceId = "instance-1"
                )
                deps.seedHabitWithTodayInstance(
                    habitId = inputSecondHabitId,
                    instanceId = "instance-2"
                )

                val viewModel = buildViewModelWithScheduler(deps)
                awaitState(viewModel) {
                    it.pendingDaily.any { habit -> habit.habitId == inputFirstHabitId } &&
                        it.pendingDaily.any { habit -> habit.habitId == inputSecondHabitId }
                }

                // When — delete first, then delete second (cancels first undo job)
                viewModel.deleteHabit(inputFirstHabitId)
                viewModel.deleteHabit(inputSecondHabitId)
                // Do NOT advanceUntilIdle — second undo job is still pending

                // Then — second habit is in pending delete state
                val actualState = viewModel.state.value
                assertTrue(
                    actualState.pendingDaily.none { it.habitId == inputSecondHabitId },
                    "Expected second habit to be removed from state"
                )
                val actualPendingDelete: PendingDelete? = actualState.pendingDelete
                assertNotNull(
                    actualPendingDelete,
                    "Expected pendingDelete to be set"
                )
                assertEquals(
                    inputSecondHabitId,
                    actualPendingDelete.habitId,
                    "Expected pendingDelete to refer to second habit"
                )

                // Advance past timeout — second habit should be deleted from repository
                advanceTimeBy(6_000L)
                advanceUntilIdle()

                val actualFirstHabit = deps.fakeHabitRepository.getHabitOrNull(inputFirstHabitId)
                val actualSecondHabit = deps.fakeHabitRepository.getHabitOrNull(inputSecondHabitId)
                assertNull(
                    actualFirstHabit,
                    "Expected first habit to be deleted from repository (its undo job was cancelled)"
                )
                assertNull(
                    actualSecondHabit,
                    "Expected second habit to be deleted from repository after timeout"
                )
            } finally {
                Dispatchers.resetMain()
            }
        }

    private fun buildDependencies(): TestDependencies = TestDependencies()

    private fun buildViewModelWithScheduler(deps: TestDependencies): TodayViewModel =
        TodayViewModel(
            userRepository = deps.fakeUserRepository,
            habitRepository = deps.fakeHabitRepository,
            habitInstanceRepository = deps.fakeInstanceRepository,
            generateDailyHabits = deps.generateDailyHabits,
            processEndOfDay = deps.processEndOfDay,
            completeHabit = deps.completeHabit,
            skipHabit = deps.skipHabit,
            undoHabit = deps.undoHabit,
            undoLastIncrement = deps.undoLastIncrement
        )

    inner class TestDependencies {
        val fakeHabitRepository = FakeHabitRepository()
        val fakeInstanceRepository = FakeHabitInstanceRepository()
        val fakeUserRepository = FakeUserRepository()
        val fakeCompletionEventRepository = FakeHabitCompletionEventRepository()
        val fakeLeavePeriodRepository = FakeLeavePeriodRepository()

        private val fakeUuidProvider: UuidProvider = object : UuidProvider {
            private var counter: Int = 0
            override fun generate(): String = "uuid-${++counter}"
        }

        val generateDailyHabits = GenerateDailyHabits(
            userRepository = fakeUserRepository,
            habitRepository = fakeHabitRepository,
            habitInstanceRepository = fakeInstanceRepository,
            leavePeriodRepository = fakeLeavePeriodRepository,
            uuidProvider = fakeUuidProvider
        )
        val processEndOfDay = ProcessEndOfDay(
            userRepository = fakeUserRepository,
            habitInstanceRepository = fakeInstanceRepository,
            habitRepository = fakeHabitRepository
        )
        val completeHabit = CompleteHabit(
            habitInstanceRepository = fakeInstanceRepository,
            habitRepository = fakeHabitRepository,
            habitCompletionEventRepository = fakeCompletionEventRepository
        )
        val skipHabit = SkipHabit(
            habitInstanceRepository = fakeInstanceRepository,
            userRepository = fakeUserRepository
        )
        val undoHabit = UndoHabit(
            habitInstanceRepository = fakeInstanceRepository,
            habitCompletionEventRepository = fakeCompletionEventRepository,
            habitRepository = fakeHabitRepository,
            userRepository = fakeUserRepository
        )
        val undoLastIncrement = UndoLastIncrement(
            habitInstanceRepository = fakeInstanceRepository,
            habitCompletionEventRepository = fakeCompletionEventRepository
        )

        fun seedHabitWithTodayInstance(
            habitId: String = "habit-1",
            habitName: String = "Morning Meditation",
            instanceId: String = "instance-1"
        ) {
            val inputToday: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date

            val inputHabit = Habit(
                id = habitId,
                name = habitName,
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
            val inputSchedule = HabitSchedule(
                id = "schedule-$habitId",
                habitId = habitId,
                scheduleType = ScheduleType.DAILY,
                startDate = inputToday,
                endDate = null,
                quota = 1
            )
            val inputInstance = HabitInstance(
                id = instanceId,
                habitId = habitId,
                date = inputToday,
                status = HabitStatus.PENDING,
                completedValue = null,
                targetValue = null,
                consecutiveSkipsAtCreation = 0,
                createdAt = Clock.System.now()
            )

            fakeHabitRepository.addHabit(inputHabit, inputSchedule)
            fakeInstanceRepository.addInstance(inputInstance)
        }
    }
}
