package com.example.pxrioverde.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.UIKit.UIImage

actual class ImageCompressor {

    /**
     * Reduz a foto salva em [path] e grava uma copia JPEG na pasta temporaria.
     * Se nao conseguir abrir a imagem, devolve o caminho original (sem quebrar o fluxo).
     */
    actual suspend fun compress(path: String, maxWidth: Int, quality: Int): String {
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return path
        val image = UIImage(contentsOfFile = path)
        val bytes = image.toJpegBytes(maxWidth, quality.coerceIn(1, 100) / 100.0) ?: return path
        val outPath = NSTemporaryDirectory() + "compressed_" + NSUUID().UUIDString + ".jpg"
        val written = bytes.toNSData().writeToFile(outPath, atomically = true)
        return if (written) outPath else path
    }

    /** Le o arquivo inteiro. Devolve vazio se o arquivo nao existir. */
    actual suspend fun readBytes(path: String): ByteArray {
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return ByteArray(0)
        val data = NSData.create(contentsOfFile = path) ?: return ByteArray(0)
        return data.toByteArray()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun ByteArray.toNSData(): NSData =
    if (isEmpty()) {
        NSData()
    } else {
        usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = size.toULong()) }
    }

@Composable
actual fun rememberImageCompressor(): ImageCompressor = remember { ImageCompressor() }
