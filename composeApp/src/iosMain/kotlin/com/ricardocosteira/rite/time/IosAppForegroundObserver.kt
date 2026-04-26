package com.ricardocosteira.rite.time

import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification

/**
 * Emits when the app receives `UIApplicationDidBecomeActiveNotification`.
 *
 * The observer holds an [NSObjectProtocol] token from `addObserverForName`.
 * No `removeObserver` call is wired because instances live for the entire
 * process lifetime (created by AppComponentFactory on app launch).
 */
class IosAppForegroundObserver : AppForegroundObserver {
    private val _onForeground = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val onForeground: SharedFlow<Unit> = _onForeground.asSharedFlow()

    init {
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            _onForeground.tryEmit(Unit)
        }
    }
}
