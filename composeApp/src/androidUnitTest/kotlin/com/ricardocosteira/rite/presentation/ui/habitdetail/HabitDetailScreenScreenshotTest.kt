package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.UndoPolicy
import com.ricardocosteira.rite.presentation.ui.habitdetail.models.HabitDetailUiModel
import com.ricardocosteira.rite.presentation.ui.habitdetail.models.HeatmapDay
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import kotlinx.collections.immutable.ImmutableList
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

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private val binaryPending = HabitDetailUiModel(
        habitId = "h1",
        instanceId = "i1",
        name = "Morning Meditation",
        description = "Clear the mind before the day begins.",
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
        currentConsecutiveSkips = 0,
        strictnessPreset = StrictnessPreset.BALANCED,
        undoPolicy = UndoPolicy.TODAY_ONLY,
        snoozesUsedToday = 1,
        maxSnoozesPerDay = 3,
        skipsThisWeek = 2
    )

    private val binaryCompleted = binaryPending.copy(
        status = HabitStatus.COMPLETED,
        progressPercentage = 1f,
        completedValue = 1
    )

    private val binaryFailed = binaryPending.copy(
        status = HabitStatus.FAILED,
        progressPercentage = 0f
    )

    private val quantInProgress = HabitDetailUiModel(
        habitId = "h2",
        instanceId = "i2",
        name = "Drink Water",
        description = "Stay hydrated throughout the day.",
        type = HabitType.QUANTITATIVE,
        unit = "ml",
        defaultIncrement = 250,
        status = HabitStatus.PENDING,
        currentProgress = 1500,
        targetValue = 2000,
        completedValue = null,
        progressPercentage = 1500f / 2000f,
        isQuantitativeComplete = false,
        currentStreak = 7,
        longestStreak = 30,
        habitScore = 72,
        maxConsecutiveSkips = 2,
        currentConsecutiveSkips = 0,
        strictnessPreset = StrictnessPreset.BALANCED,
        undoPolicy = UndoPolicy.TODAY_ONLY,
        snoozesUsedToday = 0,
        maxSnoozesPerDay = 3,
        skipsThisWeek = 1
    )

    private val quantGoalReached = quantInProgress.copy(
        currentProgress = 2000,
        completedValue = 2000,
        progressPercentage = 1f,
        isQuantitativeComplete = true,
        status = HabitStatus.PENDING
    )

    private val quantSkipLocked = quantInProgress.copy(
        currentConsecutiveSkips = 2,
        maxConsecutiveSkips = 2,
        strictnessPreset = StrictnessPreset.BALANCED,
        status = HabitStatus.PENDING
    )

    // ─── Sample heatmap (90 days, deterministic pattern) ─────────────────────

    private val sampleHeatmap: ImmutableList<HeatmapDay> = buildSampleHeatmap()

    // ─── Helper ──────────────────────────────────────────────────────────────

    private fun render(habit: HabitDetailUiModel, darkTheme: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = darkTheme) {
                HabitDetailScreen(
                    state = HabitDetailState(
                        habit = habit,
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

    // ─── Tests ───────────────────────────────────────────────────────────────

    @Test fun binary_pending_light() = render(binaryPending, darkTheme = false)

    @Test fun binary_pending_dark() = render(binaryPending, darkTheme = true)

    @Test fun binary_completed_light() = render(binaryCompleted, darkTheme = false)

    @Test fun binary_completed_dark() = render(binaryCompleted, darkTheme = true)

    @Test fun binary_failed_light() = render(binaryFailed, darkTheme = false)

    @Test fun quant_in_progress_light() = render(quantInProgress, darkTheme = false)

    @Test fun quant_in_progress_dark() = render(quantInProgress, darkTheme = true)

    @Test fun quant_goal_reached_light() = render(quantGoalReached, darkTheme = false)

    @Test fun quant_goal_reached_dark() = render(quantGoalReached, darkTheme = true)

    @Test fun quant_skip_locked_light() = render(quantSkipLocked, darkTheme = false)

    @Test fun quant_skip_locked_dark() = render(quantSkipLocked, darkTheme = true)
}

private fun buildSampleHeatmap(): ImmutableList<HeatmapDay> {
    val anchor = LocalDate(2026, 4, 19)
    return (0..89).map { daysAgo ->
        val date = anchor.minus(DatePeriod(days = daysAgo))
        val bucket = daysAgo % 5
        val (pct, status) = when (bucket) {
            0 -> 1.0f to HabitStatus.COMPLETED
            1 -> 0.75f to HabitStatus.PENDING
            2 -> 0.5f to HabitStatus.PENDING
            3 -> 0.0f to HabitStatus.SKIPPED
            else -> 0.0f to HabitStatus.FAILED
        }
        HeatmapDay(date = date.toString(), completionPercentage = pct, status = status)
    }.toImmutableList()
}
