package com.ricardocosteira.rite.domain.usecases

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ricardocosteira.rite.data.database.RiteDatabase
import com.ricardocosteira.rite.data.repositories.HabitInstanceRepositoryImpl
import com.ricardocosteira.rite.data.repositories.HabitRepositoryImpl
import com.ricardocosteira.rite.data.repositories.UserRepositoryImpl
import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitSchedule
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class ProcessEndOfDayTest {

    private fun buildDeps(): TestDeps {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
            RiteDatabase.Schema.create(it)
        }
        val db = RiteDatabase(driver)
        return TestDeps(
            userRepository = UserRepositoryImpl(database = db, ioDispatcher = Dispatchers.IO),
            habitRepository = HabitRepositoryImpl(database = db, ioDispatcher = Dispatchers.IO),
            habitInstanceRepository = HabitInstanceRepositoryImpl(
                database = db,
                ioDispatcher = Dispatchers.IO
            )
        )
    }

    private data class TestDeps(
        val userRepository: UserRepositoryImpl,
        val habitRepository: HabitRepositoryImpl,
        val habitInstanceRepository: HabitInstanceRepositoryImpl
    )

    private fun buildProcessEndOfDay(deps: TestDeps): ProcessEndOfDay = ProcessEndOfDay(
        userRepository = deps.userRepository,
        habitInstanceRepository = deps.habitInstanceRepository,
        habitRepository = deps.habitRepository
    )

    private suspend fun seedUser(deps: TestDeps) {
        deps.userRepository.createDefaultUser(timezone = TimeZone.UTC)
    }

    private fun buildHabit(habitId: String): Habit = Habit(
        id = habitId,
        name = "Test Habit",
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

    @Test
    fun `WEEKLY habit is marked FAILED after last specific day passes`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val deps = buildDeps()
            seedUser(deps)
            val processEndOfDay = buildProcessEndOfDay(deps)

            val inputHabitId = "habit-weekly"
            val inputSchedule = HabitSchedule(
                id = "schedule-1",
                habitId = inputHabitId,
                scheduleType = ScheduleType.WEEKLY,
                startDate = LocalDate(2026, 3, 30),
                endDate = null,
                quota = 3,
                weekStartDay = DayOfWeek.MONDAY,
                specificDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            )
            val inputWeekStart = LocalDate(2026, 3, 30)
            val inputInstance = HabitInstance(
                id = "instance-1",
                habitId = inputHabitId,
                date = inputWeekStart,
                status = HabitStatus.PENDING,
                completedValue = 1,
                targetValue = 3,
                consecutiveSkipsAtCreation = 0,
                createdAt = Clock.System.now()
            )

            deps.habitRepository.createHabit(
                habit = buildHabit(inputHabitId),
                schedule = inputSchedule,
                reminder = null
            )
            deps.habitInstanceRepository.createInstance(inputInstance)

            processEndOfDay.execute()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `FLEXIBLE_WEEKLY habit is marked FAILED at week boundary`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val deps = buildDeps()
            seedUser(deps)
            val processEndOfDay = buildProcessEndOfDay(deps)

            val inputHabitId = "habit-flexible"
            val inputSchedule = HabitSchedule(
                id = "schedule-1",
                habitId = inputHabitId,
                scheduleType = ScheduleType.FLEXIBLE_WEEKLY,
                startDate = LocalDate(2026, 3, 30),
                endDate = null,
                quota = 3,
                weekStartDay = DayOfWeek.MONDAY
            )
            val inputWeekStart = LocalDate(2026, 3, 30)
            val inputInstance = HabitInstance(
                id = "instance-1",
                habitId = inputHabitId,
                date = inputWeekStart,
                status = HabitStatus.PENDING,
                completedValue = 1,
                targetValue = 3,
                consecutiveSkipsAtCreation = 0,
                createdAt = Clock.System.now()
            )

            deps.habitRepository.createHabit(
                habit = buildHabit(inputHabitId),
                schedule = inputSchedule,
                reminder = null
            )
            deps.habitInstanceRepository.createInstance(inputInstance)

            processEndOfDay.execute()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
