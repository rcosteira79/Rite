package com.ricardocosteira.rite.presentation.ui.today.habitcard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
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
class HabitCardKickerRowScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun kicker_only_pending() = render(
        state = HabitCardState.Pending,
        kicker = "DAILY · 12 MIN",
        streakDays = null
    )

    @Test
    fun kicker_with_streak_pending() = render(
        state = HabitCardState.Pending,
        kicker = "DAILY · 30 PAGES",
        streakDays = 14
    )

    @Test
    fun kicker_with_streak_completed() = render(
        state = HabitCardState.Completed,
        kicker = "DAILY · 12 MIN",
        streakDays = 15
    )

    @Test
    fun kicker_skipped_hides_streak() = render(
        state = HabitCardState.Skipped,
        kicker = "DAILY · 12 MIN",
        streakDays = 14
    )

    @Test
    fun kicker_suspended_hides_streak() = render(
        state = HabitCardState.Suspended,
        kicker = "PAUSED UNTIL APR 22",
        streakDays = 14
    )

    @Test
    fun kicker_failed_shows_streak_zero() = render(
        state = HabitCardState.Failed,
        kicker = "YESTERDAY · MISSED",
        streakDays = 0
    )

    private fun render(state: HabitCardState, kicker: String, streakDays: Int?) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                HabitCardKickerRow(
                    state = state,
                    kicker = kicker,
                    streakDays = streakDays,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
