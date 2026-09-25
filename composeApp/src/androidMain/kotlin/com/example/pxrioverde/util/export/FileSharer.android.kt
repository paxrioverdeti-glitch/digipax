package com.example.pxrioverde.util.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

actual class FileSharer(private val context: Context) {
    actual fun shareCsv(content: String, fileName: String) {
        val file = File(context.cacheDir, "$fileName.csv")
        file.writeText(content)
        
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "Exportar Relatório")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
