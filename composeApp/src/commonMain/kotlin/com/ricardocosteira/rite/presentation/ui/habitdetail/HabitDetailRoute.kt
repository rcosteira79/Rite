package com.ricardocosteira.rite.presentation.ui.habitdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.rite.di.LocalAppComponent

@Composable
fun HabitDetailRoute(
    instanceId: String,
    onNavigateBack: () -> Unit,
    onCustomProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val factory = LocalAppComponent.current.habitDetailViewModelFactory
    val viewModel = remember { factory.create(instanceId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    HabitDetailScreen(
        state = state,
        onBackClick = onNavigateBack,
        onComplete = viewModel::completeBinary,
        onIncrementProgress = viewModel::incrementProgress,
        onCustomProgress = onCustomProgress,
        onSkip = viewModel::skip,
        modifier = modifier
    )
}
