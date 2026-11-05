package com.example.pxrioverde

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getCurrentTimestamp(): Long

expect fun generateUUID(): String

/** True apenas no iPhone/iPad. Usado para evitar a splash que trava a tela no iOS. */
expect fun isIOS(): Boolean
