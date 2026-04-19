package com.ricardocosteira.rite.presentation.ui.today.components

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
class SectionHeaderScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun daily_focus_with_date() = render("Today's Focus", "Apr 17")

    @Test
    fun weekly_goals_with_week() = render("Weekly Goals", "This week")

    @Test
    fun all_kept_trailing() = render("Today's Focus", "4 / 4 kept")

    private fun render(title: String, trailing: String) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = false) {
                SectionHeader(
                    title = title,
                    trailingLabel = trailing,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
