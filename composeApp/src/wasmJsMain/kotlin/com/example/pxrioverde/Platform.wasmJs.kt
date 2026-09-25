package com.example.pxrioverde

import kotlin.js.Date

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun getCurrentTimestamp(): Long = Date.now().toLong()
