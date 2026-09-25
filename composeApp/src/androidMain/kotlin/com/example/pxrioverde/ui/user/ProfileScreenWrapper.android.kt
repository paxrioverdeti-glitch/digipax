package com.example.pxrioverde.ui.user

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.pxrioverde.model.User
import com.example.pxrioverde.viewmodel.ProfileViewModel
import com.example.pxrioverde.util.rememberImageCompressor
import kotlinx.coroutines.launch
import java.io.File

@Composable
actual fun ProfileScreenWrapper(
    user: User,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val compressor = rememberImageCompressor()

    // Launcher para selecionar arquivo da galeria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    // 1. Criar arquivo temporário para processamento garantindo streams fechados
                    val tempFile = File(context.cacheDir, "temp_profile_upload.jpg")
                    context.contentResolver.openInputStream(it)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    // 2. Comprimir a imagem (Reduz para no máximo 800px para garantir tamanho ultraleve e alta estabilidade)
                    val compressedPath = compressor.compress(tempFile.absolutePath, 800, 75)
                    
                    // 3. Lê completamente para a memória em uma operação atômica de array de bytes
                    val bytes = compressor.readBytes(compressedPath)
                    
                    println("DEBUG UPLOAD: Imagem processada e compactada com sucesso. Tamanho final: ${bytes.size / 1024} KB")

                    // 4. Enviar os bytes finais
                    viewModel.uploadProfilePicture(user.id, bytes)
                    
                    // Limpeza atômica
                    if (tempFile.exists()) tempFile.delete()
                    val compFile = File(compressedPath)
                    if (compFile.exists()) compFile.delete()
                    
                } catch (e: Exception) {
                    println("ERRO no processamento da foto de perfil: ${e.message}")
                }
            }
        }
    }

    ProfileScreen(
        user = user,
        viewModel = viewModel,
        onPickPhoto = {
            galleryLauncher.launch("image/*")
        },
        onLogout = onLogout
    )
}
