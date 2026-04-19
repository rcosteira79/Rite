package com.ricardocosteira.rite.presentation.ui.habitdetail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.rite.domain.models.HabitStatus
import com.ricardocosteira.rite.presentation.ui.habitdetail.models.HeatmapDay
import com.ricardocosteira.rite.presentation.ui.theme.RiteThemeFallback
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
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
class TapestryScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test fun populated_light() = render(populatedData(), darkTheme = false)

    @Test fun populated_dark() = render(populatedData(), darkTheme = true)

    @Test fun empty_light() = render(emptyList(), darkTheme = false)

    private fun render(data: List<HeatmapDay>, darkTheme: Boolean) {
        composeRule.setContent {
            RiteThemeFallback(darkTheme = darkTheme) {
                Tapestry(
                    heatmapData = data,
                    weekRangeLabel = "W16 — W05",
                    modifier = Modifier.padding(horizontal = 22.dp),
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun populatedData(): List<HeatmapDay> {
        val today = LocalDate(2026, 4, 19)
        return (0..89).map { offset ->
            val date = today.minus(DatePeriod(days = offset))
            val bucket = offset % 7
            val (pct, status) = when (bucket) {
                0, 1, 2 -> 1.0f to HabitStatus.COMPLETED
                3 -> 0.6f to HabitStatus.PENDING
                4 -> 0.3f to HabitStatus.PENDING
                5 -> 0.0f to HabitStatus.SKIPPED
                else -> 0.0f to HabitStatus.FAILED
            }
            HeatmapDay(date = date.toString(), completionPercentage = pct, status = status)
        }
    }
}
