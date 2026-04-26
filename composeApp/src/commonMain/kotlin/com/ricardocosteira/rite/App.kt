package com.ricardocosteira.rite

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ricardocosteira.rite.di.LocalAppComponent
import com.ricardocosteira.rite.di.RiteAppComponent
import com.ricardocosteira.rite.presentation.navigation.RiteNavigation
import com.ricardocosteira.rite.presentation.ui.splash.RiteSplashScreen
import com.ricardocosteira.rite.presentation.ui.startup.StartupState
import com.ricardocosteira.rite.presentation.ui.theme.RiteTheme

@Composable
fun App(appComponent: RiteAppComponent) {
    RiteTheme {
        val startupState by appComponent.startupViewModel.state.collectAsStateWithLifecycle()
        // Only run the splash on cold starts. On warm starts / config changes the
        // singleton StartupViewModel already reports Ready, so we skip straight in.
        val coldStart = remember { startupState is StartupState.Loading }
        var splashAnimationComplete by remember { mutableStateOf(!coldStart) }

        val readyState = startupState as? StartupState.Ready
        val ready = splashAnimationComplete && readyState != null

        Crossfade(
            targetState = ready,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        ) { isReady ->
            if (!isReady) {
                RiteSplashScreen(onAnimationComplete = { splashAnimationComplete = true })
            } else {
                CompositionLocalProvider(LocalAppComponent provides appComponent) {
                    RiteNavigation(
                        isOnboardingCompleted = readyState!!.isOnboardingCompleted
                    )
                }
            }
        }
    }
}

expect fun generateUuid(): String
