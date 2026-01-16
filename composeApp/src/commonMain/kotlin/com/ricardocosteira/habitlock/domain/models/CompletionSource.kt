package com.ricardocosteira.habitlock.domain.models

/**
 * Source of a habit completion event.
 */
enum class CompletionSource {
    /** Completed from within the app UI */
    IN_APP,

    /** Completed from a notification action */
    NOTIFICATION
}

