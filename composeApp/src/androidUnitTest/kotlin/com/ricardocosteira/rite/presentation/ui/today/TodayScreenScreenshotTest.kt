package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.motivational_title_0
import rite.composeapp.generated.resources.motivational_title_1
import rite.composeapp.generated.resources.motivational_title_2
import rite.composeapp.generated.resources.motivational_title_7
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
            RiteThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = emptyState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = emptyState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = TodayState(isLoading = true),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = TodayState(isLoading = true),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = dailyPendingState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = dailyPendingState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = mixedState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = mixedState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = allDoneState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = allDoneState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = false) {
                TodayScreen(
                    state = quantitativeInProgressState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
            RiteThemeFallback(darkTheme = true) {
                TodayScreen(
                    state = quantitativeInProgressState(),
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoLastIncrement = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onEdit = {},
                    onDelete = {},
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
        habits = persistentListOf(),
        motivationalTitleRes = Res.string.motivational_title_0,
        strictnessPreset = StrictnessPreset.BALANCED,
        pendingCount = 0,
        dailyProgressDisplay = 0,
        dailyTotal = 0
    )

    private fun dailyPendingState(): TodayState {
        val meditation = buildHabit(
            instanceId = "1",
            name = "Morning Meditation",
            description = "10 minutes of mindfulness",
            type = HabitType.BINARY,
            status = HabitStatus.PENDING,
            cadence = ScheduleType.DAILY
        )
        val reading = buildHabit(
            instanceId = "2",
            name = "Read 30 Pages",
            type = HabitType.QUANTITATIVE,
            status = HabitStatus.PENDING,
            completedValue = 0,
            targetValue = 30,
            unit = "pages",
            defaultIncrement = 1,
            cadence = ScheduleType.DAILY
        )
        val walk = buildHabit(
            instanceId = "3",
            name = "Evening Walk",
            description = "06:00 PM — Neighborhood",
            type = HabitType.BINARY,
            status = HabitStatus.PENDING,
            cadence = ScheduleType.DAILY
        )
        val allHabits = persistentListOf(meditation, reading, walk)

        return TodayState(
            isLoading = false,
            habits = allHabits,
            pendingDaily = allHabits,
            motivationalTitleRes = Res.string.motivational_title_1,
            strictnessPreset = StrictnessPreset.BALANCED,
            pendingCount = 3,
            dailyProgressDisplay = 0,
            dailyTotal = 3
        )
    }

    private fun mixedState(): TodayState {
        val readingPending = buildHabit(
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
        )
        val walkPending = buildHabit(
            instanceId = "2",
            name = "Evening Walk",
            description = "06:00 PM — Neighborhood",
            type = HabitType.BINARY,
            status = HabitStatus.PENDING,
            cadence = ScheduleType.DAILY
        )
        val meditationCompleted = buildHabit(
            instanceId = "3",
            name = "Morning Meditation",
            type = HabitType.BINARY,
            status = HabitStatus.COMPLETED,
            completedAtText = "7:45 AM",
            cadence = ScheduleType.DAILY
        )
        val deepWorkPending = buildHabit(
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
        )
        val gymCompleted = buildHabit(
            instanceId = "5",
            name = "Gym Session",
            type = HabitType.BINARY,
            status = HabitStatus.COMPLETED,
            completedAtText = "6:30 PM",
            cadence = ScheduleType.WEEKLY
        )

        return TodayState(
            isLoading = false,
            habits = persistentListOf(
                readingPending,
                walkPending,
                meditationCompleted,
                deepWorkPending,
                gymCompleted
            ),
            pendingDaily = persistentListOf(readingPending, walkPending),
            resolvedDaily = persistentListOf(meditationCompleted),
            pendingWeekly = persistentListOf(deepWorkPending),
            resolvedWeekly = persistentListOf(gymCompleted),
            motivationalTitleRes = Res.string.motivational_title_0,
            strictnessPreset = StrictnessPreset.BALANCED,
            pendingCount = 2,
            dailyProgressDisplay = 1,
            dailyTotal = 3
        )
    }

    private fun allDoneState(): TodayState {
        val meditationCompleted = buildHabit(
            instanceId = "1",
            name = "Morning Meditation",
            type = HabitType.BINARY,
            status = HabitStatus.COMPLETED,
            completedAtText = "7:45 AM",
            cadence = ScheduleType.DAILY
        )
        val readingCompleted = buildHabit(
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
        )
        val walkSkipped = buildHabit(
            instanceId = "3",
            name = "Evening Walk",
            type = HabitType.BINARY,
            status = HabitStatus.SKIPPED,
            completedAtText = "10:00 PM",
            cadence = ScheduleType.DAILY
        )
        val allHabits = persistentListOf(meditationCompleted, readingCompleted, walkSkipped)

        return TodayState(
            isLoading = false,
            habits = allHabits,
            resolvedDaily = allHabits,
            motivationalTitleRes = Res.string.motivational_title_2,
            strictnessPreset = StrictnessPreset.BALANCED,
            pendingCount = 0,
            dailyProgressDisplay = 3,
            dailyTotal = 3
        )
    }

    private fun quantitativeInProgressState(): TodayState {
        val hydrate = buildHabit(
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
        )
        val reading = buildHabit(
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
        val allHabits = persistentListOf(hydrate, reading)

        return TodayState(
            isLoading = false,
            habits = allHabits,
            pendingDaily = allHabits,
            motivationalTitleRes = Res.string.motivational_title_7,
            strictnessPreset = StrictnessPreset.FLEXIBLE,
            pendingCount = 2,
            dailyProgressDisplay = 0,
            dailyTotal = 2
        )
    }

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
