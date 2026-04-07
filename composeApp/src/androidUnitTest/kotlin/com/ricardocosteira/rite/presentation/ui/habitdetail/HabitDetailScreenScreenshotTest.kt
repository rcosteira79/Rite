package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import kotlinx.collections.immutable.toImmutableList
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

    private val binaryHabitUi = HabitDetailUiModel(
        habitId = "h1",
        instanceId = "i1",
        name = "Meditate",
        description = "Daily meditation",
        type = HabitType.BINARY,
        unit = null,
        defaultIncrement = 1,
        status = HabitStatus.PENDING,
        currentProgress = 0,
        targetValue = null,
        completedValue = null,
        progressPercentage = 0f,
        isQuantitativeComplete = false,
        currentStreak = 14,
        longestStreak = 42,
        habitScore = 89,
        maxConsecutiveSkips = 2,
        currentConsecutiveSkips = 0
    )

    private val quantitativeHabitUi = binaryHabitUi.copy(
        habitId = "h2",
        instanceId = "i2",
        name = "Read",
        description = "Read 30 pages daily",
        type = HabitType.QUANTITATIVE,
        unit = "pages",
        currentProgress = 22,
        targetValue = 30,
        completedValue = 22,
        progressPercentage = 22f / 30f
    )

    private val sampleHeatmap = (0..89).map { daysAgo ->
        val date = LocalDate(2026, 4, 5).minus(DatePeriod(days = daysAgo))
        HeatmapDay(
            date = date.toString(),
            completionPercentage = when {
                daysAgo % 3 == 0 -> 1.0f
                daysAgo % 2 == 0 -> 0.5f
                else -> 0f
            },
            status = if (daysAgo % 3 == 0) HabitStatus.COMPLETED else HabitStatus.PENDING
        )
    }.toImmutableList()

    @Test
    fun habitDetail_binaryPending_darkTheme() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                HabitDetailScreen(
                    state = HabitDetailState(
                        habit = binaryHabitUi,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoIncrement = {},
                    onEditHabit = {},
                    onArchiveHabit = {},
                    onDeleteHabit = {}
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
                        habit = binaryHabitUi,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoIncrement = {},
                    onEditHabit = {},
                    onArchiveHabit = {},
                    onDeleteHabit = {}
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
                        habit = quantitativeHabitUi,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoIncrement = {},
                    onEditHabit = {},
                    onArchiveHabit = {},
                    onDeleteHabit = {}
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
                        habit = quantitativeHabitUi,
                        heatmapData = sampleHeatmap,
                        isLoading = false
                    ),
                    onBackClick = {},
                    onComplete = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    onSkip = {},
                    onUndo = {},
                    onUndoIncrement = {},
                    onEditHabit = {},
                    onArchiveHabit = {},
                    onDeleteHabit = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
