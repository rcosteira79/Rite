package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
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
class RiteButtonScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun primary_light() = render(RiteButtonVariant.Primary, dark = false)

    @Test
    fun primary_dark() = render(RiteButtonVariant.Primary, dark = true)

    @Test
    fun secondary_light() = render(RiteButtonVariant.Secondary, dark = false)

    @Test
    fun secondary_dark() = render(RiteButtonVariant.Secondary, dark = true)

    @Test
    fun ghost_light() = render(RiteButtonVariant.Ghost, dark = false)

    @Test
    fun ghost_dark() = render(RiteButtonVariant.Ghost, dark = true)

    private fun render(variant: RiteButtonVariant, dark: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = dark) {
                RiteButton(onClick = {}, variant = variant) {
                    Text("Establish Habit")
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
