package com.ricardocosteira.habitlock.domain.models

/**
 * Policy determining how far back undo operations can reach.
 */
enum class UndoPolicy {
    /** No undo allowed */
    NONE,
    
    /** Undo only for today's actions */
    TODAY_ONLY,
    
    /** Undo allowed for all history */
    ALL_HISTORY
}

