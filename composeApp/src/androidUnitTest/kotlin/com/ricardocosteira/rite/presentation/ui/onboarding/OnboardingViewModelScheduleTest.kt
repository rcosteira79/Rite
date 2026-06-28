package com.ricardocosteira.rite.presentation.ui.onboarding

import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitReminder
import com.ricardocosteira.rite.domain.models.HabitSchedule
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.LeavePeriod
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.domain.models.UndoPolicy
import com.ricardocosteira.rite.domain.models.User
import com.ricardocosteira.rite.domain.repositories.HabitInstanceRepository
import com.ricardocosteira.rite.domain.repositories.HabitRepository
import com.ricardocosteira.rite.domain.repositories.LeavePeriodRepository
import com.ricardocosteira.rite.domain.repositories.UserRepository
import com.ricardocosteira.rite.domain.usecases.ApplyStrictnessPreset
import com.ricardocosteira.rite.domain.usecases.CreateHabit
import com.ricardocosteira.rite.domain.usecases.GenerateDailyHabits
import com.ricardocosteira.rite.domain.usecases.GenerateInstanceForHabit
import com.ricardocosteira.rite.domain.usecases.UuidProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Before

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class OnboardingViewModelScheduleTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val fakeHabitRepository = FakeHabitRepository()
    private val fakeUserRepository = FakeUserRepository()
    private val fakeHabitInstanceRepository = FakeHabitInstanceRepository()
    private val fakeLeavePeriodRepository = FakeLeavePeriodRepository()
    private val fakeUuidProvider = FakeUuidProvider()

    private val generateInstanceForHabit = GenerateInstanceForHabit(
        fakeHabitRepository,
        fakeHabitInstanceRepository,
        fakeLeavePeriodRepository,
        fakeUuidProvider
    )
    private val createHabit = CreateHabit(
        fakeHabitRepository,
        generateInstanceForHabit,
        fakeUuidProvider
    )
    private val applyStrictnessPreset = ApplyStrictnessPreset(fakeUserRepository)
    private val generateDailyHabits = GenerateDailyHabits(
        fakeUserRepository,
        fakeHabitRepository,
        generateInstanceForHabit
    )

    private fun buildViewModel(): OnboardingViewModel = OnboardingViewModel(
        userRepository = fakeUserRepository,
        applyStrictnessPreset = applyStrictnessPreset,
        createHabit = createHabit,
        generateDailyHabits = generateDailyHabits
    )

    @Test
    fun `given DAILY scheduleKind when creating habit then schedule type is DAILY`() = runTest {
        // Given — default scheduleKind is DAILY
        val viewModel = buildViewModel()
        viewModel.updateHabitName("Run")
        viewModel.updateScheduleKind(OnboardingScheduleKind.DAILY)

        // When
        viewModel.createFirstHabit()

        // Then
        assertEquals(ScheduleType.DAILY, fakeHabitRepository.capturedSchedule?.scheduleType)
    }

    @Test
    fun `given WEEKLY scheduleKind when creating habit then schedule type is FLEXIBLE_WEEKLY`() =
        runTest {
            // Given
            val viewModel = buildViewModel()
            viewModel.updateHabitName("Rest")
            viewModel.updateScheduleKind(OnboardingScheduleKind.WEEKLY)

            // When
            viewModel.createFirstHabit()

            // Then
            assertEquals(
                ScheduleType.FLEXIBLE_WEEKLY,
                fakeHabitRepository.capturedSchedule?.scheduleType
            )
        }
}

private class FakeHabitRepository : HabitRepository {
    var capturedSchedule: HabitSchedule? = null

    override suspend fun createHabit(
        habit: Habit,
        schedule: HabitSchedule,
        reminder: HabitReminder?
    ) {
        capturedSchedule = schedule
    }

    override fun observeActiveHabits(): Flow<List<Habit>> = flowOf(emptyList())

    override fun observeArchivedHabits(): Flow<List<Habit>> = flowOf(emptyList())

    override suspend fun getActiveHabits(): List<Habit> = emptyList()

    override suspend fun getHabitById(habitId: String): Habit? = null

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

    override suspend fun getHabitsWithTrackingEnabled(): List<Habit> = emptyList()
}

private class FakeUserRepository : UserRepository {
    private val user = User(
        id = "user-1",
        timezone = TimeZone.UTC,
        previousTimezone = null,
        undoPolicy = UndoPolicy.TODAY_ONLY,
        maxSnoozeDurationMinutes = 30,
        maxSnoozesPerHabitPerDay = 3,
        maxConsecutiveSkips = 2,
        isOnboardingCompleted = false,
        dailySummaryTime = null,
        createdAt = Clock.System.now()
    )

    override fun observeUser(): Flow<User?> = flowOf(user)

    override suspend fun getUser(): User = user

    override suspend fun createDefaultUser(timezone: TimeZone): User = user

    override suspend fun updateUser(user: User) = Unit

    override suspend fun updateTimezone(
        userId: String,
        newTimezone: TimeZone,
        previousTimezone: TimeZone
    ) = Unit

    override suspend fun setOnboardingCompleted(userId: String, isCompleted: Boolean) = Unit
}

private class FakeHabitInstanceRepository : HabitInstanceRepository {
    override fun observeInstancesForDate(date: LocalDate): Flow<List<HabitInstance>> =
        flowOf(emptyList())

    override suspend fun getInstancesForDate(date: LocalDate): List<HabitInstance> = emptyList()

    override suspend fun getPendingInstancesForDate(date: LocalDate): List<HabitInstance> =
        emptyList()

    override suspend fun getInstanceById(instanceId: String): HabitInstance? = null

    override suspend fun getInstanceForHabitAndDate(
        habitId: String,
        date: LocalDate
    ): HabitInstance? = null

    override suspend fun getInstancesForHabit(habitId: String): List<HabitInstance> = emptyList()

    override suspend fun getInstancesInDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitInstance> = emptyList()

    override fun observeInstancesInDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitInstance>> = flowOf(emptyList())

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

private class FakeLeavePeriodRepository : LeavePeriodRepository {
    override suspend fun createLeavePeriod(leavePeriod: LeavePeriod) = Unit

    override suspend fun getLeavePeriodById(id: String): LeavePeriod? = null

    override suspend fun getLeavePeriodsByHabit(habitId: String): List<LeavePeriod> = emptyList()

    override suspend fun getActiveLeavePeriod(habitId: String, date: LocalDate): LeavePeriod? = null

    override suspend fun getAllActiveLeavePeriods(date: LocalDate): List<LeavePeriod> = emptyList()

    override suspend fun updateLeavePeriod(leavePeriod: LeavePeriod) = Unit

    override suspend fun endLeavePeriod(id: String, endDate: LocalDate) = Unit

    override suspend fun deleteLeavePeriod(id: String) = Unit

    override suspend fun deleteLeavePeriodsForHabit(habitId: String) = Unit
}

private class FakeUuidProvider : UuidProvider {
    private var counter = 0

    override fun generate(): String = "uuid-${++counter}"
}
