package com.ricardocosteira.rite.presentation.ui.today

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ricardocosteira.rite.presentation.ui.theme.RiteAppTheme

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
        DELETE -> RiteAppTheme.colors.errorContainer
        EDIT -> RiteAppTheme.colors.secondaryContainer
        REST -> RiteAppTheme.colors.background
    }

    @Composable
    fun iconTint(): Color = when (this) {
        DELETE -> RiteAppTheme.colors.onErrorContainer
        EDIT -> RiteAppTheme.colors.onSecondaryContainer
        REST -> RiteAppTheme.colors.onSurface
    }
}
