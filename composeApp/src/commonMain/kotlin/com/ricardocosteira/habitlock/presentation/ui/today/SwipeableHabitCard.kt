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
    DELETE,
    EDIT
}

private const val DELETE_ANCHOR_FRACTION = 0.4f
private const val EDIT_ANCHOR_FRACTION = -0.4f
private const val FULL_OPACITY_FRACTION = 0.15f
internal val CORNER_RADIUS = 16.dp

@Composable
internal fun SwipeBackground(
    zone: SwipeAction,
    revealFraction: Float,
    modifier: Modifier = Modifier
) {
    val alpha: Float = (revealFraction / FULL_OPACITY_FRACTION).coerceIn(0f, 1f)

    val zoneColor: Color = when (zone) {
        SwipeAction.DELETE -> MaterialTheme.colorScheme.errorContainer
        SwipeAction.EDIT -> MaterialTheme.colorScheme.secondaryContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.surface
    }

    val iconTint: Color = when (zone) {
        SwipeAction.DELETE -> MaterialTheme.colorScheme.onErrorContainer
        SwipeAction.EDIT -> MaterialTheme.colorScheme.onSecondaryContainer
        SwipeAction.REST -> MaterialTheme.colorScheme.onSurface
    }

    val icon: ImageVector? = when (zone) {
        SwipeAction.DELETE -> Icons.Filled.DeleteForever
        SwipeAction.EDIT -> Icons.Outlined.Edit
        SwipeAction.REST -> null
    }

    val alignment: Alignment = when (zone) {
        SwipeAction.DELETE -> Alignment.CenterStart
        SwipeAction.EDIT -> Alignment.CenterEnd
        SwipeAction.REST -> Alignment.CenterEnd
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CORNER_RADIUS))
            .background(zoneColor.copy(alpha = alpha))
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint.copy(alpha = alpha),
                modifier = Modifier
                    .align(alignment)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun SwipeableHabitCard(
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
                if (!hasDragged) newValue == SwipeAction.REST else true
            }
        )
    }

    if (cardWidth > 0f) {
        val anchors: DraggableAnchors<SwipeAction> = DraggableAnchors {
            SwipeAction.REST at 0f
            SwipeAction.DELETE at cardWidth * DELETE_ANCHOR_FRACTION
            SwipeAction.EDIT at cardWidth * EDIT_ANCHOR_FRACTION
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

    // Derive the visual zone and reveal fraction from the offset
    val currentZone: SwipeAction by remember {
        derivedStateOf {
            val offset: Float = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f
            when {
                offset > 0f -> SwipeAction.DELETE
                offset < 0f -> SwipeAction.EDIT
                else -> SwipeAction.REST
            }
        }
    }

    // How far the card has been dragged as a fraction of its width (0..1)
    val revealFraction: Float by remember {
        derivedStateOf {
            val offset: Float = anchoredDraggableState.offset.takeIf { !it.isNaN() } ?: 0f
            val width: Float = cardWidth
            if (width > 0f) abs(offset) / width else 0f
        }
    }

    // Haptic feedback on zone entry
    LaunchedEffect(Unit) {
        snapshotFlow { currentZone }
            .distinctUntilChanged()
            .filter { it != SwipeAction.REST }
            .collect { zone: SwipeAction ->
                when (zone) {
                    SwipeAction.DELETE -> hapticController.heavyClick()
                    SwipeAction.EDIT -> hapticController.click()
                    SwipeAction.REST -> { /* no haptic */ }
                }
            }
    }

    // Handle settle at anchors
    LaunchedEffect(Unit) {
        snapshotFlow { anchoredDraggableState.currentValue }
            .distinctUntilChanged()
            .filter { it != SwipeAction.REST }
            .collect { action: SwipeAction ->
                when (action) {
                    SwipeAction.DELETE -> onDelete()

                    SwipeAction.EDIT -> {
                        anchoredDraggableState.animateTo(SwipeAction.REST)
                        onEdit()
                    }

                    SwipeAction.REST -> { /* filtered */ }
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
            revealFraction = revealFraction,
            modifier = Modifier.matchParentSize()
        )

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
