package com.ricardocosteira.rite.presentation.ui.today.habitcard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
class MarginRuleScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun pending_empty() = render(HabitCardState.Pending, 0f)

    @Test fun pending_in_progress_40pct() = render(HabitCardState.PendingInProgress, 0.4f)

    @Test fun completed_full() = render(HabitCardState.Completed, 1f)

    @Test fun failed_full() = render(HabitCardState.Failed, 1f)

    @Test fun skipped_dashed() = render(HabitCardState.Skipped, 0f)

    @Test fun suspended_dashed() = render(HabitCardState.Suspended, 0f)

    private fun render(state: HabitCardState, fill: Float) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(72.dp)
                        .padding(8.dp)
                ) {
                    MarginRule(state = state, fillFraction = fill)
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
