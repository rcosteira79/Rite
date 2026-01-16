package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.domain.models.HabitStatus
import com.ricardocosteira.habitlock.domain.models.HabitType
import com.ricardocosteira.habitlock.presentation.models.TodayHabitUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    state: TodayState,
    onMenuClick: () -> Unit,
    onHabitClick: (String) -> Unit,
    onCompleteClick: (String) -> Unit,
    onSkipClick: (String) -> Unit,
    onUndoClick: (String) -> Unit,
    onArchiveClick: (String) -> Unit,
    onAddHabitClick: () -> Unit,
    onDismissTimezoneWarning: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Today") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHabitClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Timezone warning banner
            if (state.showTimezoneWarning) {
                TimezoneWarningBanner(
                    previousTimezone = state.previousTimezone,
                    onDismiss = onDismissTimezoneWarning
                )
            }
            
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.habits.isEmpty() -> {
                    EmptyHabitsMessage(onAddHabitClick = onAddHabitClick)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.habits,
                            key = { it.instanceId }
                        ) { habit ->
                            HabitCard(
                                habit = habit,
                                onClick = { onHabitClick(habit.instanceId) },
                                onCompleteClick = { onCompleteClick(habit.instanceId) },
                                onSkipClick = { onSkipClick(habit.instanceId) },
                                onUndoClick = { onUndoClick(habit.instanceId) },
                                onArchiveClick = { onArchiveClick(habit.habitId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimezoneWarningBanner(
    previousTimezone: String?,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Timezone changed",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Your timezone has changed from $previousTimezone. Past data remains unchanged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun EmptyHabitsMessage(onAddHabitClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No habits for today",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your first habit to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onAddHabitClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Habit")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitCard(
    habit: TodayHabitUiModel,
    onClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onSkipClick: () -> Unit,
    onUndoClick: () -> Unit,
    onArchiveClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val cardColor = when (habit.status) {
        HabitStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        HabitStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        HabitStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        HabitStatus.PENDING -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else null,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (habit.description != null) {
                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Streak info
                        if (habit.currentStreak > 0) {
                            Text(
                                text = "🔥 ${habit.currentStreak} day streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Action buttons
                    when (habit.status) {
                        HabitStatus.PENDING -> {
                            Row {
                                if (!habit.isSkipLocked) {
                                    TextButton(onClick = onSkipClick) {
                                        Text("Skip")
                                    }
                                }
                                IconButton(onClick = onCompleteClick) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Complete",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        HabitStatus.COMPLETED, HabitStatus.SKIPPED -> {
                            IconButton(onClick = onUndoClick) {
                                Icon(
                                    Icons.Default.Undo,
                                    contentDescription = "Undo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HabitStatus.FAILED -> {
                            Text(
                                text = "Failed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                // Progress bar for quantitative habits
                if (habit.type == HabitType.QUANTITATIVE && habit.targetValue != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { habit.progressPercentage },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = habit.progressText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Context menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Archive") },
                    onClick = {
                        showMenu = false
                        onArchiveClick()
                    }
                )
            }
        }
    }
}

