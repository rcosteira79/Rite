package com.ricardocosteira.habitlock.presentation.ui.habit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import com.ricardocosteira.habitlock.presentation.ui.BackHandler
import com.ricardocosteira.habitlock.presentation.ui.components.FormListRow
import com.ricardocosteira.habitlock.presentation.ui.components.PrimaryButton
import com.ricardocosteira.habitlock.presentation.ui.components.QuantityStepper
import com.ricardocosteira.habitlock.presentation.ui.components.SchedulePicker
import com.ricardocosteira.habitlock.presentation.ui.components.TypeToggle
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_cancel
import habitlock.composeapp.generated.resources.common_daily
import habitlock.composeapp.generated.resources.common_error_generic
import habitlock.composeapp.generated.resources.common_ok
import habitlock.composeapp.generated.resources.common_placeholder_habit_name
import habitlock.composeapp.generated.resources.common_weekly
import habitlock.composeapp.generated.resources.habit_form_button_discard_changes
import habitlock.composeapp.generated.resources.habit_form_button_discard_draft
import habitlock.composeapp.generated.resources.habit_form_button_establish
import habitlock.composeapp.generated.resources.habit_form_button_save
import habitlock.composeapp.generated.resources.habit_form_cadence_day
import habitlock.composeapp.generated.resources.habit_form_cadence_week
import habitlock.composeapp.generated.resources.habit_form_cd_delete
import habitlock.composeapp.generated.resources.habit_form_delete_dialog_body
import habitlock.composeapp.generated.resources.habit_form_delete_dialog_cancel
import habitlock.composeapp.generated.resources.habit_form_delete_dialog_confirm
import habitlock.composeapp.generated.resources.habit_form_delete_dialog_title
import habitlock.composeapp.generated.resources.habit_form_error_required_fields
import habitlock.composeapp.generated.resources.habit_form_note_collapsed_subtitle
import habitlock.composeapp.generated.resources.habit_form_note_collapsed_title
import habitlock.composeapp.generated.resources.habit_form_note_expanded_title
import habitlock.composeapp.generated.resources.habit_form_placeholder_unit
import habitlock.composeapp.generated.resources.habit_form_reminder_off
import habitlock.composeapp.generated.resources.habit_form_reminder_title
import habitlock.composeapp.generated.resources.habit_form_section_daily_target
import habitlock.composeapp.generated.resources.habit_form_section_habit_name
import habitlock.composeapp.generated.resources.habit_form_section_schedule
import habitlock.composeapp.generated.resources.habit_form_section_type
import habitlock.composeapp.generated.resources.habit_form_stepper_label_times
import habitlock.composeapp.generated.resources.habit_form_subtitle_create
import habitlock.composeapp.generated.resources.habit_form_title_edit
import habitlock.composeapp.generated.resources.habit_form_title_new_habit
import habitlock.composeapp.generated.resources.habit_form_unit_label
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun HabitFormScreen(
    habitIdToEdit: String?,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val factory = LocalAppComponent.current.habitFormViewModelFactory
    val viewModel = remember { factory.create(habitIdToEdit) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    val messageRequiredFields = stringResource(Res.string.habit_form_error_required_fields)
    val messageGenericError = stringResource(Res.string.common_error_generic)

    if (state.isEditing) {
        BackHandler { viewModel.discardChanges() }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HabitFormEvent.NavigateBack -> onNavigateBack()
                HabitFormEvent.RequiredFieldsMissing -> snackbarHostState.showSnackbar(messageRequiredFields)
                is HabitFormEvent.ShowError -> snackbarHostState.showSnackbar(event.message ?: messageGenericError)
            }
        }
    }

    HabitFormScreen(
        state = state,
        onNameChange = viewModel::updateName,
        onDescriptionChange = viewModel::updateDescription,
        onTypeChange = viewModel::updateType,
        onTargetValueChange = viewModel::updateTargetValue,
        onUnitChange = viewModel::updateUnit,
        onScheduleTypeChange = viewModel::updateScheduleType,
        onSelectedDaysChange = viewModel::updateSelectedDays,
        onQuotaChange = viewModel::updateQuota,
        onHasReminderChange = viewModel::updateHasReminder,
        onReminderTimeChange = viewModel::updateReminderTime,
        onSaveClick = viewModel::saveHabit,
        onDeleteClick = viewModel::deleteHabit,
        onDiscardDraftClick = viewModel::discardDraft,
        onDiscardChangesClick = viewModel::discardChanges
    )
}

