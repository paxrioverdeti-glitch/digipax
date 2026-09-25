package com.example.pxrioverde

import kotlin.js.Date

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun getCurrentTimestamp(): Long = Date.now().toLong()
