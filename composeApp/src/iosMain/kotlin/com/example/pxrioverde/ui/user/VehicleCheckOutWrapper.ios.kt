package com.example.pxrioverde.ui.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.pxrioverde.model.Booking
import com.example.pxrioverde.ui.components.PhotoGuideDialog
import com.example.pxrioverde.util.IosAlert
import com.example.pxrioverde.util.IosImagePicker
import com.example.pxrioverde.viewmodel.TripViewModel

/** Fotos de viagem: 2048px e qualidade 85%, iguais ao Android. */
internal const val TRIP_PHOTO_MAX_SIDE = 2048
internal const val TRIP_PHOTO_QUALITY = 0.85

/**
 * Abre a câmera para a foto [index] da vistoria e guarda no ViewModel.
 * Só câmera, nunca galeria: a vistoria precisa ser do carro naquele momento (mesma regra do Android).
 */
internal fun captureTripPhoto(index: Int, viewModel: TripViewModel) {
    IosImagePicker.present(
        source = IosImagePicker.Source.CAMERA,
        maxSide = TRIP_PHOTO_MAX_SIDE,
        quality = TRIP_PHOTO_QUALITY,
        allowGalleryFallback = false
    ) { bytes ->
        if (bytes != null && bytes.isNotEmpty()) {
            viewModel.setCapturedPhoto(index, bytes)
        }
    }
}

@Composable
actual fun VehicleCheckOutWrapper(
    booking: Booking,
    viewModel: TripViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val capturedPhotos by viewModel.capturedPhotos.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    // Guia de enquadramento mostrado antes de abrir a câmera (igual ao Android)
    var guideIndex by remember { mutableStateOf<Int?>(null) }

    guideIndex?.let { index ->
        PhotoGuideDialog(
            photoIndex = index,
            onOpenCamera = {
                guideIndex = null
                captureTripPhoto(index, viewModel)
            },
            onDismiss = { guideIndex = null }
        )
    }

    val isFinishing = booking.status == "EM_ANDAMENTO"

    VehicleCheckOutScreen(
        carName = "Veículo do Agendamento",
        capturedPhotos = capturedPhotos,
        isUploading = isUploading,
        onTakePhoto = { index -> guideIndex = index },
        onSubmit = { km ->
            if (isFinishing) {
                viewModel.finishBooking(booking, km) { success, inspectionError ->
                    if (success) {
                        if (inspectionError != null) {
                            IosAlert.show(
                                "Viagem finalizada",
                                "A análise de avarias não foi concluída. Avise o gestor da frota."
                            )
                        }
                        onSuccess()
                    }
                }
            } else {
                viewModel.submitTripStart(booking, km) { success ->
                    if (success) onSuccess()
                }
            }
        },
        onBack = onBack,
        isFinishing = isFinishing
    )
}
