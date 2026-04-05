package com.ricardocosteira.rite.presentation.ui.habit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.rite.di.LocalAppComponent
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ReminderType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.presentation.ui.BackHandler
import com.ricardocosteira.rite.presentation.ui.components.DetailRow
import com.ricardocosteira.rite.presentation.ui.components.PrimaryButton
import com.ricardocosteira.rite.presentation.ui.components.QuantityStepper
import com.ricardocosteira.rite.presentation.ui.components.SchedulePicker
import com.ricardocosteira.rite.presentation.ui.components.TypeToggle
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import rite.composeapp.generated.resources.Res
import rite.composeapp.generated.resources.common_cancel
import rite.composeapp.generated.resources.common_cd_back
import rite.composeapp.generated.resources.common_daily
import rite.composeapp.generated.resources.common_error_generic
import rite.composeapp.generated.resources.common_ok
import rite.composeapp.generated.resources.common_placeholder_habit_name
import rite.composeapp.generated.resources.common_weekly
import rite.composeapp.generated.resources.habit_form_both_notifications_hint
import rite.composeapp.generated.resources.habit_form_button_discard_changes
import rite.composeapp.generated.resources.habit_form_button_discard_draft
import rite.composeapp.generated.resources.habit_form_button_establish
import rite.composeapp.generated.resources.habit_form_button_save
import rite.composeapp.generated.resources.habit_form_cadence_day
import rite.composeapp.generated.resources.habit_form_cadence_week
import rite.composeapp.generated.resources.habit_form_cd_archive
import rite.composeapp.generated.resources.habit_form_cd_delete
import rite.composeapp.generated.resources.habit_form_delete_dialog_body
import rite.composeapp.generated.resources.habit_form_delete_dialog_cancel
import rite.composeapp.generated.resources.habit_form_delete_dialog_confirm
import rite.composeapp.generated.resources.habit_form_delete_dialog_title
import rite.composeapp.generated.resources.habit_form_error_habit_not_found
import rite.composeapp.generated.resources.habit_form_error_required_fields
import rite.composeapp.generated.resources.habit_form_note_collapsed_subtitle
import rite.composeapp.generated.resources.habit_form_note_collapsed_title
import rite.composeapp.generated.resources.habit_form_note_expanded_title
import rite.composeapp.generated.resources.habit_form_notification_permission_denied
import rite.composeapp.generated.resources.habit_form_periodic_every
import rite.composeapp.generated.resources.habit_form_periodic_from
import rite.composeapp.generated.resources.habit_form_periodic_hours
import rite.composeapp.generated.resources.habit_form_periodic_invalid_window
import rite.composeapp.generated.resources.habit_form_periodic_minutes
import rite.composeapp.generated.resources.habit_form_periodic_until
import rite.composeapp.generated.resources.habit_form_placeholder_unit
import rite.composeapp.generated.resources.habit_form_reminder_off
import rite.composeapp.generated.resources.habit_form_reminder_title
import rite.composeapp.generated.resources.habit_form_reminder_type_fixed
import rite.composeapp.generated.resources.habit_form_reminder_type_periodic
import rite.composeapp.generated.resources.habit_form_section_daily_target
import rite.composeapp.generated.resources.habit_form_section_habit_name
import rite.composeapp.generated.resources.habit_form_section_schedule
import rite.composeapp.generated.resources.habit_form_section_type
import rite.composeapp.generated.resources.habit_form_stepper_label_times
import rite.composeapp.generated.resources.habit_form_subtitle_create
import rite.composeapp.generated.resources.habit_form_title_edit
import rite.composeapp.generated.resources.habit_form_title_new_habit
import rite.composeapp.generated.resources.habit_form_tracking_subtitle
import rite.composeapp.generated.resources.habit_form_tracking_title
import rite.composeapp.generated.resources.habit_form_unit_label

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
    val messageHabitNotFound = stringResource(Res.string.habit_form_error_habit_not_found)

    if (state.isEditing) {
        BackHandler { viewModel.discardChanges() }
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.refreshNotificationPermission()
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HabitFormEvent.NavigateBack -> onNavigateBack()

                HabitFormEvent.RequiredFieldsMissing ->
                    snackbarHostState.showSnackbar(
                        messageRequiredFields
                    )

                HabitFormEvent.HabitNotFound -> snackbarHostState.showSnackbar(messageHabitNotFound)

                is HabitFormEvent.ShowError ->
                    snackbarHostState.showSnackbar(
                        event.message ?: messageGenericError
                    )
            }
        }
    }

    HabitFormScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is HabitFormUiAction.NameChanged -> viewModel.updateName(action.name)

                is HabitFormUiAction.DescriptionChanged ->
                    viewModel.updateDescription(
                        action.description
                    )

                is HabitFormUiAction.TypeChanged -> viewModel.updateType(action.type)

                is HabitFormUiAction.TargetValueChanged -> viewModel.updateTargetValue(action.value)

                is HabitFormUiAction.UnitChanged -> viewModel.updateUnit(action.unit)

                is HabitFormUiAction.ScheduleTypeChanged ->
                    viewModel.updateScheduleType(
                        action.scheduleType
                    )

                is HabitFormUiAction.SelectedDaysChanged ->
                    viewModel.updateSelectedDays(
                        action.days
                    )

                is HabitFormUiAction.QuotaChanged -> viewModel.updateQuota(action.quota)

                is HabitFormUiAction.HasReminderChanged ->
                    viewModel.updateHasReminder(
                        action.hasReminder
                    )

                is HabitFormUiAction.ReminderTimeChanged ->
                    viewModel.updateReminderTime(
                        action.hour,
                        action.minute
                    )

                is HabitFormUiAction.ReminderTypeChanged ->
                    viewModel.updateReminderType(action.reminderType)

                is HabitFormUiAction.IntervalChanged ->
                    viewModel.updateIntervalMinutes(action.interval)

                is HabitFormUiAction.PeriodicStartTimeChanged ->
                    viewModel.updatePeriodicStartTime(action.hour, action.minute)

                is HabitFormUiAction.PeriodicEndTimeChanged ->
                    viewModel.updatePeriodicEndTime(action.hour, action.minute)

                HabitFormUiAction.SaveClicked -> viewModel.saveHabit()

                HabitFormUiAction.DeleteClicked -> viewModel.deleteHabit()

                HabitFormUiAction.ArchiveClicked -> viewModel.archiveHabit()

                HabitFormUiAction.DiscardDraftClicked -> viewModel.discardDraft()

                HabitFormUiAction.DiscardChangesClicked -> viewModel.discardChanges()

                is HabitFormUiAction.IsTrackingEnabledChanged ->
                    viewModel.updateIsTrackingEnabled(action.isEnabled)

                HabitFormUiAction.NotificationSettingsClicked ->
                    viewModel.openNotificationSettings()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitFormScreen(
    state: HabitFormState,
    onAction: (HabitFormUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var isNoteExpanded by rememberSaveable { mutableStateOf(false) }
    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isTimePickerVisible by rememberSaveable { mutableStateOf(false) }
    var isStartTimePickerVisible by remember { mutableStateOf(false) }
    var isEndTimePickerVisible by remember { mutableStateOf(false) }

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
                onAction(HabitFormUiAction.DeleteClicked)
            },
            onDismiss = { isDeleteDialogVisible = false }
        )
    }

    if (isTimePickerVisible) {
        ReminderTimePickerDialog(
            initialTime = state.resolvedReminderTime,
            onConfirm = { hour, minute ->
                onAction(HabitFormUiAction.ReminderTimeChanged(hour, minute))
                isTimePickerVisible = false
            },
            onDismiss = { isTimePickerVisible = false }
        )
    }

    // Start time picker for periodic reminders
    if (isStartTimePickerVisible) {
        ReminderTimePickerDialog(
            initialTime = state.startTime ?: LocalTime(8, 0),
            onConfirm = { hour: Int, minute: Int ->
                onAction(HabitFormUiAction.PeriodicStartTimeChanged(hour, minute))
                isStartTimePickerVisible = false
            },
            onDismiss = { isStartTimePickerVisible = false }
        )
    }

    // End time picker for periodic reminders
    if (isEndTimePickerVisible) {
        ReminderTimePickerDialog(
            initialTime = state.endTime ?: LocalTime(22, 0),
            onConfirm = { hour: Int, minute: Int ->
                onAction(HabitFormUiAction.PeriodicEndTimeChanged(hour, minute))
                isEndTimePickerVisible = false
            },
            onDismiss = { isEndTimePickerVisible = false }
        )
    }

    val scrollState: ScrollState = rememberScrollState()
    val isScrolled: Boolean by remember { derivedStateOf { scrollState.value > 0 } }
    val filledColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
    val targetColor: Color = if (isScrolled) {
        filledColor
    } else {
        filledColor.copy(alpha = 0f)
    }
    val iconContainerColor: Color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 200),
        label = "iconContainerColor"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(HabitFormUiAction.DiscardChangesClicked) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = iconContainerColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_cd_back)
                        )
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(
                            onClick = { onAction(HabitFormUiAction.ArchiveClicked) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = iconContainerColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = stringResource(
                                    Res.string.habit_form_cd_archive
                                ),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { isDeleteDialogVisible = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = iconContainerColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(
                                    Res.string.habit_form_cd_delete
                                ),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Heading
            Text(
                text = if (state.isEditing) {
                    stringResource(Res.string.habit_form_title_edit)
                } else {
                    stringResource(Res.string.habit_form_title_new_habit)
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

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
            SectionLabel(Res.string.habit_form_section_habit_name)
            Spacer(modifier = Modifier.height(8.dp))
            UnderlineTextField(
                value = state.name,
                onValueChange = { onAction(HabitFormUiAction.NameChanged(it)) },
                placeholder = stringResource(Res.string.common_placeholder_habit_name),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // TYPE
            SectionLabel(Res.string.habit_form_section_type)
            Spacer(modifier = Modifier.height(8.dp))
            TypeToggle(
                selected = state.type,
                onSelectionChange = { onAction(HabitFormUiAction.TypeChanged(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // DAILY TARGET
            SectionLabel(Res.string.habit_form_section_daily_target)
            Spacer(modifier = Modifier.height(8.dp))

            val cadence: String = if (state.scheduleType == ScheduleType.DAILY) {
                stringResource(Res.string.habit_form_cadence_day)
            } else {
                stringResource(Res.string.habit_form_cadence_week)
            }
            val stepperLabel: String = if (state.type == HabitType.QUANTITATIVE &&
                state.unit.isNotBlank()
            ) {
                "${state.unit} / $cadence"
            } else {
                "${stringResource(Res.string.habit_form_stepper_label_times)} / $cadence"
            }

            QuantityStepper(
                value = state.stepperValue,
                onValueChange = { newValue: Int ->
                    onAction(state.stepperChangeAction(newValue))
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
                        onValueChange = { onAction(HabitFormUiAction.UnitChanged(it)) },
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
                SectionLabel(Res.string.habit_form_section_schedule)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ScheduleTypePill(
                        text = stringResource(Res.string.common_daily),
                        isSelected = state.scheduleType == ScheduleType.DAILY,
                        onClick = {
                            onAction(HabitFormUiAction.ScheduleTypeChanged(ScheduleType.DAILY))
                        }
                    )
                    ScheduleTypePill(
                        text = stringResource(Res.string.common_weekly),
                        isSelected = state.scheduleType == ScheduleType.WEEKLY,
                        onClick = {
                            onAction(HabitFormUiAction.ScheduleTypeChanged(ScheduleType.WEEKLY))
                        }
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
                        onSelectedDaysChange = {
                            onAction(HabitFormUiAction.SelectedDaysChanged(it))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reminder + Note card
            val reminderSubtitle: String = if (state.hasReminder) {
                when (state.reminderType) {
                    ReminderType.FIXED -> state.reminderTime?.formatAmPm().orEmpty()

                    ReminderType.PERIODIC -> {
                        val interval = state.intervalMinutes.toIntOrNull()
                        if (interval != null && state.startTime != null && state.endTime != null) {
                            val intervalText = if (interval >= 60 && interval % 60 == 0) {
                                "${interval / 60}${stringResource(
                                    Res.string.habit_form_periodic_hours
                                )}"
                            } else {
                                "$interval${stringResource(Res.string.habit_form_periodic_minutes)}"
                            }
                            "${stringResource(Res.string.habit_form_periodic_every)} $intervalText"
                        } else {
                            stringResource(Res.string.habit_form_reminder_type_periodic)
                        }
                    }
                }
            } else {
                stringResource(Res.string.habit_form_reminder_off)
            }

            // Contextual notification messages
            if (!state.isNotificationPermissionGranted) {
                Text(
                    text = stringResource(Res.string.habit_form_notification_permission_denied),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(HabitFormUiAction.NotificationSettingsClicked) }
                        .padding(bottom = 8.dp)
                )
            } else if (state.showBothEnabledHint) {
                Text(
                    text = stringResource(Res.string.habit_form_both_notifications_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    DetailRow(
                        icon = Icons.Outlined.Notifications,
                        iconTint = if (state.hasReminder) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        title = stringResource(Res.string.habit_form_reminder_title),
                        subtitle = reminderSubtitle,
                        onClick = if (state.hasReminder &&
                            state.reminderType == ReminderType.FIXED
                        ) {
                            { isTimePickerVisible = true }
                        } else {
                            null
                        },
                        showTopDivider = false,
                        trailingContent = {
                            Switch(
                                checked = state.hasReminder,
                                onCheckedChange = { checked: Boolean ->
                                    onAction(HabitFormUiAction.HasReminderChanged(checked))
                                },
                                enabled = state.areNotificationTogglesEnabled
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // Reminder type selector and config (animated)
                    AnimatedVisibility(
                        visible = state.hasReminder,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                            // Segmented button row
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                SegmentedButton(
                                    selected = state.reminderType == ReminderType.FIXED,
                                    onClick = {
                                        onAction(
                                            HabitFormUiAction.ReminderTypeChanged(
                                                ReminderType.FIXED
                                            )
                                        )
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = 0,
                                        count = 2
                                    )
                                ) {
                                    Text(
                                        text = stringResource(
                                            Res.string.habit_form_reminder_type_fixed
                                        )
                                    )
                                }
                                SegmentedButton(
                                    selected = state.reminderType == ReminderType.PERIODIC,
                                    onClick = {
                                        onAction(
                                            HabitFormUiAction.ReminderTypeChanged(
                                                ReminderType.PERIODIC
                                            )
                                        )
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = 1,
                                        count = 2
                                    )
                                ) {
                                    Text(
                                        text = stringResource(
                                            Res.string.habit_form_reminder_type_periodic
                                        )
                                    )
                                }
                            }

                            // Content crossfade between Fixed and Periodic
                            Crossfade(
                                targetState = state.reminderType,
                                label = "reminder_type_content"
                            ) { currentType: ReminderType ->
                                when (currentType) {
                                    ReminderType.FIXED -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isTimePickerVisible = true }
                                                .padding(vertical = 12.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = state.resolvedReminderTime.formatAmPm(),
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    ReminderType.PERIODIC -> {
                                        PeriodicReminderConfig(
                                            intervalMinutes = state.intervalMinutes,
                                            startTime = state.startTime,
                                            endTime = state.endTime,
                                            onIntervalChanged = { interval: String ->
                                                onAction(
                                                    HabitFormUiAction.IntervalChanged(interval)
                                                )
                                            },
                                            onStartTimeClick = {
                                                isStartTimePickerVisible = true
                                            },
                                            onEndTimeClick = { isEndTimePickerVisible = true }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    DetailRow(
                        icon = Icons.Outlined.Notifications,
                        iconTint = if (state.isTrackingEnabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        title = stringResource(Res.string.habit_form_tracking_title),
                        subtitle = stringResource(Res.string.habit_form_tracking_subtitle),
                        onClick = null,
                        showTopDivider = false,
                        trailingContent = {
                            Switch(
                                checked = state.isTrackingEnabled,
                                onCheckedChange = { checked: Boolean ->
                                    onAction(HabitFormUiAction.IsTrackingEnabledChanged(checked))
                                },
                                enabled = state.areNotificationTogglesEnabled
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    DetailRow(
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
                        subtitle = if (isNoteExpanded) {
                            ""
                        } else {
                            stringResource(Res.string.habit_form_note_collapsed_subtitle)
                        },
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
                            onValueChange = { onAction(HabitFormUiAction.DescriptionChanged(it)) },
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
                onClick = { onAction(HabitFormUiAction.SaveClicked) },
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
                onClick = if (state.isEditing) {
                    { onAction(HabitFormUiAction.DiscardChangesClicked) }
                } else {
                    { onAction(HabitFormUiAction.DiscardDraftClicked) }
                },
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
}

@Composable
private fun SectionLabel(resource: StringResource) {
    Text(
        text = stringResource(resource),
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
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
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
private fun PeriodicReminderConfig(
    intervalMinutes: String,
    startTime: LocalTime?,
    endTime: LocalTime?,
    onIntervalChanged: (String) -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    val isWindowInvalid: Boolean = startTime != null && endTime != null && startTime >= endTime

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Interval row with unit toggle
        var isHoursMode by remember {
            mutableStateOf(
                intervalMinutes.toIntOrNull()?.let { it >= 60 && it % 60 == 0 } ?: false
            )
        }
        val displayValue: String = if (isHoursMode) {
            val mins = intervalMinutes.toIntOrNull() ?: 0
            if (mins > 0) (mins / 60).toString() else ""
        } else {
            intervalMinutes
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.habit_form_periodic_every),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = displayValue,
                    onValueChange = { value: String ->
                        if (value.all { it.isDigit() }) {
                            val rawMinutes: Int = if (isHoursMode) {
                                (value.toIntOrNull() ?: 0) * 60
                            } else {
                                value.toIntOrNull() ?: 0
                            }
                            onIntervalChanged(rawMinutes.toString())
                        }
                    },
                    modifier = Modifier.width(72.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = !isHoursMode,
                        onClick = { isHoursMode = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(
                            text = stringResource(Res.string.habit_form_periodic_minutes),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    SegmentedButton(
                        selected = isHoursMode,
                        onClick = { isHoursMode = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(
                            text = stringResource(Res.string.habit_form_periodic_hours),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // From row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onStartTimeClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.habit_form_periodic_from),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = startTime?.formatAmPm() ?: "--:--",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Until row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEndTimeClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.habit_form_periodic_until),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isWindowInvalid) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = endTime?.formatAmPm() ?: "--:--",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isWindowInvalid) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .background(
                        color = if (isWindowInvalid) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Validation error
        if (isWindowInvalid) {
            Text(
                text = stringResource(Res.string.habit_form_periodic_invalid_window),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (hour: Int, minute: Int) -> Unit,
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
                        onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }
                    ) {
                        Text(stringResource(Res.string.common_ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteHabitDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.habit_form_delete_dialog_title)) },
        text = { Text(stringResource(Res.string.habit_form_delete_dialog_body)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
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
