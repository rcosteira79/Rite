package com.ricardocosteira.rite.domain.usecases

/**
 * Platform-independent UUID provider interface.
 */
interface UuidProvider {
    fun generate(): String
}


