package com.ricardocosteira.habitlock.presentation.ui.components.toolbar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Stable
class CollapsingToolbarSpec(
    val state: TopAppBarState,
    val collapsedHeight: Dp,
    val scrollIsPinned: Boolean,
    private val scrollBehavior: TopAppBarScrollBehavior
) {
    val nestedScrollConnection = scrollBehavior.nestedScrollConnection
    val flingAnimationSpec = scrollBehavior.flingAnimationSpec
    val snapAnimationSpec = scrollBehavior.snapAnimationSpec

    var heightOffsetLimit: Float
        get() = scrollBehavior.state.heightOffsetLimit
        set(newOffset) {
            if (newOffset != scrollBehavior.state.heightOffsetLimit) {
                scrollBehavior.state.heightOffsetLimit = newOffset
            }
        }
}

/**
 * Creates a [CollapsingToolbarSpec] with exactly like exitUntilCollapsedBehavior, but with pinned scrolling so that
 * users can't scroll the toolbar itself.
 *
 * @param collapsedToolbarHeight The height of the toolbar when collapsed, not including window insets.
 * @param accountForFontScaling Whether to scale the collapsed height based on the user's font size settings.
 * @param canScroll A lambda that returns whether scrolling is currently allowed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pinnedExitUntilCollapsedToolbarSpec(
    collapsedToolbarHeight: Dp = 44.dp,
    accountForFontScaling: Boolean = true,
    canScroll: () -> Boolean = { true }
): CollapsingToolbarSpec {
    val density = LocalDensity.current
    val fontScale = density.fontScale.coerceAtLeast(1f)

    /**
     Because of the possibility of having a background composable in the toolbar, we can't apply top window insets
     before we set the min height on [com.waitrose.ui.toolbar.CollapsingToolbarLayout]'s modifier, as seen
     in [com.waitrose.ui.toolbar.DynamicCollapsingToolbar]. Otherwise, the background composable would appear
     underneath the status bar. That said, we account for top insets here so that the toolbar's height is correct,
     and then apply them individually to the elements that need them
     inside [com.waitrose.ui.toolbar.CollapsingToolbarLayout].
     */
    val topInset = with(density) { TopAppBarDefaults.windowInsets.getTop(density).toDp() }

    val collapsedHeight = if (accountForFontScaling) {
        collapsedToolbarHeight * fontScale
    } else {
        collapsedToolbarHeight
    }.run { this + topInset }

    val state = rememberTopAppBarState()

    return CollapsingToolbarSpec(
        state = state,
        collapsedHeight = collapsedHeight,
        scrollIsPinned = true,
        scrollBehavior = exitUntilCollapsedScrollBehavior(state, canScroll = canScroll)
    )
}
