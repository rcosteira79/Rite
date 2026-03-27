package com.ricardocosteira.habitlock.presentation.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import com.ricardocosteira.habitlock.domain.models.UndoPolicy
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_cd_back
import habitlock.composeapp.generated.resources.common_error_generic
import habitlock.composeapp.generated.resources.settings_archived_habits
import habitlock.composeapp.generated.resources.settings_info_timezone_label
import habitlock.composeapp.generated.resources.settings_section_info
import habitlock.composeapp.generated.resources.settings_section_skip
import habitlock.composeapp.generated.resources.settings_section_snooze
import habitlock.composeapp.generated.resources.settings_section_undo_policy
import habitlock.composeapp.generated.resources.settings_skip_status_limited
import habitlock.composeapp.generated.resources.settings_skip_status_unlimited
import habitlock.composeapp.generated.resources.settings_skip_unlimited_label
import habitlock.composeapp.generated.resources.settings_snooze_max_duration
import habitlock.composeapp.generated.resources.settings_snooze_status_limited
import habitlock.composeapp.generated.resources.settings_snooze_status_unlimited
import habitlock.composeapp.generated.resources.settings_snooze_unlimited_label
import habitlock.composeapp.generated.resources.settings_success_daily_summary_updated
import habitlock.composeapp.generated.resources.settings_success_saved
import habitlock.composeapp.generated.resources.settings_title
import habitlock.composeapp.generated.resources.settings_undo_all_description
import habitlock.composeapp.generated.resources.settings_undo_all_label
import habitlock.composeapp.generated.resources.settings_undo_disabled_description
import habitlock.composeapp.generated.resources.settings_undo_disabled_label
import habitlock.composeapp.generated.resources.settings_undo_today_description
import habitlock.composeapp.generated.resources.settings_undo_today_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onArchivedHabitsClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = LocalAppComponent.current.settingsViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    val messageSettingsSaved = stringResource(Res.string.settings_success_saved)
    val messageDailySummaryUpdated = stringResource(Res.string.settings_success_daily_summary_updated)
    val messageGenericError = stringResource(Res.string.common_error_generic)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.SettingsSaved -> snackbarHostState.showSnackbar(messageSettingsSaved)
                SettingsEvent.DailySummaryUpdated -> snackbarHostState.showSnackbar(messageDailySummaryUpdated)
                is SettingsEvent.ShowError -> snackbarHostState.showSnackbar(event.message ?: messageGenericError)
            }
        }
    }

    SettingsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onUndoPolicyChange = viewModel::updateUndoPolicy,
        onMaxSnoozeDurationChange = viewModel::updateMaxSnoozeDuration,
        onMaxSnoozesPerDayChange = viewModel::updateMaxSnoozesPerDay,
        onMaxConsecutiveSkipsChange = viewModel::updateMaxConsecutiveSkips,
        onArchivedHabitsClick = onArchivedHabitsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SettingsState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onUndoPolicyChange: (UndoPolicy) -> Unit,
    onMaxSnoozeDurationChange: (Int) -> Unit,
    onMaxSnoozesPerDayChange: (Int?) -> Unit,
    onMaxConsecutiveSkipsChange: (Int?) -> Unit,
    onArchivedHabitsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
            ) {
                // Undo Policy Section
                SettingsSection(title = stringResource(Res.string.settings_section_undo_policy)) {
                    Column(modifier = Modifier.selectableGroup()) {
                        UndoPolicyOption(
                            title = stringResource(Res.string.settings_undo_disabled_label),
                            description = stringResource(Res.string.settings_undo_disabled_description),
                            selected = state.undoPolicy == UndoPolicy.NONE,
                            onClick = { onUndoPolicyChange(UndoPolicy.NONE) },
                        )
                        UndoPolicyOption(
                            title = stringResource(Res.string.settings_undo_today_label),
                            description = stringResource(Res.string.settings_undo_today_description),
                            selected = state.undoPolicy == UndoPolicy.TODAY_ONLY,
                            onClick = { onUndoPolicyChange(UndoPolicy.TODAY_ONLY) },
                        )
                        UndoPolicyOption(
                            title = stringResource(Res.string.settings_undo_all_label),
                            description = stringResource(Res.string.settings_undo_all_description),
                            selected = state.undoPolicy == UndoPolicy.ALL_HISTORY,
                            onClick = { onUndoPolicyChange(UndoPolicy.ALL_HISTORY) },
                        )
                    }
                }

                HorizontalDivider()

                // Snooze Settings Section
                SettingsSection(title = stringResource(Res.string.settings_section_snooze)) {
                    Column {
                        Text(
                            text = stringResource(Res.string.settings_snooze_max_duration, state.maxSnoozeDurationMinutes),
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        var sliderValue by remember(state.maxSnoozeDurationMinutes) {
                            mutableFloatStateOf(state.maxSnoozeDurationMinutes.toFloat())
                        }

                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = { onMaxSnoozeDurationChange(sliderValue.toInt()) },
                            valueRange = 5f..60f,
                            steps = 10,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = stringResource(Res.string.settings_snooze_unlimited_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text =
                                        if (state.maxSnoozesPerHabitPerDay ==
                                            null
                                        ) {
                                            stringResource(Res.string.settings_snooze_status_unlimited)
                                        } else {
                                            stringResource(Res.string.settings_snooze_status_limited, state.maxSnoozesPerHabitPerDay)
                                        },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = state.maxSnoozesPerHabitPerDay == null,
                                onCheckedChange = { unlimited ->
                                    onMaxSnoozesPerDayChange(if (unlimited) null else 3)
                                },
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Skip Settings Section
                SettingsSection(title = stringResource(Res.string.settings_section_skip)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = stringResource(Res.string.settings_skip_unlimited_label),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text =
                                    if (state.maxConsecutiveSkips == null) {
                                        stringResource(Res.string.settings_skip_status_unlimited)
                                    } else {
                                        stringResource(Res.string.settings_skip_status_limited, state.maxConsecutiveSkips)
                                    },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = state.maxConsecutiveSkips == null,
                            onCheckedChange = { unlimited ->
                                onMaxConsecutiveSkipsChange(if (unlimited) null else 2)
                            },
                        )
                    }
                }

                HorizontalDivider()

                // Info Section
                SettingsSection(title = stringResource(Res.string.settings_section_info)) {
                    Column {
                        Text(
                            text = stringResource(Res.string.settings_info_timezone_label),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = state.currentTimezone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                HorizontalDivider()

                // Archived Habits Link
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onArchivedHabitsClick)
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.settings_archived_habits),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun UndoPolicyOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
