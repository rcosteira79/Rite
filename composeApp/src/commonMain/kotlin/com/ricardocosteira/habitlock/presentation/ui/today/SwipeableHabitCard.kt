package com.ricardocosteira.habitlock.presentation.ui.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.ui.graphics.Color
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

private const val ARCHIVE_ANCHOR_FRACTION = 0.3f
private const val DELETE_ANCHOR_FRACTION = -0.6f
private const val EDIT_VISUAL_THRESHOLD_FRACTION = 0.15f
private const val DELETE_VISUAL_THRESHOLD_FRACTION = 0.45f
private const val COLOR_FADE_DURATION_MS = 200
internal val CORNER_RADIUS = 16.dp

@Composable
internal fun SwipeBackground(zone: SwipeAction, modifier: Modifier = Modifier) {
    val targetColor: Color = when (zone) {
        SwipeAction.ARCHIVE -> MaterialTheme.colorScheme.surfaceContainerHighest
        SwipeAction.EDIT -> MaterialTheme.colorScheme.secondaryContainer
        SwipeAction.DELETE -> MaterialTheme.colorScheme.errorContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.surface
    }

    val backgroundColor: Color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = COLOR_FADE_DURATION_MS),
        label = "swipeBackgroundColor"
    )

    val iconTint: Color = when (zone) {
        SwipeAction.ARCHIVE -> MaterialTheme.colorScheme.onSurface
        SwipeAction.EDIT -> MaterialTheme.colorScheme.onSecondaryContainer
        SwipeAction.DELETE -> MaterialTheme.colorScheme.onErrorContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.onSurface
    }

    val icon: ImageVector? = when (zone) {
        SwipeAction.ARCHIVE -> Icons.Outlined.Inventory2
        SwipeAction.EDIT -> Icons.Outlined.Edit
        SwipeAction.DELETE -> Icons.Filled.DeleteForever
        SwipeAction.REST -> null
    }

    val alignment: Alignment = when (zone) {
        SwipeAction.ARCHIVE -> Alignment.CenterStart
        else -> Alignment.CenterEnd
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CORNER_RADIUS))
            .background(backgroundColor)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .align(alignment)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

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
    var hasDragged: Boolean by remember { mutableStateOf(false) }

    val anchoredDraggableState: AnchoredDraggableState<SwipeAction> = remember {
        AnchoredDraggableState(
            initialValue = SwipeAction.REST,
            confirmValueChange = { newValue: SwipeAction ->
                if (!hasDragged) {
                    newValue == SwipeAction.REST
                } else {
                    true
                }
            }
        )
    }

    // Only REST, ARCHIVE, and DELETE are real anchors.
    // EDIT is a visual-only zone — no anchor, so the card can drag through it to DELETE.
    if (cardWidth > 0f) {
        val anchors: DraggableAnchors<SwipeAction> = DraggableAnchors {
            SwipeAction.REST at 0f
            SwipeAction.ARCHIVE at cardWidth * ARCHIVE_ANCHOR_FRACTION
            SwipeAction.DELETE at cardWidth * DELETE_ANCHOR_FRACTION
        }
        anchoredDraggableState.updateAnchors(anchors)
    }

    val currentOffset: Float = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f

    // Track that the user has started dragging
    LaunchedEffect(Unit) {
        snapshotFlow { anchoredDraggableState.offset }
            .filter { !it.isNaN() && it != 0f }
            .collect { hasDragged = true }
    }

    // Derive the visual zone from the offset for background color and haptic
    val currentZone: SwipeAction by remember {
        derivedStateOf {
            val offset: Float = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f
            val width: Float = cardWidth
            when {
                offset > 0f && width > 0f &&
                    offset >= width * EDIT_VISUAL_THRESHOLD_FRACTION -> SwipeAction.ARCHIVE

                offset < 0f && width > 0f &&
                    abs(offset) >= width * DELETE_VISUAL_THRESHOLD_FRACTION -> SwipeAction.DELETE

                offset < 0f && width > 0f &&
                    abs(offset) >= width * EDIT_VISUAL_THRESHOLD_FRACTION -> SwipeAction.EDIT

                else -> SwipeAction.REST
            }
        }
    }

    // Haptic feedback on zone entry
    LaunchedEffect(Unit) {
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

    // Handle settle at real anchors (ARCHIVE and DELETE only)
    LaunchedEffect(Unit) {
        snapshotFlow { anchoredDraggableState.currentValue }
            .distinctUntilChanged()
            .filter { it != SwipeAction.REST }
            .collect { action: SwipeAction ->
                when (action) {
                    SwipeAction.ARCHIVE -> onArchive()
                    SwipeAction.DELETE -> onDelete()
                    else -> { /* EDIT is not an anchor, REST filtered */ }
                }
            }
    }

    // Handle EDIT: when the card settles back at REST from the edit zone, fire onEdit.
    // Since EDIT has no anchor, releasing in the edit zone snaps back to REST.
    // We detect this by watching for REST settle while the visual zone was EDIT.
    LaunchedEffect(Unit) {
        snapshotFlow { anchoredDraggableState.currentValue }
            .collect { value: SwipeAction ->
                if (value == SwipeAction.REST && hasDragged && currentZone == SwipeAction.EDIT) {
                    // Card just snapped back from the edit zone
                    onEdit()
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size -> cardWidth = size.width.toFloat() }
    ) {
        SwipeBackground(
            zone = currentZone,
            modifier = Modifier.matchParentSize()
        )

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
