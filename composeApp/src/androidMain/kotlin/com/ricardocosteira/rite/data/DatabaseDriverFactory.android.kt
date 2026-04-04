package com.ricardocosteira.rite.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ricardocosteira.rite.data.database.HabitLockDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = HabitLockDatabase.Schema,
            context = context,
            name = "habitlock.db"
        )
    }
}

