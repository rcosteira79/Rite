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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.habitlock.di.LocalAppComponent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.UndoPolicy

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onArchivedHabitsClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.settingsViewModel
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
                is SettingsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
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
        onArchivedHabitsClick = onArchivedHabitsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onUndoPolicyChange: (UndoPolicy) -> Unit,
    onMaxSnoozeDurationChange: (Int) -> Unit,
    onMaxSnoozesPerDayChange: (Int?) -> Unit,
    onMaxConsecutiveSkipsChange: (Int?) -> Unit,
    onArchivedHabitsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Undo Policy Section
                SettingsSection(title = "Undo Policy") {
                    Column(modifier = Modifier.selectableGroup()) {
                        UndoPolicyOption(
                            title = "Disabled",
                            description = "No undo allowed",
                            selected = state.undoPolicy == UndoPolicy.NONE,
                            onClick = { onUndoPolicyChange(UndoPolicy.NONE) }
                        )
                        UndoPolicyOption(
                            title = "Today Only",
                            description = "Can undo actions from today",
                            selected = state.undoPolicy == UndoPolicy.TODAY_ONLY,
                            onClick = { onUndoPolicyChange(UndoPolicy.TODAY_ONLY) }
                        )
                        UndoPolicyOption(
                            title = "All History",
                            description = "Can undo any past action",
                            selected = state.undoPolicy == UndoPolicy.ALL_HISTORY,
                            onClick = { onUndoPolicyChange(UndoPolicy.ALL_HISTORY) }
                        )
                    }
                }

                HorizontalDivider()

                // Snooze Settings Section
                SettingsSection(title = "Snooze Settings") {
                    Column {
                        Text(
                            text = "Max snooze duration: ${state.maxSnoozeDurationMinutes} minutes",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        var sliderValue by remember(state.maxSnoozeDurationMinutes) {
                            mutableFloatStateOf(state.maxSnoozeDurationMinutes.toFloat())
                        }

                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = { onMaxSnoozeDurationChange(sliderValue.toInt()) },
                            valueRange = 5f..60f,
                            steps = 10
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Unlimited snoozes",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (state.maxSnoozesPerHabitPerDay == null) "Enabled"
                                           else "Limited to ${state.maxSnoozesPerHabitPerDay} per habit/day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.maxSnoozesPerHabitPerDay == null,
                                onCheckedChange = { unlimited ->
                                    onMaxSnoozesPerDayChange(if (unlimited) null else 3)
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Skip Settings Section
                SettingsSection(title = "Skip Settings") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Unlimited skips",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (state.maxConsecutiveSkips == null) "Enabled"
                                       else "Limited to ${state.maxConsecutiveSkips} consecutive days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.maxConsecutiveSkips == null,
                            onCheckedChange = { unlimited ->
                                onMaxConsecutiveSkipsChange(if (unlimited) null else 2)
                            }
                        )
                    }
                }

                HorizontalDivider()

                // Info Section
                SettingsSection(title = "Info") {
                    Column {
                        Text(
                            text = "Current timezone",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = state.currentTimezone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // Archived Habits Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onArchivedHabitsClick)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Archived Habits",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

