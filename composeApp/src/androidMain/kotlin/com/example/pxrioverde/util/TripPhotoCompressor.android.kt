package com.example.pxrioverde.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

internal fun compressTripPhoto(context: Context, uri: Uri): ByteArray {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: error("Não foi possível ler a foto da vistoria.")
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes

    return try {
        val maxDimension = maxOf(bitmap.width, bitmap.height)
        val scale = if (maxDimension > 1600) 1600f / maxDimension else 1f
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        try {
            ByteArrayOutputStream().use { output ->
                check(resized.compress(Bitmap.CompressFormat.JPEG, 75, output)) {
                    "Não foi possível comprimir a foto da vistoria."
                }
                output.toByteArray()
            }
        } finally {
            if (resized !== bitmap) resized.recycle()
        }
    } finally {
        bitmap.recycle()
    }
}
