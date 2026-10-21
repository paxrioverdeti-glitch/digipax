package com.example.pxrioverde.ui.feed

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun FeedImagePicker(
    onImageSelected: (bytes: ByteArray, fileName: String) -> Unit
) {
    TextButton(onClick = {
        val chooser = JFileChooser().apply {
            dialogTitle = "Selecionar imagem"
            fileFilter = FileNameExtensionFilter(
                "Imagens (JPG, JPEG, PNG, GIF, BMP)",
                "jpg",
                "jpeg",
                "png",
                "gif",
                "bmp"
            )
        }

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = chooser.selectedFile
            onImageSelected(file.readBytes(), file.name)
        }
    }) {
        Text("Selecionar imagem")
    }
}
