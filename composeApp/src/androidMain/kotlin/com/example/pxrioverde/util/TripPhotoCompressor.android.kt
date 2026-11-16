package com.example.pxrioverde.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/** Foto recusada por qualidade (escura, estourada ou tremida). A mensagem vai para o motorista. */
class TripPhotoRejectedException(message: String) : Exception(message)

// Tamanho e qualidade pensados para a IA enxergar arranhões finos
// (cerca de 300 a 500 KB por foto).
private const val MAX_DIMENSION = 2048
private const val JPEG_QUALITY = 85

// Limites de qualidade. São conservadores para não recusar foto boa;
// os valores medidos aparecem no log ([FOTO]) para podermos calibrar.
private const val MIN_BRIGHTNESS = 45.0   // 0 = preto, 255 = branco
private const val MAX_BRIGHTNESS = 240.0
private const val MIN_SHARPNESS = 30.0    // nitidez (variância do Laplaciano)

internal fun compressTripPhoto(context: Context, uri: Uri): ByteArray {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: error("Não foi possível ler a foto da vistoria.")
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes

    // 1. Coloca a foto em pé: o celular grava a rotação à parte (EXIF).
    val bitmap = rotateByExif(decoded, bytes)

    try {
        // 2. Confere a qualidade antes de aceitar.
        checkQuality(bitmap)

        // 3. Reduz para no máximo 2048 px no lado maior.
        val maxSide = maxOf(bitmap.width, bitmap.height)
        val scale = if (maxSide > MAX_DIMENSION) MAX_DIMENSION.toFloat() / maxSide else 1f
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else bitmap

        try {
            return ByteArrayOutputStream().use { output ->
                check(resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    "Não foi possível comprimir a foto da vistoria."
                }
                output.toByteArray().also { println("[FOTO] ${resized.width}x${resized.height}, ${it.size / 1024} KB") }
            }
        } finally {
            if (resized !== bitmap) resized.recycle()
        }
    } finally {
        bitmap.recycle()
    }
}

private fun rotateByExif(bitmap: Bitmap, bytes: ByteArray): Bitmap {
    val orientation = try {
        ExifInterface(ByteArrayInputStream(bytes))
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } catch (_: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }
    val degrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> return bitmap
    }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true)
    if (rotated !== bitmap) bitmap.recycle()
    return rotated
}

/**
 * Mede brilho e nitidez numa cópia pequena (rápido) e recusa fotos claramente ruins.
 * Nitidez: compara cada ponto com os vizinhos; foto tremida tem pouca diferença entre eles.
 */
private fun checkQuality(bitmap: Bitmap) {
    val w = 256
    val h = (bitmap.height * w / bitmap.width.toFloat()).toInt().coerceAtLeast(1)
    val small = Bitmap.createScaledBitmap(bitmap, w, h, true)
    try {
        val pixels = IntArray(w * h)
        small.getPixels(pixels, 0, w, 0, 0, w, h)
        val gray = DoubleArray(pixels.size) { i ->
            val p = pixels[i]
            0.299 * ((p shr 16) and 0xFF) + 0.587 * ((p shr 8) and 0xFF) + 0.114 * (p and 0xFF)
        }
        val brightness = gray.average()

        var sum = 0.0
        var sumSq = 0.0
        var count = 0
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val i = y * w + x
                val lap = gray[i - 1] + gray[i + 1] + gray[i - w] + gray[i + w] - 4 * gray[i]
                sum += lap
                sumSq += lap * lap
                count++
            }
        }
        val sharpness = if (count > 0) sumSq / count - (sum / count) * (sum / count) else 0.0
        println("[FOTO] brilho=${brightness.toInt()} nitidez=${sharpness.toInt()}")

        when {
            brightness < MIN_BRIGHTNESS ->
                throw TripPhotoRejectedException("Foto muito escura. Procure um lugar mais claro e tire de novo.")
            brightness > MAX_BRIGHTNESS ->
                throw TripPhotoRejectedException("Foto muito clara (sol ou flash direto). Mude o ângulo e tire de novo.")
            sharpness < MIN_SHARPNESS ->
                throw TripPhotoRejectedException("Foto tremida ou desfocada. Segure firme e tire de novo.")
        }
    } finally {
        small.recycle()
    }
}
