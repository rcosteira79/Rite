package com.ricardocosteira.habitlock.presentation.ui.onboarding

import com.ricardocosteira.habitlock.domain.models.Habit
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.domain.models.UndoPolicy
import com.ricardocosteira.habitlock.domain.models.User
import com.ricardocosteira.habitlock.domain.repositories.UserRepository
import com.ricardocosteira.habitlock.domain.usecases.ApplyStrictnessPreset
import com.ricardocosteira.habitlock.domain.usecases.CreateHabit
import com.ricardocosteira.habitlock.domain.usecases.GenerateDailyHabits
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone

class OnboardingViewModelScheduleTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockApplyStrictnessPreset = mockk<ApplyStrictnessPreset>(relaxed = true)
    private val mockCreateHabit = mockk<CreateHabit>()
    private val mockGenerateDailyHabits = mockk<GenerateDailyHabits>(relaxed = true)

    private val mockUser = User(
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

    private val mockHabit = mockk<Habit>(relaxed = true)

    private fun buildViewModel(): OnboardingViewModel = OnboardingViewModel(
        userRepository = mockUserRepository,
        applyStrictnessPreset = mockApplyStrictnessPreset,
        createHabit = mockCreateHabit,
        generateDailyHabits = mockGenerateDailyHabits
    )

    private fun givenCreateHabitSucceeds() {
        coEvery { mockUserRepository.getUser() } returns mockUser
        coEvery { mockCreateHabit.execute(any(), any()) } returns Result.success(mockHabit)
    }

    @Test
    fun `given every day schedule when creating habit then specificDays is null`() = runTest {
        // Given
        givenCreateHabitSucceeds()
        val viewModel = buildViewModel()
        viewModel.updateHabitName("Run")
        viewModel.updateScheduleOption(ScheduleOption.EVERY_DAY)

        // When
        viewModel.createFirstHabit()

        // Then
        val actualParamsSlot = slot<CreateHabit.CreateHabitParams>()
        coVerify { mockCreateHabit.execute(capture(actualParamsSlot), any()) }
        assertNull(actualParamsSlot.captured.specificDays)
    }

    @Test
    fun `given weekdays schedule when creating habit then specificDays is mon to fri`() = runTest {
        // Given
        givenCreateHabitSucceeds()
        val viewModel = buildViewModel()
        viewModel.updateHabitName("Run")
        viewModel.updateScheduleOption(ScheduleOption.WEEKDAYS)

        // When
        viewModel.createFirstHabit()

        // Then
        val actualParamsSlot = slot<CreateHabit.CreateHabitParams>()
        coVerify { mockCreateHabit.execute(capture(actualParamsSlot), any()) }
        val expectedDays = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
        assertEquals(expectedDays, actualParamsSlot.captured.specificDays)
    }

    @Test
    fun `given weekends schedule when creating habit then specificDays is sat and sun`() = runTest {
        // Given
        givenCreateHabitSucceeds()
        val viewModel = buildViewModel()
        viewModel.updateHabitName("Rest")
        viewModel.updateScheduleOption(ScheduleOption.WEEKENDS)

        // When
        viewModel.createFirstHabit()

        // Then
        val actualParamsSlot = slot<CreateHabit.CreateHabitParams>()
        coVerify { mockCreateHabit.execute(capture(actualParamsSlot), any()) }
        val expectedDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        assertEquals(expectedDays, actualParamsSlot.captured.specificDays)
    }

    @Test
    fun `given custom schedule when creating habit then specificDays matches selected days`() = runTest {
        // Given
        givenCreateHabitSucceeds()
        val viewModel = buildViewModel()
        viewModel.updateHabitName("Yoga")
        viewModel.updateScheduleOption(ScheduleOption.CUSTOM)
        val inputCustomDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        viewModel.updateCustomDays(inputCustomDays)

        // When
        viewModel.createFirstHabit()

        // Then
        val actualParamsSlot = slot<CreateHabit.CreateHabitParams>()
        coVerify { mockCreateHabit.execute(capture(actualParamsSlot), any()) }
        assertEquals(inputCustomDays, actualParamsSlot.captured.specificDays)
    }
}
