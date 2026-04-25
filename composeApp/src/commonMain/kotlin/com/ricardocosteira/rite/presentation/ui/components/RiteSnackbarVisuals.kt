package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

/**
 * Typed [SnackbarVisuals] carrying a [RiteSnackbarVariant] + pre-composed
 * [RiteSnackbarContent]. The root SnackbarHost unwraps these and renders
 * them via [RiteSnackbar].
 */
data class RiteSnackbarVisuals(
    val variant: RiteSnackbarVariant,
    val content: RiteSnackbarContent,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false
) : SnackbarVisuals {
    override val message: String
        get() = listOf(content.prefix, content.emphasized, content.suffix).joinToString("")
}
