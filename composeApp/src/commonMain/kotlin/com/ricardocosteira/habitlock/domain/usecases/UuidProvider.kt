package com.ricardocosteira.habitlock.domain.usecases

/**
 * Platform-independent UUID provider interface.
 */
interface UuidProvider {
    fun generate(): String
}


