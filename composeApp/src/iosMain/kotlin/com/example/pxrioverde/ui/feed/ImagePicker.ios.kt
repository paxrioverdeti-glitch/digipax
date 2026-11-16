package com.example.pxrioverde.ui.feed

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.pxrioverde.getCurrentTimestamp
import com.example.pxrioverde.util.IosImagePicker

@Composable
actual fun FeedImagePicker(
    onImageSelected: (bytes: ByteArray, fileName: String) -> Unit
) {
    // Mesmo botão do Android ("Selecionar imagem"), abrindo a galeria nativa do iPhone.
    TextButton(onClick = {
        IosImagePicker.present(source = IosImagePicker.Source.GALLERY, maxSide = 1600, quality = 0.85) { bytes ->
            if (bytes != null && bytes.isNotEmpty()) {
                onImageSelected(bytes, "mural_${getCurrentTimestamp()}.jpg")
            }
        }
    }) {
        Text("Selecionar imagem")
    }
}
