package com.ricardocosteira.rite.presentation.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

@Composable
fun RiteDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RiteAppTheme.shapes.lg,
        containerColor = RiteAppTheme.colors.surface,
        titleContentColor = RiteAppTheme.colors.onSurface,
        textContentColor = RiteAppTheme.colors.onSurfaceMuted,
        title = { Text(title, style = RiteAppTheme.typography.headlineSmall) },
        text = { Text(message, style = RiteAppTheme.typography.bodyMedium) },
        confirmButton = {
            // Destructive dialogs tint the confirm button with the error color directly.
            // Non-destructive dialogs use the default primary (ink) treatment.
            if (destructive) {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = RiteAppTheme.colors.error
                    )
                ) { Text(confirmLabel, style = RiteAppTheme.typography.labelLarge) }
            } else {
                PrimaryButton(onClick = onConfirm, variant = RiteButtonVariant.Primary) {
                    Text(confirmLabel)
                }
            }
        },
        dismissButton = {
            PrimaryButton(onClick = onDismiss, variant = RiteButtonVariant.Ghost) {
                Text(dismissLabel)
            }
        }
    )
}
