package com.ricardocosteira.rite.presentation.ui.today

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
import org.jetbrains.compose.resources.StringResource
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.today_header_salute_all_done
import rite.composeapp.generated.resources.today_header_salute_empty

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [33],
    qualifiers = "w360dp-h800dp-420dpi",
    application = android.app.Application::class
)
class TodayHeaderExpandedScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun mixed_progress_light() = render(
        pendingCount = 2,
        totalDaily = 4,
        progress = 0.5f
    )

    @Test fun all_done_light() = render(
        pendingCount = 0,
        totalDaily = 4,
        progress = 1f,
        saluteKey = Res.string.today_header_salute_all_done
    )

    @Test fun empty_light() = render(
        pendingCount = 0,
        totalDaily = 0,
        progress = 0f,
        hasHabits = false,
        saluteKey = Res.string.today_header_salute_empty
    )

    @Test fun mixed_progress_dark() = render(
        pendingCount = 2,
        totalDaily = 4,
        progress = 0.5f,
        dark = true
    )

    private fun render(
        pendingCount: Int,
        totalDaily: Int,
        progress: Float,
        hasHabits: Boolean = true,
        saluteKey: StringResource? = null,
        dark: Boolean = false
    ) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RiteAppTheme.colors.background)
                ) {
                    TodayHeaderExpanded(
                        saluteKey = saluteKey,
                        pendingCount = pendingCount,
                        dailyTotal = totalDaily,
                        hasHabits = hasHabits,
                        dailyProgressFraction = progress,
                        strictnessPreset = StrictnessPreset.BALANCED,
                        modifier = Modifier
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
