package com.example.pxrioverde

import java.util.UUID

class DesktopPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = DesktopPlatform()

actual fun getCurrentTimestamp(): Long = System.currentTimeMillis()

actual fun generateUUID(): String = UUID.randomUUID().toString()
