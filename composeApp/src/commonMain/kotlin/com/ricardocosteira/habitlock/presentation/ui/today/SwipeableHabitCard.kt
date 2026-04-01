package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ricardocosteira.habitlock.presentation.ui.haptics.HapticController
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

enum class SwipeAction {
    REST,
    ARCHIVE,
    EDIT,
    DELETE
}

private const val ARCHIVE_THRESHOLD_FRACTION = 0.3f
private const val EDIT_THRESHOLD_FRACTION = -0.3f
private const val DELETE_THRESHOLD_FRACTION = -0.6f
private val CORNER_RADIUS = 16.dp

@Composable
fun SwipeableHabitCard(
    onArchive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    hapticController: HapticController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var cardWidth: Float by remember { mutableStateOf(0f) }

    val anchoredDraggableState: AnchoredDraggableState<SwipeAction> = remember {
        AnchoredDraggableState(initialValue = SwipeAction.REST)
    }

    // Update anchors when card width changes
    LaunchedEffect(cardWidth) {
        if (cardWidth > 0f) {
            anchoredDraggableState.updateAnchors(
                DraggableAnchors {
                    SwipeAction.REST at 0f
                    SwipeAction.ARCHIVE at cardWidth * ARCHIVE_THRESHOLD_FRACTION
                    SwipeAction.EDIT at cardWidth * EDIT_THRESHOLD_FRACTION
                    SwipeAction.DELETE at cardWidth * DELETE_THRESHOLD_FRACTION
                }
            )
        }
    }

    // currentOffset reads Compose state (anchoredDraggableState.offset) so it is reactive
    val currentOffset: Float = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f

    // Derive the active swipe zone from the offset using derivedStateOf so snapshotFlow can track it
    val currentZone: SwipeAction by remember {
        derivedStateOf {
            val offset = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f
            val width = cardWidth
            when {
                offset > 0f && width > 0f &&
                    offset >= width * ARCHIVE_THRESHOLD_FRACTION * 0.5f -> SwipeAction.ARCHIVE

                offset < 0f && width > 0f &&
                    abs(
                        offset
                    ) >= width * abs(DELETE_THRESHOLD_FRACTION) * 0.75f -> SwipeAction.DELETE

                offset < 0f && width > 0f &&
                    abs(offset) >= width * abs(EDIT_THRESHOLD_FRACTION) * 0.5f -> SwipeAction.EDIT

                else -> SwipeAction.REST
            }
        }
    }

    val backgroundColor = when (currentZone) {
        SwipeAction.ARCHIVE -> MaterialTheme.colorScheme.surfaceContainerHighest
        SwipeAction.EDIT -> MaterialTheme.colorScheme.secondaryContainer
        SwipeAction.DELETE -> MaterialTheme.colorScheme.errorContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.surface
    }

    val iconTint = when (currentZone) {
        SwipeAction.ARCHIVE -> MaterialTheme.colorScheme.onSurface
        SwipeAction.EDIT -> MaterialTheme.colorScheme.onSecondaryContainer
        SwipeAction.DELETE -> MaterialTheme.colorScheme.onErrorContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.onSurface
    }

    val icon: ImageVector? = when (currentZone) {
        SwipeAction.ARCHIVE -> Icons.Outlined.Inventory2
        SwipeAction.EDIT -> Icons.Outlined.Edit
        SwipeAction.DELETE -> Icons.Filled.DeleteForever
        SwipeAction.REST -> null
    }

    // Haptic feedback on zone entry — snapshotFlow tracks derivedStateOf correctly
    LaunchedEffect(anchoredDraggableState) {
        snapshotFlow { currentZone }
            .distinctUntilChanged()
            .filter { it != SwipeAction.REST }
            .collect { zone: SwipeAction ->
                when (zone) {
                    SwipeAction.ARCHIVE -> hapticController.tick()
                    SwipeAction.EDIT -> hapticController.click()
                    SwipeAction.DELETE -> hapticController.heavyClick()
                    SwipeAction.REST -> { /* no haptic */ }
                }
            }
    }

    // Handle settle at non-rest anchors
    LaunchedEffect(anchoredDraggableState) {
        snapshotFlow { anchoredDraggableState.currentValue }
            .distinctUntilChanged()
            .filter { it != SwipeAction.REST }
            .collect { action: SwipeAction ->
                when (action) {
                    SwipeAction.ARCHIVE -> onArchive()

                    SwipeAction.EDIT -> {
                        anchoredDraggableState.animateTo(SwipeAction.REST)
                        onEdit()
                    }

                    SwipeAction.DELETE -> onDelete()

                    SwipeAction.REST -> { /* handled by filter */ }
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size -> cardWidth = size.width.toFloat() }
            .clip(RoundedCornerShape(CORNER_RADIUS))
            .background(backgroundColor)
    ) {
        // Background icon
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .align(
                        if (currentOffset > 0f) Alignment.CenterStart else Alignment.CenterEnd
                    )
                    .padding(horizontal = 24.dp)
            )
        }

        // Foreground card content
        Box(
            modifier = Modifier
                .offset { IntOffset(x = currentOffset.roundToInt(), y = 0) }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Horizontal
                )
        ) {
            content()
        }
    }
}
