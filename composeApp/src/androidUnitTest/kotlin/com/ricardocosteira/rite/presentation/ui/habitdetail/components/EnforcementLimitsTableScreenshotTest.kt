package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.domain.models.UndoPolicy
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
class EnforcementLimitsTableScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun flexible_light() = render(
        StrictnessPreset.FLEXIBLE,
        UndoPolicy.ALL_HISTORY,
        0,
        null,
        0,
        0,
        null,
        darkTheme = false
    )

    @Test fun flexible_dark() = render(
        StrictnessPreset.FLEXIBLE,
        UndoPolicy.ALL_HISTORY,
        0,
        null,
        0,
        0,
        null,
        darkTheme = true
    )

    @Test fun balanced_light() =
        render(StrictnessPreset.BALANCED, UndoPolicy.TODAY_ONLY, 1, 3, 2, 0, 2, darkTheme = false)

    @Test fun balanced_dark() =
        render(StrictnessPreset.BALANCED, UndoPolicy.TODAY_ONLY, 1, 3, 2, 0, 2, darkTheme = true)

    @Test fun unwavering_light() =
        render(StrictnessPreset.UNWAVERING, UndoPolicy.NONE, 1, 1, 0, 0, 0, darkTheme = false)

    @Test fun unwavering_dark() =
        render(StrictnessPreset.UNWAVERING, UndoPolicy.NONE, 1, 1, 0, 0, 0, darkTheme = true)

    @Test fun locked_light() =
        render(StrictnessPreset.BALANCED, UndoPolicy.TODAY_ONLY, 3, 3, 1, 2, 2, darkTheme = false)

    private fun render(
        preset: StrictnessPreset?,
        undo: UndoPolicy,
        snoozesUsed: Int,
        maxSnoozes: Int?,
        skipsWeek: Int,
        consecUsed: Int,
        consecMax: Int?,
        darkTheme: Boolean
    ) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = darkTheme) {
                EnforcementLimitsTable(
                    strictnessPreset = preset,
                    undoPolicy = undo,
                    snoozesUsedToday = snoozesUsed,
                    maxSnoozesPerDay = maxSnoozes,
                    skipsThisWeek = skipsWeek,
                    currentConsecutiveSkips = consecUsed,
                    maxConsecutiveSkips = consecMax,
                    modifier = Modifier.padding(horizontal = 22.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
