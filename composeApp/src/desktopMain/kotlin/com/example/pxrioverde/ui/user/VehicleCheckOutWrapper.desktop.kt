package com.example.pxrioverde.ui.user

import androidx.compose.runtime.*
import com.example.pxrioverde.model.Booking
import com.example.pxrioverde.viewmodel.TripViewModel

@Composable
actual fun VehicleCheckOutWrapper(
    booking: Booking,
    viewModel: TripViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val capturedPhotos by viewModel.capturedPhotos.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    VehicleCheckOutScreen(
        carName = "Veículo", // Booking não tem carName diretamente
        capturedPhotos = capturedPhotos,
        isUploading = isUploading,
        onTakePhoto = {
            // No camera
        },
        onSubmit = { km ->
            viewModel.finishBooking(booking, km) { success ->
                if (success) onSuccess()
            }
        },
        onBack = onBack,
        isFinishing = true
    )
}
