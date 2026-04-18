package com.ricardocosteira.rite.presentation.ui.today

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
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
import kotlinx.datetime.toLocalDateTime

/**
 * Verifies TodayViewModel emits typed TodayEvents carrying the habit name
 * so the TodayScreen snackbar layer can render RiteSnackbar with italic accents.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelSnackbarTest {

    @Test
    fun `completeHabit binary emits HabitCompleted with habit name`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val deps = TestDependencies(testDispatcher)
            val habitId = "habit-1"
            val habitName = "Morning sit"
            val instanceId = "instance-1"
            deps.seedBinaryPending(habitId, habitName, instanceId)

            val vm = buildViewModel(deps, testDispatcher)
            advanceUntilIdle()

            vm.events.test {
                vm.completeHabit(instanceId)
                advanceUntilIdle()
                val event = awaitItem()
                assertTrue(event is TodayEvent.HabitCompleted)
                assertEquals(habitName, (event as TodayEvent.HabitCompleted).habitName)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `skipHabit success emits HabitSkipped with habit name`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val deps = TestDependencies(testDispatcher)
            val habitId = "habit-2"
            val habitName = "Read before sleep"
            val instanceId = "instance-2"
            deps.seedBinaryPending(habitId, habitName, instanceId)

            val vm = buildViewModel(deps, testDispatcher)
            advanceUntilIdle()

            vm.events.test {
                vm.skipHabit(instanceId)
                advanceUntilIdle()
                val event = awaitItem()
                assertTrue(event is TodayEvent.HabitSkipped)
                assertEquals(habitName, (event as TodayEvent.HabitSkipped).habitName)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `deleteHabit emits HabitDeleted with habit name`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val deps = TestDependencies(testDispatcher)
            val habitId = "habit-3"
            val habitName = "Strength work"
            deps.seedBinaryPending(habitId, habitName, "instance-3")

            val vm = buildViewModel(deps, testDispatcher)
            advanceUntilIdle()

            vm.events.test {
                vm.deleteHabit(habitId)
                val event = awaitItem()
                assertTrue(event is TodayEvent.HabitDeleted)
                assertEquals(habitName, (event as TodayEvent.HabitDeleted).habitName)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun buildViewModel(
        deps: TestDependencies,
        testDispatcher: kotlinx.coroutines.CoroutineDispatcher
    ): TodayViewModel = TodayViewModel(
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
        defaultDispatcher = testDispatcher
    )

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
            habitInstanceRepository = habitInstanceRepository,
            leavePeriodRepository = leavePeriodRepository,
            uuidProvider = uuidProvider
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

        suspend fun seedBinaryPending(habitId: String, habitName: String, instanceId: String) {
            val today: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date

            if (userRepository.getUser() == null) {
                userRepository.createDefaultUser(timezone = TimeZone.UTC)
            }

            val habit: Habit = Habit(
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
            val schedule: HabitSchedule = HabitSchedule(
                id = "schedule-$habitId",
                habitId = habitId,
                scheduleType = ScheduleType.DAILY,
                startDate = today,
                endDate = null,
                quota = 1
            )
            val instance: HabitInstance = HabitInstance(
                id = instanceId,
                habitId = habitId,
                date = today,
                status = HabitStatus.PENDING,
                completedValue = null,
                targetValue = null,
                consecutiveSkipsAtCreation = 0,
                createdAt = Clock.System.now()
            )

            habitRepository.createHabit(habit = habit, schedule = schedule, reminder = null)
            habitInstanceRepository.createInstance(instance)
        }
    }
}
