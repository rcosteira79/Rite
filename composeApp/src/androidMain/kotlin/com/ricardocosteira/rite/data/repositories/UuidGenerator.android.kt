package com.ricardocosteira.rite.data.repositories

import java.util.UUID

internal actual fun generateUuid(): String = UUID.randomUUID().toString()
