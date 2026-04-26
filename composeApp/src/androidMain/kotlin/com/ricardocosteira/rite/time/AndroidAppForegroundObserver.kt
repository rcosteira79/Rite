package com.ricardocosteira.rite.time

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ricardocosteira.rite.domain.time.AppForegroundObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Emits when the application process returns to the foreground.
 *
 * Must be constructed on the main thread (Application.onCreate is on main, so
 * constructing this observer there is safe). The first ON_RESUME the
 * [ProcessLifecycleOwner] sees after construction also produces an emission.
 */
class AndroidAppForegroundObserver :
    AppForegroundObserver,
    DefaultLifecycleObserver {
    private val _onForeground = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val onForeground: SharedFlow<Unit> = _onForeground.asSharedFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        _onForeground.tryEmit(Unit)
    }
}
