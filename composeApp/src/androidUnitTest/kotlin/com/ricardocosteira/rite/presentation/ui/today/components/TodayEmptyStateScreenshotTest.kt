package com.ricardocosteira.rite.presentation.ui.today.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
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
class TodayEmptyStateScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun empty_state_light() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                Surface(
                    color = RiteAppTheme.colors.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    TodayEmptyState(onAddFirstHabit = {})
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun empty_state_dark() {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = true) {
                Surface(
                    color = RiteAppTheme.colors.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    TodayEmptyState(onAddFirstHabit = {})
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
