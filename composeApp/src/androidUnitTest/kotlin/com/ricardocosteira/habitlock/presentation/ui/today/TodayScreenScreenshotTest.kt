package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import com.ricardocosteira.habitlock.domain.models.StrictnessPreset
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel
import com.ricardocosteira.habitlock.presentation.ui.theme.HabitLockThemeFallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [33],
    qualifiers = "w360dp-h800dp-420dpi",
    application = android.app.Application::class
)
class TodayScreenScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    // --- Empty state ---

    @Test
    fun todayScreen_empty_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = emptyState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun todayScreen_empty_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = emptyState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Loading ---

    @Test
    fun todayScreen_loading_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = TodayState(isLoading = true),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun todayScreen_loading_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = TodayState(isLoading = true),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Daily habits only (all pending, collapsed) ---

    @Test
    fun todayScreen_dailyPending_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = dailyPendingState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun todayScreen_dailyPending_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = dailyPendingState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Mixed state: pending + resolved daily, weekly section ---

    @Test
    fun todayScreen_mixedState_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = mixedState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun todayScreen_mixedState_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = mixedState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- All done state ---

    @Test
    fun todayScreen_allDone_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = allDoneState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun todayScreen_allDone_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = allDoneState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Quantitative in progress ---

    @Test
    fun todayScreen_quantitativeInProgress_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = quantitativeInProgressState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun todayScreen_quantitativeInProgress_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = quantitativeInProgressState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onDismissTimezoneWarning = {},
                    onAddFirstHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Test data factories ---

    private fun emptyState(): TodayState = TodayState(
        isLoading = false,
        habits = emptyList(),
        motivationalTitle = "Quiet discipline",
        strictnessPreset = StrictnessPreset.BALANCED,
        pendingCount = 0,
        dailyProgressDisplay = 0,
        dailyTotal = 0
    )

    private fun dailyPendingState(): TodayState = TodayState(
        isLoading = false,
        habits =
            listOf(
                buildHabit(
                    instanceId = "1",
                    name = "Morning Meditation",
                    description = "10 minutes of mindfulness",
                    type = HabitType.BINARY,
                    status = HabitStatus.PENDING,
                    cadence = ScheduleType.DAILY
                ),
                buildHabit(
                    instanceId = "2",
                    name = "Read 30 Pages",
                    type = HabitType.QUANTITATIVE,
                    status = HabitStatus.PENDING,
                    completedValue = 0,
                    targetValue = 30,
                    unit = "pages",
                    defaultIncrement = 1,
                    cadence = ScheduleType.DAILY
                ),
                buildHabit(
                    instanceId = "3",
                    name = "Evening Walk",
                    description = "06:00 PM — Neighborhood",
                    type = HabitType.BINARY,
                    status = HabitStatus.PENDING,
                    cadence = ScheduleType.DAILY
                )
            ),
        motivationalTitle = "Small steps, big change",
        strictnessPreset = StrictnessPreset.BALANCED,
        pendingCount = 3,
        dailyProgressDisplay = 0,
        dailyTotal = 3
    )

    private fun mixedState(): TodayState {
        val habits: List<TodayHabitUiModel> = listOf(
            buildHabit(
                instanceId = "1",
                name = "Read 30 Pages",
                type = HabitType.QUANTITATIVE,
                status = HabitStatus.PENDING,
                completedValue = 10,
                targetValue = 30,
                unit = "pages",
                defaultIncrement = 1,
                progressPercentage = 0.33f,
                cadence = ScheduleType.DAILY
            ),
            buildHabit(
                instanceId = "2",
                name = "Evening Walk",
                description = "06:00 PM — Neighborhood",
                type = HabitType.BINARY,
                status = HabitStatus.PENDING,
                cadence = ScheduleType.DAILY
            ),
            buildHabit(
                instanceId = "3",
                name = "Morning Meditation",
                type = HabitType.BINARY,
                status = HabitStatus.COMPLETED,
                completedAtText = "7:45 AM",
                cadence = ScheduleType.DAILY
            ),
            buildHabit(
                instanceId = "4",
                name = "Deep Work Blocks",
                type = HabitType.QUANTITATIVE,
                status = HabitStatus.PENDING,
                completedValue = 4,
                targetValue = 5,
                unit = "blocks",
                defaultIncrement = 1,
                progressPercentage = 0.8f,
                cadence = ScheduleType.WEEKLY
            ),
            buildHabit(
                instanceId = "5",
                name = "Gym Session",
                type = HabitType.BINARY,
                status = HabitStatus.COMPLETED,
                completedAtText = "6:30 PM",
                cadence = ScheduleType.WEEKLY
            )
        )

        return TodayState(
            isLoading = false,
            habits = habits,
            motivationalTitle = "Quiet discipline",
            strictnessPreset = StrictnessPreset.BALANCED,
            pendingCount = 2,
            dailyProgressDisplay = 1,
            dailyTotal = 3
        )
    }

    private fun allDoneState(): TodayState = TodayState(
        isLoading = false,
        habits =
            listOf(
                buildHabit(
                    instanceId = "1",
                    name = "Morning Meditation",
                    type = HabitType.BINARY,
                    status = HabitStatus.COMPLETED,
                    completedAtText = "7:45 AM",
                    cadence = ScheduleType.DAILY
                ),
                buildHabit(
                    instanceId = "2",
                    name = "Read 30 Pages",
                    type = HabitType.QUANTITATIVE,
                    status = HabitStatus.COMPLETED,
                    completedValue = 30,
                    targetValue = 30,
                    unit = "pages",
                    defaultIncrement = 1,
                    progressPercentage = 1f,
                    completedAtText = "9:15 PM",
                    cadence = ScheduleType.DAILY
                ),
                buildHabit(
                    instanceId = "3",
                    name = "Evening Walk",
                    type = HabitType.BINARY,
                    status = HabitStatus.SKIPPED,
                    completedAtText = "10:00 PM",
                    cadence = ScheduleType.DAILY
                )
            ),
        motivationalTitle = "Trust the process",
        strictnessPreset = StrictnessPreset.BALANCED,
        pendingCount = 0,
        dailyProgressDisplay = 3,
        dailyTotal = 3
    )

    private fun quantitativeInProgressState(): TodayState = TodayState(
        isLoading = false,
        habits =
            listOf(
                buildHabit(
                    instanceId = "1",
                    name = "Hydrate",
                    type = HabitType.QUANTITATIVE,
                    status = HabitStatus.PENDING,
                    completedValue = 2000,
                    targetValue = 2500,
                    unit = "ml",
                    defaultIncrement = 250,
                    progressPercentage = 0.8f,
                    cadence = ScheduleType.DAILY
                ),
                buildHabit(
                    instanceId = "2",
                    name = "Read 30 Pages",
                    type = HabitType.QUANTITATIVE,
                    status = HabitStatus.PENDING,
                    completedValue = 10,
                    targetValue = 30,
                    unit = "pages",
                    defaultIncrement = 1,
                    progressPercentage = 0.33f,
                    cadence = ScheduleType.DAILY
                )
            ),
        motivationalTitle = "Consistency compounds",
        strictnessPreset = StrictnessPreset.FLEXIBLE,
        pendingCount = 2,
        dailyProgressDisplay = 0,
        dailyTotal = 2
    )

    private fun buildHabit(
        instanceId: String,
        name: String,
        type: HabitType,
        status: HabitStatus,
        cadence: ScheduleType,
        description: String? = null,
        completedValue: Int? = null,
        targetValue: Int? = null,
        unit: String? = null,
        defaultIncrement: Int = 1,
        progressPercentage: Float = 0f,
        completedAtText: String? = null
    ): TodayHabitUiModel = TodayHabitUiModel(
        instanceId = instanceId,
        habitId = "habit-$instanceId",
        name = name,
        description = description,
        type = type,
        status = status,
        completedValue = completedValue,
        targetValue = targetValue,
        unit = unit,
        defaultIncrement = defaultIncrement,
        progressPercentage = progressPercentage,
        isSkipLocked = false,
        currentStreak = 5,
        longestStreak = 12,
        scorePercentage = 85,
        cadence = cadence,
        completedAtText = completedAtText
    )
}
