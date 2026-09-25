package com.example.pxrioverde

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getCurrentTimestamp(): Long

expect fun generateUUID(): String
