package com.ricardocosteira.rite.presentation.ui.today.components.habitcard

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.HabitType
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
class HabitCardActionScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun pending_binary() = render(HabitCardState.Pending, HabitType.BINARY)

    @Test fun pending_binary_skip_locked() = render(
        HabitCardState.Pending,
        HabitType.BINARY,
        skipLocked = true
    )

    @Test fun pending_quantitative() = render(
        HabitCardState.Pending,
        HabitType.QUANTITATIVE,
        increment = 5
    )

    @Test fun pending_in_progress_quantitative() = render(
        HabitCardState.PendingInProgress,
        HabitType.QUANTITATIVE,
        increment = 5
    )

    @Test fun completed() = render(HabitCardState.Completed, HabitType.BINARY)

    @Test fun skipped() = render(HabitCardState.Skipped, HabitType.BINARY)

    @Test fun failed() = render(HabitCardState.Failed, HabitType.BINARY)

    @Test fun suspended() = render(HabitCardState.Suspended, HabitType.QUANTITATIVE)

    private fun render(
        state: HabitCardState,
        type: HabitType,
        increment: Int = 1,
        skipLocked: Boolean = false
    ) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                HabitCardAction(
                    state = state,
                    type = type,
                    defaultIncrement = increment,
                    skipLocked = skipLocked,
                    onComplete = {},
                    onIncrement = {},
                    onSkip = {},
                    onUndo = {},
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
