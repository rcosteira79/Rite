package com.ricardocosteira.rite.domain.time

import kotlinx.coroutines.flow.SharedFlow

/**
 * Platform-agnostic signal that the app has come to the foreground.
 *
 * - Android: backed by [androidx.lifecycle.ProcessLifecycleOwner] ON_RESUME.
 * - iOS: backed by `UIApplicationDidBecomeActiveNotification`.
 * - JVM (desktop): emits once at construction; desktop has no equivalent app-level
 *   foreground signal worth wiring for this app's needs.
 *
 * Consumers should treat each emission as a hint to re-evaluate time-sensitive state
 * (e.g. "is today still today?"). Idempotent re-evaluation is the implementor's
 * responsibility, not this interface's.
 */
interface AppForegroundObserver {
    val onForeground: SharedFlow<Unit>
}
