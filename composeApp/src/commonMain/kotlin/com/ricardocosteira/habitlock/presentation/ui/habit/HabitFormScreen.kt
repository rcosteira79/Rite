package com.ricardocosteira.habitlock.presentation.ui.habit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.domain.models.ReminderType
import com.ricardocosteira.habitlock.domain.models.ScheduleType
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_cd_back
import habitlock.composeapp.generated.resources.common_daily
import habitlock.composeapp.generated.resources.common_placeholder_habit_name
import habitlock.composeapp.generated.resources.common_placeholder_target_value
import habitlock.composeapp.generated.resources.common_quantitative
import habitlock.composeapp.generated.resources.common_weekly
import habitlock.composeapp.generated.resources.habit_form_button_create
import habitlock.composeapp.generated.resources.habit_form_button_save
import habitlock.composeapp.generated.resources.habit_form_cd_delete
import habitlock.composeapp.generated.resources.habit_form_label_description
import habitlock.composeapp.generated.resources.habit_form_label_interval
import habitlock.composeapp.generated.resources.habit_form_label_name
import habitlock.composeapp.generated.resources.habit_form_label_quota
import habitlock.composeapp.generated.resources.habit_form_label_target_value
import habitlock.composeapp.generated.resources.habit_form_label_unit
import habitlock.composeapp.generated.resources.habit_form_placeholder_quota
import habitlock.composeapp.generated.resources.habit_form_placeholder_unit
import habitlock.composeapp.generated.resources.habit_form_quota_supporting_daily
import habitlock.composeapp.generated.resources.habit_form_quota_supporting_weekly
import habitlock.composeapp.generated.resources.habit_form_reminder_info_periodic
import habitlock.composeapp.generated.resources.habit_form_reminder_info_time
import habitlock.composeapp.generated.resources.habit_form_schedule_daily_description
import habitlock.composeapp.generated.resources.habit_form_schedule_weekly_description
import habitlock.composeapp.generated.resources.habit_form_section_reminder
import habitlock.composeapp.generated.resources.habit_form_section_schedule
import habitlock.composeapp.generated.resources.habit_form_section_target
import habitlock.composeapp.generated.resources.habit_form_section_type
import habitlock.composeapp.generated.resources.habit_form_title_create
import habitlock.composeapp.generated.resources.habit_form_title_edit
import habitlock.composeapp.generated.resources.habit_form_type_binary_description
import habitlock.composeapp.generated.resources.habit_form_type_binary_label
import habitlock.composeapp.generated.resources.habit_form_type_quantitative_description
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormScreen(
    state: HabitFormState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (HabitType) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onScheduleTypeChange: (ScheduleType) -> Unit,
    onQuotaChange: (String) -> Unit,
    onHasReminderChange: (Boolean) -> Unit,
    onReminderTypeChange: (ReminderType) -> Unit,
    onIntervalChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditing) stringResource(Res.string.habit_form_title_edit) else stringResource(Res.string.habit_form_title_create))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_cd_back))
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.habit_form_cd_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(Res.string.habit_form_label_name)) },
                    placeholder = { Text(stringResource(Res.string.common_placeholder_habit_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(Res.string.habit_form_label_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Type selection
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(Res.string.habit_form_section_type),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.selectableGroup()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = state.type == HabitType.BINARY,
                                        onClick = { onTypeChange(HabitType.BINARY) },
                                        role = Role.RadioButton
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = state.type == HabitType.BINARY,
                                    onClick = null
                                )
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Text(stringResource(Res.string.habit_form_type_binary_label), style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        stringResource(Res.string.habit_form_type_binary_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = state.type == HabitType.QUANTITATIVE,
                                        onClick = { onTypeChange(HabitType.QUANTITATIVE) },
                                        role = Role.RadioButton
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = state.type == HabitType.QUANTITATIVE,
                                    onClick = null
                                )
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Text(stringResource(Res.string.common_quantitative), style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        stringResource(Res.string.habit_form_type_quantitative_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Quantitative options
                if (state.type == HabitType.QUANTITATIVE) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(Res.string.habit_form_section_target),
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = state.targetValue,
                                    onValueChange = onTargetValueChange,
                                    label = { Text(stringResource(Res.string.habit_form_label_target_value)) },
                                    placeholder = { Text(stringResource(Res.string.common_placeholder_target_value)) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = state.unit,
                                    onValueChange = onUnitChange,
                                    label = { Text(stringResource(Res.string.habit_form_label_unit)) },
                                    placeholder = { Text(stringResource(Res.string.habit_form_placeholder_unit)) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Schedule/Cadence selection
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(Res.string.habit_form_section_schedule),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.selectableGroup()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = state.scheduleType == ScheduleType.DAILY,
                                        onClick = { onScheduleTypeChange(ScheduleType.DAILY) },
                                        role = Role.RadioButton
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = state.scheduleType == ScheduleType.DAILY,
                                    onClick = null
                                )
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Text(stringResource(Res.string.common_daily), style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        stringResource(Res.string.habit_form_schedule_daily_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = state.scheduleType == ScheduleType.WEEKLY,
                                        onClick = { onScheduleTypeChange(ScheduleType.WEEKLY) },
                                        role = Role.RadioButton
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = state.scheduleType == ScheduleType.WEEKLY,
                                    onClick = null
                                )
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Text(stringResource(Res.string.common_weekly), style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        stringResource(Res.string.habit_form_schedule_weekly_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.quota,
                            onValueChange = onQuotaChange,
                            label = { Text(stringResource(Res.string.habit_form_label_quota)) },
                            placeholder = { Text(stringResource(Res.string.habit_form_placeholder_quota)) },
                            supportingText = {
                                Text(
                                    if (state.scheduleType == ScheduleType.DAILY) {
                                        stringResource(Res.string.habit_form_quota_supporting_daily)
                                    } else {
                                        stringResource(Res.string.habit_form_quota_supporting_weekly)
                                    }
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Reminder settings
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.habit_form_section_reminder),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Switch(
                                checked = state.hasReminder,
                                onCheckedChange = onHasReminderChange
                            )
                        }

                        if (state.hasReminder) {
                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.type == HabitType.QUANTITATIVE) {
                                Text(
                                    text = stringResource(Res.string.habit_form_reminder_info_periodic),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = state.intervalMinutes,
                                    onValueChange = onIntervalChange,
                                    label = { Text(stringResource(Res.string.habit_form_label_interval)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = stringResource(Res.string.habit_form_reminder_info_time),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // Time picker would go here - simplified for now
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.isValid && !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator()
                    } else {
                        Text(if (state.isEditing) stringResource(Res.string.habit_form_button_save) else stringResource(Res.string.habit_form_button_create))
                    }
                }
            }
        }
    }
}

