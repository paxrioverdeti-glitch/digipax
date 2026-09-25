package com.example.pxrioverde

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getCurrentTimestamp(): Long = System.currentTimeMillis()

actual fun generateUUID(): String = java.util.UUID.randomUUID().toString()
