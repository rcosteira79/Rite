package com.ricardocosteira.rite.presentation.ui.habit

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.rite.di.LocalAppComponent
import com.ricardocosteira.rite.domain.models.HabitType
import com.ricardocosteira.rite.domain.models.ScheduleType
import com.ricardocosteira.rite.presentation.ui.BackHandler
import com.ricardocosteira.rite.presentation.ui.components.DetailRow
import com.ricardocosteira.rite.presentation.ui.components.PrimaryButton
import com.ricardocosteira.rite.presentation.ui.components.QuantityStepper
import com.ricardocosteira.rite.presentation.ui.components.SchedulePicker
import com.ricardocosteira.rite.presentation.ui.components.TypeToggle
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme
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
import rite.composeapp.generated.resources.habit_form_increment_label
import rite.composeapp.generated.resources.habit_form_note_collapsed_subtitle
import rite.composeapp.generated.resources.habit_form_note_collapsed_title
import rite.composeapp.generated.resources.habit_form_note_expanded_title
import rite.composeapp.generated.resources.habit_form_notification_permission_denied
import rite.composeapp.generated.resources.habit_form_placeholder_increment
import rite.composeapp.generated.resources.habit_form_placeholder_unit
import rite.composeapp.generated.resources.habit_form_reminder_off
import rite.composeapp.generated.resources.habit_form_reminder_title
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

                is HabitFormUiAction.DefaultIncrementChanged ->
                    viewModel.updateDefaultIncrement(action.value)

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

    val scrollState: ScrollState = rememberScrollState()
    val isScrolled: Boolean by remember { derivedStateOf { scrollState.value > 0 } }
    val filledColor: Color = RiteAppTheme.colorScheme.surfaceContainerHighest
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
        modifier = modifier.imePadding(),
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
                                tint = RiteAppTheme.colorScheme.error,
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
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Heading
            Text(
                text = if (state.isEditing) {
                    stringResource(Res.string.habit_form_title_edit)
                } else {
                    stringResource(Res.string.habit_form_title_new_habit)
                },
                style = RiteAppTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = RiteAppTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Accent bar
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(3.dp)
                    .background(RiteAppTheme.colorScheme.primary, RoundedCornerShape(2.dp))
            )

            if (!state.isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.habit_form_subtitle_create),
                    style = RiteAppTheme.typography.bodyLarge,
                    color = RiteAppTheme.colorScheme.onSurfaceVariant
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

            AnimatedVisibility(
                visible = state.type == HabitType.QUANTITATIVE,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val cadence: String = if (state.scheduleType == ScheduleType.DAILY) {
                    stringResource(Res.string.habit_form_cadence_day)
                } else {
                    stringResource(Res.string.habit_form_cadence_week)
                }
                val stepperLabel: String = if (state.unit.isNotBlank()) {
                    "${state.unit} / $cadence"
                } else {
                    "${stringResource(Res.string.habit_form_stepper_label_times)} / $cadence"
                }

                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    QuantityStepper(
                        value = state.stepperValue,
                        onValueChange = { newValue: Int ->
                            onAction(state.stepperChangeAction(newValue))
                        },
                        label = stepperLabel
                    )
                }
            }

            AnimatedVisibility(
                visible = state.type == HabitType.QUANTITATIVE,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.unit,
                        onValueChange = { onAction(HabitFormUiAction.UnitChanged(it)) },
                        label = {
                            Text(
                                text = stringResource(Res.string.habit_form_unit_label),
                                style = RiteAppTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.habit_form_placeholder_unit),
                                color = RiteAppTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RiteAppTheme.colorScheme.primary,
                            unfocusedBorderColor = RiteAppTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.defaultIncrement,
                        onValueChange = { onAction(HabitFormUiAction.DefaultIncrementChanged(it)) },
                        label = {
                            Text(
                                text = stringResource(Res.string.habit_form_increment_label),
                                style = RiteAppTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.habit_form_placeholder_increment),
                                color = RiteAppTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RiteAppTheme.colorScheme.primary,
                            unfocusedBorderColor = RiteAppTheme.colorScheme.outlineVariant
                        )
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
                state.reminderTime?.formatAmPm().orEmpty()
            } else {
                stringResource(Res.string.habit_form_reminder_off)
            }

            // Contextual notification messages
            if (!state.isNotificationPermissionGranted) {
                Text(
                    text = stringResource(Res.string.habit_form_notification_permission_denied),
                    style = RiteAppTheme.typography.bodySmall,
                    color = RiteAppTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(HabitFormUiAction.NotificationSettingsClicked) }
                        .padding(bottom = 8.dp)
                )
            } else if (state.showBothEnabledHint) {
                Text(
                    text = stringResource(Res.string.habit_form_both_notifications_hint),
                    style = RiteAppTheme.typography.bodySmall,
                    color = RiteAppTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = RiteAppTheme.colorScheme.surfaceContainerLow,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    DetailRow(
                        icon = Icons.Outlined.Notifications,
                        iconTint = if (state.hasReminder) {
                            RiteAppTheme.colorScheme.onSurface
                        } else {
                            RiteAppTheme.colorScheme.onSurfaceVariant
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
                                    onAction(HabitFormUiAction.HasReminderChanged(checked))
                                    if (checked) isTimePickerVisible = true
                                },
                                enabled = state.areNotificationTogglesEnabled
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    DetailRow(
                        icon = Icons.Outlined.Notifications,
                        iconTint = if (state.isTrackingEnabled) {
                            RiteAppTheme.colorScheme.onSurface
                        } else {
                            RiteAppTheme.colorScheme.onSurfaceVariant
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
                            RiteAppTheme.colorScheme.onSurface
                        } else {
                            RiteAppTheme.colorScheme.onSurfaceVariant
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
                    color = RiteAppTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(resource: StringResource) {
    Text(
        text = stringResource(resource),
        style = RiteAppTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = RiteAppTheme.colorScheme.onSurfaceVariant
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
            { Text(placeholder, color = RiteAppTheme.colorScheme.onSurfaceVariant) }
        } else {
            null
        },
        label = if (label.isNotEmpty()) {
            {
                Text(
                    text = label,
                    style = RiteAppTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            null
        },
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = RiteAppTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = RiteAppTheme.colorScheme.surfaceContainerHighest,
            focusedIndicatorColor = RiteAppTheme.colorScheme.primary,
            unfocusedIndicatorColor = RiteAppTheme.colorScheme.outlineVariant
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
        RiteAppTheme.colorScheme.primaryContainer
    } else {
        RiteAppTheme.colorScheme.surfaceContainerLow
    }
    val contentColor = if (isSelected) {
        RiteAppTheme.colorScheme.onPrimaryContainer
    } else {
        RiteAppTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(backgroundColor)
            .then(
                if (!isSelected && isDarkTheme) {
                    Modifier.border(1.dp, RiteAppTheme.colorScheme.outlineVariant, pillShape)
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
            style = RiteAppTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
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
            color = RiteAppTheme.colorScheme.surface
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
                    contentColor = RiteAppTheme.colorScheme.error
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
