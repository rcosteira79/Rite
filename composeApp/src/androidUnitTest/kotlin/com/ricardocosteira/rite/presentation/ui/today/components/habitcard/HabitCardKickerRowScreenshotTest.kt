package com.ricardocosteira.rite.presentation.ui.today.components.habitcard

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

    @Test fun pending() = render(HabitCardState.Pending, "DAILY · 12 MIN")

    @Test fun completed() = render(HabitCardState.Completed, "DAILY · 12 MIN")

    @Test fun skipped() = render(HabitCardState.Skipped, "DAILY · 12 MIN")

    @Test fun failed() = render(HabitCardState.Failed, "YESTERDAY · MISSED")

    @Test fun suspended() = render(HabitCardState.Suspended, "PAUSED UNTIL APR 22")

    private fun render(state: HabitCardState, kicker: String) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                HabitCardKickerRow(
                    state = state,
                    kicker = kicker,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
