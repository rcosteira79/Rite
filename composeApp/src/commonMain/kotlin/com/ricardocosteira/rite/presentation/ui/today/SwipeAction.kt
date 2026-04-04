package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class SwipeAction(
    val unarmedIcon: ImageVector?,
    val armedIcon: ImageVector?,
    val alignment: Alignment
) {
    REST(
        unarmedIcon = null,
        armedIcon = null,
        alignment = Alignment.CenterEnd
    ),
    DELETE(
        unarmedIcon = Icons.Outlined.Delete,
        armedIcon = Icons.Filled.DeleteForever,
        alignment = Alignment.CenterStart
    ),
    EDIT(
        unarmedIcon = Icons.Outlined.Edit,
        armedIcon = Icons.Filled.Edit,
        alignment = Alignment.CenterEnd
    );

    @Composable
    fun backgroundColor(): Color = when (this) {
        DELETE -> MaterialTheme.colorScheme.errorContainer
        EDIT -> MaterialTheme.colorScheme.secondaryContainer
        REST -> MaterialTheme.colorScheme.surface
    }

    @Composable
    fun iconTint(): Color = when (this) {
        DELETE -> MaterialTheme.colorScheme.onErrorContainer
        EDIT -> MaterialTheme.colorScheme.onSecondaryContainer
        REST -> MaterialTheme.colorScheme.onSurface
    }
}
