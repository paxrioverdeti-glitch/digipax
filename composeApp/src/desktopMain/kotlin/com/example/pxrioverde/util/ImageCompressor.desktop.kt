package com.example.pxrioverde.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import java.nio.file.Files

actual class ImageCompressor {
    actual suspend fun compress(path: String, maxWidth: Int, quality: Int): String {
        return path // Simplified for desktop for now
    }

    actual suspend fun readBytes(path: String): ByteArray {
        return Files.readAllBytes(File(path).toPath())
    }
}

@Composable
actual fun rememberImageCompressor(): ImageCompressor = remember { ImageCompressor() }
