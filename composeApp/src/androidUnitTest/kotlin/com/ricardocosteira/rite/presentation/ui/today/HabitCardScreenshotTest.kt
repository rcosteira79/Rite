package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.presentation.models.TodayHabitUiModel
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
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
class HabitCardScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Binary × 5 statuses
    @Test fun binary_pending() = render(binaryDaily(HabitStatus.PENDING))

    @Test fun binary_pending_skip_locked() = render(
        binaryDaily(HabitStatus.PENDING, skipLocked = true)
    )

    @Test fun binary_completed() = render(
        binaryDaily(HabitStatus.COMPLETED, completedAtText = "07:14 AM", streak = 15)
    )

    @Test fun binary_skipped() = render(
        binaryDaily(HabitStatus.SKIPPED, completedAtText = "09:00 AM")
    )

    @Test fun binary_failed() = render(binaryDaily(HabitStatus.FAILED, streak = 0))

    @Test fun binary_suspended() = render(binaryDaily(HabitStatus.SUSPENDED))

    // Quantitative × 5 statuses
    @Test fun quant_pending_fresh() = render(quantDaily(HabitStatus.PENDING, cur = 0))

    @Test fun quant_pending_in_progress() = render(quantDaily(HabitStatus.PENDING, cur = 12))

    @Test fun quant_completed() = render(
        quantDaily(HabitStatus.COMPLETED, cur = 30, streak = 10, completedAtText = "08:45 PM")
    )

    @Test fun quant_skipped() = render(
        quantDaily(HabitStatus.SKIPPED, cur = 0, completedAtText = "06:30 AM")
    )

    @Test fun quant_failed() = render(quantDaily(HabitStatus.FAILED, cur = 6, streak = 0))

    @Test fun quant_suspended() = render(quantDaily(HabitStatus.SUSPENDED, cur = 0))

    // Flexible-weekly variants
    @Test fun flex_weekly_binary_pending() = render(flexibleWeeklyBinary(HabitStatus.PENDING))

    @Test fun flex_weekly_binary_completed() = render(
        flexibleWeeklyBinary(HabitStatus.COMPLETED, completedAtText = "10:15 AM", streak = 3)
    )

    @Test fun flex_weekly_quant_pending() = render(flexibleWeeklyQuant(HabitStatus.PENDING))

    // Dark-mode sanity for the most visually distinct states
    @Test fun dark_binary_pending() = render(binaryDaily(HabitStatus.PENDING), dark = true)

    @Test fun dark_quant_pending_in_progress() = render(
        quantDaily(HabitStatus.PENDING, cur = 12),
        dark = true
    )

    @Test fun dark_quant_completed() = render(
        quantDaily(HabitStatus.COMPLETED, cur = 30, completedAtText = "08:45 PM"),
        dark = true
    )

    private fun render(habit: TodayHabitUiModel, dark: Boolean = false) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                HabitCard(
                    habit = habit,
                    onClick = {},
                    onComplete = {},
                    onSkip = {},
                    onUndo = {},
                    onIncrementProgress = {},
                    onCustomProgress = {},
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
