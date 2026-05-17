package com.ricardocosteira.rite.time

import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Desktop has no app-level foreground signal worth wiring up — the JVM build is
 * primarily for hot-reload during development, not production. Emits once at
 * construction so the [com.ricardocosteira.rite.domain.time.CurrentDateProvider]
 * still gets a kick to recompute on app start.
 */
class JvmAppForegroundObserver : AppForegroundObserver {
    private val _onForeground = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
    override val onForeground: SharedFlow<Unit> = _onForeground.asSharedFlow()

    init {
        _onForeground.tryEmit(Unit)
    }
}
