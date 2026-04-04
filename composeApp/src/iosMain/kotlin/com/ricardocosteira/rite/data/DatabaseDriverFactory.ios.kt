package com.ricardocosteira.rite.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.ricardocosteira.rite.data.database.RiteDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = RiteDatabase.Schema,
            name = "habitlock.db"
        )
    }
}

