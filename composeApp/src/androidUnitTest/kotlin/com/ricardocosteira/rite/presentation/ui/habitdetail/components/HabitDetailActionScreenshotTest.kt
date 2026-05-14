package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.HabitStatus
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
class HabitDetailActionScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun binary_pending() = render(HabitType.BINARY, HabitStatus.PENDING)

    @Test fun binary_pending_skip_locked() =
        render(HabitType.BINARY, HabitStatus.PENDING, skipLocked = true)

    @Test fun binary_completed() = render(HabitType.BINARY, HabitStatus.COMPLETED)

    @Test fun binary_skipped() = render(HabitType.BINARY, HabitStatus.SKIPPED)

    @Test fun binary_failed() = render(HabitType.BINARY, HabitStatus.FAILED)

    @Test fun quant_pending_zero() =
        render(HabitType.QUANTITATIVE, HabitStatus.PENDING, currentProgress = 0)

    @Test fun quant_pending_mid() =
        render(HabitType.QUANTITATIVE, HabitStatus.PENDING, currentProgress = 1750)

    @Test fun quant_pending_goal_reached() = render(
        HabitType.QUANTITATIVE,
        HabitStatus.PENDING,
        currentProgress = 2000
    )

    @Test fun quant_completed() = render(
        HabitType.QUANTITATIVE,
        HabitStatus.COMPLETED,
        currentProgress = 2000
    )

    @Test fun quant_skipped() = render(HabitType.QUANTITATIVE, HabitStatus.SKIPPED)

    @Test fun quant_failed() = render(
        HabitType.QUANTITATIVE,
        HabitStatus.FAILED,
        currentProgress = 500
    )

    private fun render(
        type: HabitType,
        status: HabitStatus,
        currentProgress: Int = 0,
        skipLocked: Boolean = false
    ) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                HabitDetailAction(
                    type = type,
                    status = status,
                    currentProgress = currentProgress,
                    unit = if (type == HabitType.QUANTITATIVE) "ml" else null,
                    isSkipLocked = skipLocked,
                    onComplete = {}, onIncrementProgress = {}, onCustomProgress = {},
                    onSkip = {}, onUndo = {}, onUndoIncrement = {},
                    modifier = Modifier.padding(horizontal = 22.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
