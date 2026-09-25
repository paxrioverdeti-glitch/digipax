package com.example.pxrioverde.util

/**
 * Interface para compressão de imagens multiplataforma.
 */
expect class ImageCompressor {
    /**
     * Comprime uma imagem a partir de um caminho local.
     */
    suspend fun compress(path: String, maxWidth: Int = 1024, quality: Int = 75): String

    /**
     * Lê os bytes de um arquivo local.
     */
    suspend fun readBytes(path: String): ByteArray
}

@androidx.compose.runtime.Composable
expect fun rememberImageCompressor(): ImageCompressor
