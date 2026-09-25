package com.example.pxrioverde.ui.user

import androidx.compose.runtime.Composable
import com.example.pxrioverde.model.Booking
import com.example.pxrioverde.viewmodel.TripViewModel

@Composable
expect fun VehicleCheckOutWrapper(
    booking: Booking,
    viewModel: TripViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
)
