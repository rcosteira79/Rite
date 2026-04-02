package com.ricardocosteira.habitlock.presentation.ui.habit

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import com.ricardocosteira.habitlock.presentation.ui.theme.HabitLockThemeFallback
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
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
class HabitFormScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    // --- Binary create ---

    @Test
    fun habitForm_create_binary_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        name = "",
                        type = HabitType.BINARY,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitForm_create_binary_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        name = "",
                        type = HabitType.BINARY,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Quantitative create ---

    @Test
    fun habitForm_create_quantitative_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.QUANTITATIVE,
                        targetValue = "5",
                        unit = "km",
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitForm_create_quantitative_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.QUANTITATIVE,
                        targetValue = "5",
                        unit = "km",
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Weekly schedule create ---

    @Test
    fun habitForm_create_weeklySchedule_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        scheduleType = ScheduleType.WEEKLY,
                        selectedDays = setOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.FRIDAY
                        )
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitForm_create_weeklySchedule_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        scheduleType = ScheduleType.WEEKLY,
                        selectedDays = setOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.FRIDAY
                        )
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Note with content ---

    @Test
    fun habitForm_create_noteExpanded_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        description = "Typical intention",
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitForm_create_noteExpanded_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        description = "Typical intention",
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Reminder on ---

    @Test
    fun habitForm_create_reminderOn_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        hasReminder = true,
                        reminderTime = LocalTime(9, 0),
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitForm_create_reminderOn_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        hasReminder = true,
                        reminderTime = LocalTime(9, 0),
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Edit mode binary ---

    @Test
    fun habitForm_edit_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        habitId = "habit-1",
                        name = "Deep Work",
                        type = HabitType.BINARY,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitForm_edit_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        habitId = "habit-1",
                        name = "Deep Work",
                        type = HabitType.BINARY,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Edit mode weekly ---

    @Test
    fun habitForm_edit_weeklySchedule_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        habitId = "habit-2",
                        name = "Run",
                        type = HabitType.BINARY,
                        scheduleType = ScheduleType.WEEKLY,
                        selectedDays = setOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.FRIDAY
                        )
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitForm_edit_weeklySchedule_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        habitId = "habit-2",
                        name = "Run",
                        type = HabitType.BINARY,
                        scheduleType = ScheduleType.WEEKLY,
                        selectedDays = setOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.FRIDAY
                        )
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Tracking enabled ---

    @Test
    fun habitForm_create_trackingEnabled_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        isTrackingEnabled = true,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitForm_create_trackingEnabled_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        isTrackingEnabled = true,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Both reminder and tracking enabled ---

    @Test
    fun habitForm_create_bothNotificationsEnabled_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        hasReminder = true,
                        reminderTime = LocalTime(9, 0),
                        isTrackingEnabled = true,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitForm_create_bothNotificationsEnabled_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        hasReminder = true,
                        reminderTime = LocalTime(9, 0),
                        isTrackingEnabled = true,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Notification permission denied ---

    @Test
    fun habitForm_create_notificationPermissionDenied_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        isNotificationPermissionGranted = false,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitForm_create_notificationPermissionDenied_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        isNotificationPermissionGranted = false,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
