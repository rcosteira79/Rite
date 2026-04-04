package com.ricardocosteira.rite.data.repositories

import platform.Foundation.NSUUID

internal actual fun generateUuid(): String = NSUUID().UUIDString()

