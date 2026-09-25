package com.example.pxrioverde.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class ImageCompressor {
    actual suspend fun compress(path: String, maxWidth: Int, quality: Int): String {
        return path // No-op para iOS por enquanto
    }

    actual suspend fun readBytes(path: String): ByteArray {
        return ByteArray(0) // TODO: Implementar para iOS
    }
}

@Composable
actual fun rememberImageCompressor(): ImageCompressor = remember { ImageCompressor() }
