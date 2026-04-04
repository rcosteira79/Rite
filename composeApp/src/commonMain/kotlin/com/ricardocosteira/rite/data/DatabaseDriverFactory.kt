package com.ricardocosteira.rite.data

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating platform-specific SQLDelight drivers.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
