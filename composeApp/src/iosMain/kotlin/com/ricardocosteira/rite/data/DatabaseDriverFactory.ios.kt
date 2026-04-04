package com.ricardocosteira.rite.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.ricardocosteira.rite.data.database.HabitLockDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = HabitLockDatabase.Schema,
            name = "habitlock.db"
        )
    }
}

