package com.ricardocosteira.rite.presentation.ui.habitdetail.components

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
class StatTileRowScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun typical_light() = render(darkTheme = false)

    @Test fun typical_dark() = render(darkTheme = true)

    private fun render(darkTheme: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = darkTheme) {
                StatTileRow(
                    currentStreak = 14,
                    longestStreak = 42,
                    habitScore = 82,
                    modifier = Modifier.padding(horizontal = 22.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
