package com.ricardocosteira.rite.presentation.ui.habitdetail.components

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
class CategoryEyebrowScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun binary_light() = render(HabitType.BINARY, darkTheme = false)

    @Test fun binary_dark() = render(HabitType.BINARY, darkTheme = true)

    @Test fun quantitative_light() = render(HabitType.QUANTITATIVE, darkTheme = false)

    @Test fun quantitative_dark() = render(HabitType.QUANTITATIVE, darkTheme = true)

    private fun render(type: HabitType, darkTheme: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = darkTheme) {
                CategoryEyebrow(type = type, modifier = Modifier.padding(16.dp))
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
