package com.ricardocosteira.habitlock.data

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating platform-specific SQLDelight drivers.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
