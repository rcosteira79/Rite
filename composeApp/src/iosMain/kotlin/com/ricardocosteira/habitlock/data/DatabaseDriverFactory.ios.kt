package com.ricardocosteira.habitlock.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.ricardocosteira.habitlock.data.database.HabitLockDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = HabitLockDatabase.Schema,
            name = "habitlock.db"
        )
    }
}

