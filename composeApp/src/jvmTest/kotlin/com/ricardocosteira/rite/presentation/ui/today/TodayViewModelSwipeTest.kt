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
import com.ricardocosteira.rite.domain.usecases.CompleteHabit
import com.ricardocosteira.rite.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.rite.domain.usecases.GenerateInstanceForHabit
import com.ricardocosteira.rite.domain.usecases.ProcessEndOfDay
import com.ricardocosteira.rite.domain.usecases.SkipHabit
import com.ricardocosteira.rite.domain.usecases.UndoHabit
import com.ricardocosteira.rite.domain.usecases.UndoLastIncrement
import com.ricardocosteira.rite.domain.usecases.UuidProvider
import com.ricardocosteira.rite.notifications.HabitNotification
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
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
 * Uses real repository implementations backed by an in-memory JdbcSqliteDriver so that
 * the full data layer (SQLDelight queries, mappers, dispatchers) is exercised end-to-end.
 *
 * All dispatchers (IO, Default, Main) are replaced with the test dispatcher so that
 * virtual-time control is deterministic — no real threads, no polling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelSwipeTest {

    @Test
    fun `deleteHabit removes habit from state and sets pendingDelete`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            // Given
            val deps = buildDependencies(testDispatcher)
            val inputHabitId = "habit-1"
            val inputHabitName = "Morning Meditation"
            deps.seedHabitWithTodayInstance(habitId = inputHabitId, habitName = inputHabitName)

            val viewModel = buildViewModel(deps, testDispatcher)
            // Keep a persistent subscriber so WhileSubscribed keeps the upstream alive.
            backgroundScope.launch { viewModel.state.collect {} }
            advanceUntilIdle()

            assertTrue(
                viewModel.state.value.pendingDaily.any { it.habitId == inputHabitId },
                "Expected habit to be in pendingDaily before delete"
            )

            // When — deleteHabit sets _pendingDelete; the reactive pipeline re-emits.
            viewModel.deleteHabit(inputHabitId)

            // Then — wait for the reactive pipeline to emit a state where the habit is gone
            // and pendingDelete is set. state.first suspends until the predicate matches,
            // cooperatively running the test dispatcher coroutines.
            val actualState = viewModel.state.first { state ->
                state.pendingDelete?.habitId == inputHabitId
            }
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
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            // Given
            val deps = buildDependencies(testDispatcher)
            val inputHabitId = "habit-1"
            deps.seedHabitWithTodayInstance(habitId = inputHabitId)

            val viewModel = buildViewModel(deps, testDispatcher)
            // Keep a persistent subscriber so WhileSubscribed keeps the upstream alive.
            backgroundScope.launch { viewModel.state.collect {} }
            advanceUntilIdle()

            // When — delete. Do NOT call advanceUntilIdle() — it would drain the 5s delay.
            viewModel.deleteHabit(inputHabitId)

            // Then — habit still in real repository (delete is deferred).
            val actualHabit: Habit? = deps.habitRepository.getHabitById(inputHabitId)
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
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            // Given
            val deps = buildDependencies(testDispatcher)
            val inputHabitId = "habit-1"
            deps.seedHabitWithTodayInstance(habitId = inputHabitId)

            val viewModel = buildViewModel(deps, testDispatcher)
            // Keep a persistent subscriber so WhileSubscribed keeps the upstream alive.
            backgroundScope.launch { viewModel.state.collect {} }
            advanceUntilIdle()

            // When — delete, then advance past the undo timeout (5 seconds)
            viewModel.deleteHabit(inputHabitId)
            advanceTimeBy(6_000L)
            advanceUntilIdle()

            // Then — habit deleted from repository after timeout
            assertNull(
                viewModel.state.value.pendingDelete,
                "Expected pendingDelete to be null after delete committed"
            )
            val actualHabit: Habit? = deps.habitRepository.getHabitById(inputHabitId)
            assertNull(
                actualHabit,
                "Expected habit to be deleted from repository after undo timeout"
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `undoDelete cancels pending delete and restores habit to state`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            // Given
            val deps = buildDependencies(testDispatcher)
            val inputHabitId = "habit-1"
            deps.seedHabitWithTodayInstance(habitId = inputHabitId)

            val viewModel = buildViewModel(deps, testDispatcher)
            // Keep a persistent subscriber so WhileSubscribed keeps the upstream alive.
            backgroundScope.launch { viewModel.state.collect {} }
            advanceUntilIdle()

            viewModel.deleteHabit(inputHabitId)
            // Wait for the reactive pipeline to reflect the deletion before checking undo.
            viewModel.state.first { it.pendingDelete?.habitId == inputHabitId }

            // When — undo before timeout
            viewModel.undoDelete()

            // Then — wait for the reactive pipeline to restore the habit (pendingDelete cleared,
            // habit back in pendingDaily because _pendingDelete = null re-runs the combine filter).
            val actualState = viewModel.state.first { state ->
                state.pendingDelete == null && state.pendingDaily.any { it.habitId == inputHabitId }
            }
            assertNull(
                actualState.pendingDelete,
                "Expected pendingDelete to be null after undoDelete"
            )
            assertTrue(
                actualState.pendingDaily.any { it.habitId == inputHabitId },
                "Expected habit to be restored to pendingDaily after undoDelete"
            )
            // Habit still exists in repository (delete was cancelled)
            val actualHabit: Habit? = deps.habitRepository.getHabitById(inputHabitId)
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
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            // Given
            val deps = buildDependencies(testDispatcher)
            val inputHabitId = "habit-1"
            deps.seedHabitWithTodayInstance(habitId = inputHabitId)

            val viewModel = buildViewModel(deps, testDispatcher)
            // Keep a persistent subscriber so WhileSubscribed keeps the upstream alive.
            backgroundScope.launch { viewModel.state.collect {} }
            advanceUntilIdle()

            viewModel.deleteHabit(inputHabitId)

            // When — undo immediately, then advance past timeout
            viewModel.undoDelete()
            advanceTimeBy(6_000L)
            advanceUntilIdle()

            // Then — habit still in repository (undo cancelled the deferred delete)
            val actualHabit: Habit? = deps.habitRepository.getHabitById(inputHabitId)
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
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            try {
                // Given
                val deps = buildDependencies(testDispatcher)
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

                val viewModel = buildViewModel(deps, testDispatcher)
                // Keep a persistent subscriber so WhileSubscribed keeps the upstream alive.
                backgroundScope.launch { viewModel.state.collect {} }
                advanceUntilIdle()

                assertTrue(
                    viewModel.state.value.pendingDaily.any { it.habitId == inputFirstHabitId } &&
                        viewModel.state.value.pendingDaily.any { it.habitId == inputSecondHabitId },
                    "Expected both habits to be loaded into pendingDaily"
                )

                // When — delete first, then delete second (cancels first undo job)
                viewModel.deleteHabit(inputFirstHabitId)
                viewModel.deleteHabit(inputSecondHabitId)

                // Then — wait for the reactive pipeline to reflect the second deletion.
                // state.first cooperatively runs test-dispatcher coroutines until the predicate matches.
                val actualState = viewModel.state.first { state ->
                    state.pendingDelete?.habitId == inputSecondHabitId
                }
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

                // Advance past timeout — second habit should be deleted from repository.
                advanceTimeBy(6_000L)
                advanceUntilIdle()

                assertNull(
                    viewModel.state.value.pendingDelete,
                    "Expected pendingDelete to be null after timeout"
                )

                val actualFirstHabit: Habit? = deps.habitRepository.getHabitById(inputFirstHabitId)
                val actualSecondHabit: Habit? = deps.habitRepository.getHabitById(
                    inputSecondHabitId
                )
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

    private fun buildDependencies(
        testDispatcher: kotlinx.coroutines.CoroutineDispatcher
    ): TestDependencies = TestDependencies(testDispatcher)

    private fun buildViewModel(
        deps: TestDependencies,
        testDispatcher: kotlinx.coroutines.CoroutineDispatcher
    ): TodayViewModel {
        val today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val dateProvider = com.ricardocosteira.rite.domain.time.FakeCurrentDateProvider(
            initial = today
        )
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

    inner class TestDependencies(testDispatcher: kotlinx.coroutines.CoroutineDispatcher) {

        private val driver: JdbcSqliteDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
            RiteDatabase.Schema.create(it)
        }
        private val database: RiteDatabase = RiteDatabase(driver)

        val habitRepository: HabitRepositoryImpl =
            HabitRepositoryImpl(database = database, ioDispatcher = testDispatcher)

        val habitInstanceRepository: HabitInstanceRepositoryImpl =
            HabitInstanceRepositoryImpl(database = database, ioDispatcher = testDispatcher)

        val userRepository: UserRepositoryImpl =
            UserRepositoryImpl(database = database, ioDispatcher = testDispatcher)

        private val completionEventRepository: HabitCompletionEventRepositoryImpl =
            HabitCompletionEventRepositoryImpl(database = database, ioDispatcher = testDispatcher)

        private val leavePeriodRepository: LeavePeriodRepositoryImpl =
            LeavePeriodRepositoryImpl(database = database, ioDispatcher = testDispatcher)

        private val uuidProvider: UuidProvider = object : UuidProvider {
            private var counter: Int = 0
            override fun generate(): String = "uuid-${++counter}"
        }

        val generateDailyHabits: GenerateDailyHabits = GenerateDailyHabits(
            userRepository = userRepository,
            habitRepository = habitRepository,
            generateInstanceForHabit = GenerateInstanceForHabit(
                habitRepository = habitRepository,
                habitInstanceRepository = habitInstanceRepository,
                leavePeriodRepository = leavePeriodRepository,
                uuidProvider = uuidProvider
            )
        )
        val processEndOfDay: ProcessEndOfDay = ProcessEndOfDay(
            userRepository = userRepository,
            habitInstanceRepository = habitInstanceRepository,
            habitRepository = habitRepository
        )
        val completeHabit: CompleteHabit = CompleteHabit(
            habitInstanceRepository = habitInstanceRepository,
            habitRepository = habitRepository,
            habitCompletionEventRepository = completionEventRepository
        )
        val skipHabit: SkipHabit = SkipHabit(
            habitInstanceRepository = habitInstanceRepository,
            userRepository = userRepository
        )
        val undoHabit: UndoHabit = UndoHabit(
            habitInstanceRepository = habitInstanceRepository,
            habitCompletionEventRepository = completionEventRepository,
            habitRepository = habitRepository,
            userRepository = userRepository
        )
        val undoLastIncrement: UndoLastIncrement = UndoLastIncrement(
            habitInstanceRepository = habitInstanceRepository,
            habitCompletionEventRepository = completionEventRepository,
            habitRepository = habitRepository
        )

        suspend fun seedHabitWithTodayInstance(
            habitId: String = "habit-1",
            habitName: String = "Morning Meditation",
            instanceId: String = "instance-1"
        ) {
            val inputToday: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date

            // Ensure a user exists so GenerateDailyHabits / use cases can resolve it.
            if (userRepository.getUser() == null) {
                userRepository.createDefaultUser(timezone = TimeZone.UTC)
            }

            val inputHabit: Habit = Habit(
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
            val inputSchedule: HabitSchedule = HabitSchedule(
                id = "schedule-$habitId",
                habitId = habitId,
                scheduleType = ScheduleType.DAILY,
                startDate = inputToday,
                endDate = null,
                quota = 1
            )
            val inputInstance: HabitInstance = HabitInstance(
                id = instanceId,
                habitId = habitId,
                date = inputToday,
                status = HabitStatus.PENDING,
                completedValue = null,
                targetValue = null,
                consecutiveSkipsAtCreation = 0,
                createdAt = Clock.System.now()
            )

            habitRepository.createHabit(
                habit = inputHabit,
                schedule = inputSchedule,
                reminder = null
            )
            habitInstanceRepository.createInstance(inputInstance)
        }
    }
}
