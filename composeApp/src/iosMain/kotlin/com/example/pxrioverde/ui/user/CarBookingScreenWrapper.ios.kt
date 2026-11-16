package com.example.pxrioverde.ui.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.pxrioverde.model.User
import com.example.pxrioverde.ui.components.PhotoGuideDialog
import com.example.pxrioverde.viewmodel.TripViewModel

@Composable
actual fun CarBookingScreenWrapper(
    user: User,
    viewModel: TripViewModel,
    onBack: () -> Unit,
    defaultDepartment: String
) {
    // Fotos do agendamento emergencial: mesmo guia e mesma câmera da vistoria.
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

    CarBookingScreen(
        user = user,
        viewModel = viewModel,
        onBack = onBack,
        onTakePhoto = { index -> guideIndex = index },
        defaultDepartment = defaultDepartment
    )
}
