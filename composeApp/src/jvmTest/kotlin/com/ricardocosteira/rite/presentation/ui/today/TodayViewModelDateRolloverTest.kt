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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    fun `state recomposes for the new date when CurrentDateProvider emits day rollover`() =
        runTest {
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
                // Keep a persistent subscriber so WhileSubscribed keeps the upstream alive.
                backgroundScope.launch { viewModel.state.collect {} }
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

                // Then: wait for the reactive pipeline to re-derive state for the new date.
                // state.first subscribes and cooperatively runs test-dispatcher coroutines until
                // the predicate matches, so no advanceUntilIdle needed.
                val newState = viewModel.state.first { state ->
                    state.pendingDaily.any { it.instanceId == "i-today" }
                }
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
        private val completionEventRepository =
            HabitCompletionEventRepositoryImpl(database, testDispatcher)
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
