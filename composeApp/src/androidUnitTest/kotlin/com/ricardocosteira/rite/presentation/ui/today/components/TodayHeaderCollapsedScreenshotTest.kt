package com.ricardocosteira.rite.presentation.ui.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.StrictnessPreset
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
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
class TodayHeaderCollapsedScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun collapsed_mixed_light() = render(
        pendingCount = 2,
        totalDaily = 4,
        progress = 0.5f
    )

    @Test fun collapsed_all_done_light() = render(
        pendingCount = 0,
        totalDaily = 4,
        progress = 1f
    )

    @Test fun collapsed_dark() = render(
        pendingCount = 2,
        totalDaily = 4,
        progress = 0.5f,
        dark = true
    )

    private fun render(pendingCount: Int, totalDaily: Int, progress: Float, dark: Boolean = false) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RiteAppTheme.colors.background)
                ) {
                    TodayHeaderCollapsed(
                        saluteKey = null,
                        pendingCount = pendingCount,
                        dailyTotal = totalDaily,
                        dailyProgressFraction = progress,
                        strictnessPreset = StrictnessPreset.BALANCED,
                        onAddHabit = {}
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
