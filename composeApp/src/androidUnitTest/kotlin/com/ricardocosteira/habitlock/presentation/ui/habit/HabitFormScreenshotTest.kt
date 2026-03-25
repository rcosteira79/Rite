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
@Config(sdk = [33], qualifiers = "w360dp-h800dp-420dpi", application = android.app.Application::class)
class HabitFormScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    // --- Binary create ---

    @Test
    fun habitFormScreen_binaryCreate_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        name = "",
                        type = HabitType.BINARY,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitFormScreen_binaryCreate_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        name = "",
                        type = HabitType.BINARY,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Quantitative create ---

    @Test
    fun habitFormScreen_quantitativeCreate_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.QUANTITATIVE,
                        targetValue = "5",
                        unit = "km",
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitFormScreen_quantitativeCreate_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.QUANTITATIVE,
                        targetValue = "5",
                        unit = "km",
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Weekly schedule create ---

    @Test
    fun habitFormScreen_weeklyScheduleCreate_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        scheduleType = ScheduleType.WEEKLY,
                        selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitFormScreen_weeklyScheduleCreate_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        scheduleType = ScheduleType.WEEKLY,
                        selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Note with content ---

    @Test
    fun habitFormScreen_noteWithContent_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        description = "Typical intention",
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitFormScreen_noteWithContent_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        description = "Typical intention",
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Reminder on ---

    @Test
    fun habitFormScreen_reminderOn_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        hasReminder = true,
                        reminderTime = LocalTime(9, 0),
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitFormScreen_reminderOn_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        type = HabitType.BINARY,
                        hasReminder = true,
                        reminderTime = LocalTime(9, 0),
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Edit mode binary ---

    @Test
    fun habitFormScreen_editModeBinary_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        habitId = "habit-1",
                        name = "Deep Work",
                        type = HabitType.BINARY,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitFormScreen_editModeBinary_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        habitId = "habit-1",
                        name = "Deep Work",
                        type = HabitType.BINARY,
                        selectedDays = DayOfWeek.entries.toSet()
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // --- Edit mode weekly ---

    @Test
    fun habitFormScreen_editModeWeekly_lightTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = false) {
                HabitFormScreen(
                    state = HabitFormState(
                        habitId = "habit-2",
                        name = "Run",
                        type = HabitType.BINARY,
                        scheduleType = ScheduleType.WEEKLY,
                        selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun habitFormScreen_editModeWeekly_darkTheme() {
        composeRule.setContent {
            HabitLockThemeFallback(darkTheme = true) {
                HabitFormScreen(
                    state = HabitFormState(
                        habitId = "habit-2",
                        name = "Run",
                        type = HabitType.BINARY,
                        scheduleType = ScheduleType.WEEKLY,
                        selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                    ),
                    onNameChange = {},
                    onDescriptionChange = {},
                    onTypeChange = {},
                    onTargetValueChange = {},
                    onUnitChange = {},
                    onScheduleTypeChange = {},
                    onSelectedDaysChange = {},
                    onQuotaChange = {},
                    onHasReminderChange = {},
                    onSaveClick = {},
                    onDeleteClick = {},
                    onDiscardDraftClick = {},
                    onDiscardChangesClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
