package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.Habit
import com.ricardocosteira.rite.domain.models.HabitInstance
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
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
class HabitDetailScreenScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val binaryHabit = Habit(
        id = "h1",
        name = "Meditate",
        description = "Daily meditation",
        type = HabitType.BINARY,
        targetValue = null,
        unit = null,
        defaultIncrement = 1,
        isTrackingEnabled = false,
        isActive = true,
        isArchived = false,
        currentStreak = 14,
        longestStreak = 42,
        totalCompletions = 80,
        expectedCompletions = 90,
        createdAt = Clock.System.now(),
        archivedAt = null
    )

    private val quantitativeHabit = binaryHabit.copy(
        id = "h2",
        name = "Read",
        description = "Read 30 pages daily",
        type = HabitType.QUANTITATIVE,
        targetValue = 30,
        unit = "pages"
    )

    private val pendingBinaryInstance = HabitInstance(
        id = "i1",
        habitId = "h1",
        date = LocalDate(2026, 4, 5),
        status = HabitStatus.PENDING,
        completedValue = null,
        targetValue = null,
        consecutiveSkipsAtCreation = 0,
        createdAt = Clock.System.now(),
        completedAt = null
    )

    private val inProgressQuantInstance = pendingBinaryInstance.copy(
        id = "i2",
        habitId = "h2",
        completedValue = 22,
        targetValue = 30
    )

    private val sampleHeatmap: List<HeatmapDay> = (0..89).map { daysAgo ->
        val date = LocalDate(2026, 4, 5).minus(DatePeriod(days = daysAgo))
        HeatmapDay(
            date = date,
            completionPercentage = if (daysAgo % 3 ==
                0
            ) {
                1.0f
            } else if (daysAgo % 2 == 0) {
                0.5f
            } else {
                0f
            },
            status = if (daysAgo % 3 == 0) HabitStatus.COMPLETED else HabitStatus.PENDING
        )
    }

    @Test
    fun habitDetail_binaryPending_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                HabitDetailScreen(
                    state = HabitDetailState(
                        habit = binaryHabit,
                        instance = pendingBinaryInstance,
                        maxConsecutiveSkips = 2,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoIncrement = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitDetail_binaryPending_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                HabitDetailScreen(
                    state = HabitDetailState(
                        habit = binaryHabit,
                        instance = pendingBinaryInstance,
                        maxConsecutiveSkips = 2,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoIncrement = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitDetail_quantitativeInProgress_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                HabitDetailScreen(
                    state = HabitDetailState(
                        habit = quantitativeHabit,
                        instance = inProgressQuantInstance,
                        maxConsecutiveSkips = 2,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoIncrement = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitDetail_quantitativeInProgress_lightTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                HabitDetailScreen(
                    state = HabitDetailState(
                        habit = quantitativeHabit,
                        instance = inProgressQuantInstance,
                        maxConsecutiveSkips = 2,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoIncrement = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
