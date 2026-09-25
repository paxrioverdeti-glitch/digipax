package com.example.pxrioverde.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class ImageCompressor(private val context: Context) {
    actual suspend fun compress(path: String, maxWidth: Int, quality: Int): String = withContext(Dispatchers.IO) {
        val file = File(path)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)

        var inSampleSize = 1
        if (options.outWidth > maxWidth) {
            inSampleSize = options.outWidth / maxWidth
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        
        val bitmap = BitmapFactory.decodeFile(path, decodeOptions) ?: return@withContext path
        
        // Corrigir orientação baseada no EXIF
        val orientedBitmap = rotateImageIfRequired(bitmap, path)

        val compressedFile = File(context.cacheDir, "compressed_${file.name}")
        FileOutputStream(compressedFile).use { out ->
            orientedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        
        compressedFile.absolutePath
    }

    actual suspend fun readBytes(path: String): ByteArray = withContext(Dispatchers.IO) {
        File(path).readBytes()
    }

    private fun rotateImageIfRequired(img: Bitmap, path: String): Bitmap {
        val ei = ExifInterface(path)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
}

@androidx.compose.runtime.Composable
actual fun rememberImageCompressor(): ImageCompressor {
    val context = androidx.compose.ui.platform.LocalContext.current
    return androidx.compose.runtime.remember(context) { ImageCompressor(context) }
}
