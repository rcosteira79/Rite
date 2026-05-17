package com.ricardocosteira.rite.data.repositories

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.ricardocosteira.rite.data.database.RiteDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryObserveTest {

    @Test
    fun `observeUser emits null when no user exists, then emits user after insert`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
            RiteDatabase.Schema.create(it)
        }
        val database = RiteDatabase(driver)
        val repository = UserRepositoryImpl(database = database, ioDispatcher = testDispatcher)

        repository.observeUser().test {
            assertNull(awaitItem(), "Expected null before any user is created")
            repository.createDefaultUser(timezone = TimeZone.UTC)
            val emitted = awaitItem()
            assertEquals(
                TimeZone.UTC,
                emitted?.timezone,
                "Expected emission with new user's timezone"
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
