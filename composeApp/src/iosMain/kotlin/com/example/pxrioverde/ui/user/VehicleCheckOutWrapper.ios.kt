package com.example.pxrioverde.ui.user

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import com.example.pxrioverde.model.Booking
import com.example.pxrioverde.viewmodel.TripViewModel

@Composable
actual fun VehicleCheckOutWrapper(
    booking: Booking,
    viewModel: TripViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    // Placeholder para iOS
    Box {
        Text("Câmera não suportada em iOS (Preview)")
    }
}
