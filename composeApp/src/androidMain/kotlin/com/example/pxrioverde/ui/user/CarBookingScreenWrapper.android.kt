package com.example.pxrioverde.ui.user

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
import com.example.pxrioverde.util.compressTripPhoto
import com.example.pxrioverde.viewmodel.TripViewModel
import java.io.File

@Composable
actual fun CarBookingScreenWrapper(
    user: User,
    viewModel: TripViewModel,
    onBack: () -> Unit,
    defaultDepartment: String
) {
    val context = LocalContext.current
    var currentPhotoIndex by rememberSaveable { mutableStateOf(-1) }
    var imageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val imageUri = imageUriString?.let { Uri.parse(it) }

    // Launcher para tirar foto com a câmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null && currentPhotoIndex != -1) {
            try {
                viewModel.setCapturedPhoto(currentPhotoIndex, compressTripPhoto(context, imageUri))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Launcher para solicitar permissão da câmera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && currentPhotoIndex != -1) {
            // launchCamera() será chamado novamente pelo clique do usuário ou poderíamos auto-trigger
        }
    }

    fun launchCamera(index: Int) {
        currentPhotoIndex = index
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            val file = File(context.cacheDir, "temp_booking_image_$index.jpg")
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

    CarBookingScreen(
        user = user,
        viewModel = viewModel,
        onBack = onBack,
        onTakePhoto = { index -> launchCamera(index) },
        defaultDepartment = defaultDepartment
    )
}
