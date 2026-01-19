package com.ricardocosteira.habitlock

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
