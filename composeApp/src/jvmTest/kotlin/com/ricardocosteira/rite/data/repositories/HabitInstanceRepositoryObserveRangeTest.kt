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
        val habitRepository =
            HabitRepositoryImpl(database = database, ioDispatcher = testDispatcher)
        val instanceRepository =
            HabitInstanceRepositoryImpl(database = database, ioDispatcher = testDispatcher)

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
