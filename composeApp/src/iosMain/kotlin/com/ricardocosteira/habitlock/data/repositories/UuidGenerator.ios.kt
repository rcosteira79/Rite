package com.ricardocosteira.habitlock.data.repositories

import platform.Foundation.NSUUID

internal actual fun generateUuid(): String = NSUUID().UUIDString()