@Composable
internal fun HabitFormScreen(
    state: HabitFormState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onScheduleTypeChange: (ScheduleType) -> Unit,
    onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    onQuotaChange: (String) -> Unit,
    onHasReminderChange: (Boolean) -> Unit,
    onReminderTimeChange: (LocalTime) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDiscardDraftClick: () -> Unit,
    onDiscardChangesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isNoteExpanded by remember { mutableStateOf(false) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    var isTimePickerVisible by remember { mutableStateOf(false) }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (isDeleteDialogVisible) {
        DeleteHabitDialog(
            onConfirm = {
                isDeleteDialogVisible = false
                onDeleteClick()
            },
            onDismiss = { isDeleteDialogVisible = false }
        )
    }

    if (isTimePickerVisible) {
        ReminderTimePickerDialog(
            initialTime = state.reminderTime ?: LocalTime(9, 0),
            onConfirm = { time: LocalTime ->
                onReminderTimeChange(time)
                isTimePickerVisible = false
            },
            onDismiss = { isTimePickerVisible = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Heading + delete icon (edit mode only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.isEditing) {
                    stringResource(Res.string.habit_form_title_edit)
                } else {
                    stringResource(Res.string.habit_form_title_new_habit)
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (state.isEditing) {
                IconButton(onClick = { isDeleteDialogVisible = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(Res.string.habit_form_cd_delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Accent bar
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )

        if (!state.isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.habit_form_subtitle_create),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // HABIT NAME
        SectionLabel(stringResource(Res.string.habit_form_section_habit_name))
        Spacer(modifier = Modifier.height(8.dp))
        UnderlineTextField(
            value = state.name,
            onValueChange = onNameChange,
            placeholder = stringResource(Res.string.common_placeholder_habit_name)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // TYPE
        SectionLabel(stringResource(Res.string.habit_form_section_type))
        Spacer(modifier = Modifier.height(8.dp))
        TypeToggle(
            selected = state.type,
            onSelectionChange = onTypeChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // DAILY TARGET
        SectionLabel(stringResource(Res.string.habit_form_section_daily_target))
        Spacer(modifier = Modifier.height(8.dp))

        val stepperValue: Int = if (state.type == HabitType.BINARY) {
            state.quota.toIntOrNull() ?: 1
        } else {
            state.targetValue.toIntOrNull() ?: 1
        }
        val cadence: String = if (state.scheduleType == ScheduleType.DAILY) {
            stringResource(Res.string.habit_form_cadence_day)
        } else {
            stringResource(Res.string.habit_form_cadence_week)
        }
        val stepperLabel: String = if (state.type == HabitType.QUANTITATIVE && state.unit.isNotBlank()) {
            "${state.unit} / $cadence"
        } else {
            "${stringResource(Res.string.habit_form_stepper_label_times)} / $cadence"
        }

        QuantityStepper(
            value = stepperValue,
            onValueChange = { newValue: Int ->
                if (state.type == HabitType.BINARY) {
                    onQuotaChange(newValue.toString())
                } else {
                    onTargetValueChange(newValue.toString())
                }
            },
            label = stepperLabel
        )

        AnimatedVisibility(
            visible = state.type == HabitType.QUANTITATIVE,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                UnderlineTextField(
                    value = state.unit,
                    onValueChange = onUnitChange,
                    label = stringResource(Res.string.habit_form_unit_label),
                    placeholder = stringResource(Res.string.habit_form_placeholder_unit)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SCHEDULE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(stringResource(Res.string.habit_form_section_schedule))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ScheduleTypePill(
                    text = stringResource(Res.string.common_daily),
                    isSelected = state.scheduleType == ScheduleType.DAILY,
                    onClick = { onScheduleTypeChange(ScheduleType.DAILY) }
                )
                ScheduleTypePill(
                    text = stringResource(Res.string.common_weekly),
                    isSelected = state.scheduleType == ScheduleType.WEEKLY,
                    onClick = { onScheduleTypeChange(ScheduleType.WEEKLY) }
                )
            }
        }

        AnimatedVisibility(
            visible = state.scheduleType == ScheduleType.WEEKLY,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                SchedulePicker(
                    selectedDays = state.selectedDays,
                    onSelectedDaysChange = onSelectedDaysChange
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reminder + Note card
        val reminderSubtitle: String = if (state.hasReminder) {
            state.reminderTime?.formatAmPm().orEmpty()
        } else {
            stringResource(Res.string.habit_form_reminder_off)
        }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                FormListRow(
                    icon = Icons.Outlined.Notifications,
                    iconTint = if (state.hasReminder) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    title = stringResource(Res.string.habit_form_reminder_title),
                    subtitle = reminderSubtitle,
                    onClick = if (state.hasReminder) {
                        { isTimePickerVisible = true }
                    } else {
                        null
                    },
                    showTopDivider = false,
                    trailingContent = {
                        Switch(
                            checked = state.hasReminder,
                            onCheckedChange = { checked: Boolean ->
                                onHasReminderChange(checked)
                                if (checked) isTimePickerVisible = true
                            }
                        )
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                FormListRow(
                    icon = Icons.Outlined.Edit,
                    iconTint = if (isNoteExpanded) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    title = if (isNoteExpanded) {
                        stringResource(Res.string.habit_form_note_expanded_title)
                    } else {
                        stringResource(Res.string.habit_form_note_collapsed_title)
                    },
                    subtitle = if (isNoteExpanded) "" else stringResource(Res.string.habit_form_note_collapsed_subtitle),
                    onClick = { isNoteExpanded = !isNoteExpanded },
                    showTopDivider = false,
                    trailingContent = null,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                AnimatedVisibility(
                    visible = isNoteExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    UnderlineTextField(
                        value = state.description,
                        onValueChange = onDescriptionChange,
                        placeholder = "",
                        maxLines = 5,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Primary CTA
        PrimaryButton(
            onClick = onSaveClick,
            enabled = state.isValid && !state.isSaving
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = if (state.isEditing) {
                        stringResource(Res.string.habit_form_button_save)
                    } else {
                        stringResource(Res.string.habit_form_button_establish)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary CTA
        TextButton(
            onClick = if (state.isEditing) onDiscardChangesClick else onDiscardDraftClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (state.isEditing) {
                    stringResource(Res.string.habit_form_button_discard_changes)
                } else {
                    stringResource(Res.string.habit_form_button_discard_draft)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun UnderlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String = "",
    maxLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            null
        },
        label = if (label.isNotEmpty()) {
            {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            null
        },
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
        ),
        maxLines = maxLines,
        singleLine = maxLines == 1
    )
}

@Composable
private fun ScheduleTypePill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val pillShape = RoundedCornerShape(percent = 50)
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(backgroundColor)
            .then(
                if (!isSelected && isDarkTheme) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, pillShape)
                } else {
                    Modifier
                }
            ).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ).padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.common_cancel))
                    }
                    TextButton(
                        onClick = {
                            onConfirm(LocalTime(timePickerState.hour, timePickerState.minute))
                        }
                    ) {
                        Text(stringResource(Res.string.common_ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteHabitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.habit_form_delete_dialog_title)) },
        text = { Text(stringResource(Res.string.habit_form_delete_dialog_body)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(Res.string.habit_form_delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.habit_form_delete_dialog_cancel))
            }
        }
    )
}

private fun LocalTime.formatAmPm(): String {
    val hour12: Int = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val amPm: String = if (hour < 12) "AM" else "PM"
    return "${hour12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
}
