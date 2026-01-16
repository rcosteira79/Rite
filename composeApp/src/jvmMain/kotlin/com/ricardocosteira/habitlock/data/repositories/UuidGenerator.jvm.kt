package com.ricardocosteira.habitlock.data.repositories

import java.util.UUID

internal actual fun generateUuid(): String = UUID.randomUUID().toString()

