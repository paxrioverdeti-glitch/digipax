package com.example.pxrioverde.ui.feed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun FeedImagePicker(
    onImageSelected: (bytes: ByteArray, fileName: String) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { input -> input.readBytes() }
            if (bytes != null) {
                onImageSelected(bytes, "mural_${System.currentTimeMillis()}.jpg")
            }
        }
    }

    TextButton(onClick = { launcher.launch("image/*") }) {
        Text("Selecionar imagem")
    }
}
