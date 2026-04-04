package com.ricardocosteira.rite.presentation.ui.startup

sealed interface StartupState {
    data object Loading : StartupState
    data class Ready(val isOnboardingCompleted: Boolean) : StartupState
}
