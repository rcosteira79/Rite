package com.ricardocosteira.habitlock.presentation.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.di.LocalAppComponent
import habitlock.composeapp.generated.resources.Res
import habitlock.composeapp.generated.resources.common_continue
import habitlock.composeapp.generated.resources.common_skip
import habitlock.composeapp.generated.resources.philosophy_body
import habitlock.composeapp.generated.resources.philosophy_heading
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhilosophyScreen(
    onNavigateToStrictness: () -> Unit,
    onNavigateToToday: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = LocalAppComponent.current.onboardingViewModel

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToStrictness -> onNavigateToStrictness()
                OnboardingEvent.NavigateToFirstHabit -> Unit  // not reachable from PhilosophyScreen
                OnboardingEvent.NavigateToToday -> onNavigateToToday()
                is OnboardingEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    PhilosophyScreen(
        onContinue = viewModel::continueFromPhilosophy,
        onSkip = viewModel::skipToToday
    )
}

@Composable
private fun PhilosophyScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.philosophy_heading),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(Res.string.philosophy_body),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.common_continue))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(onClick = onSkip) {
            Text(stringResource(Res.string.common_skip))
        }
    }
}

