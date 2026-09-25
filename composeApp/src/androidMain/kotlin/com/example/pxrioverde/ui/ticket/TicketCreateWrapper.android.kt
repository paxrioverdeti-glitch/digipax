package com.example.pxrioverde.ui.ticket

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.pxrioverde.model.User
import com.example.pxrioverde.viewmodel.TicketViewModel
import com.example.pxrioverde.util.rememberImageCompressor
import kotlinx.coroutines.launch
import java.io.File

@Composable
actual fun TicketCreateWrapper(
    user: User,
    viewModel: TicketViewModel,
    onBack: () -> Unit,
    preselectedSector: String?,
    defaultSector: String,
    defaultComputer: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val compressor = rememberImageCompressor()
    
    var imageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val imageUri = imageUriString?.let { Uri.parse(it) }
    var selectedFileName by remember { mutableStateOf("") }

    suspend fun processAndSetImage(uri: Uri, label: String) {
        try {
            // Criar arquivo temporário para processamento
            val tempFile = File(context.cacheDir, "temp_ticket_attachment.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Comprimir para 1200px (um pouco maior que perfil para manter detalhes do erro)
            val compressedPath = compressor.compress(tempFile.absolutePath, 1200, 80)
            val bytes = compressor.readBytes(compressedPath)
            
            viewModel.setAttachment(bytes, "${label.lowercase().replace(" ", "_")}.jpg")
            selectedFileName = label
            
            // Limpeza
            tempFile.delete()
            File(compressedPath).delete()
        } catch (e: Exception) {
            println("Erro ao processar anexo: ${e.message}")
        }
    }

    // Launcher para selecionar arquivo da galeria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                processAndSetImage(it, "Arquivo selecionado")
            }
        }
    }

    // Launcher para tirar foto com a câmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            scope.launch {
                processAndSetImage(imageUri, "Foto da Câmera")
            }
        }
    }

    // Launcher para solicitar permissão da câmera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            val file = File(context.cacheDir, "temp_camera_capture.jpg")
            if (file.exists()) file.delete()
            file.createNewFile()
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            imageUriString = uri.toString()
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    TicketCreateScreen(
        user = user,
        viewModel = viewModel,
        onBack = onBack,
        onPickFile = { galleryLauncher.launch("image/*") },
        onTakeNoFoto = { launchCamera() },
        selectedFileName = selectedFileName,
        preselectedSector = preselectedSector,
        defaultSector = defaultSector,
        defaultComputer = defaultComputer
    )
}
