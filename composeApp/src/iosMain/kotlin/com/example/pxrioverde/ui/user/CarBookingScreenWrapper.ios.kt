package com.example.pxrioverde.ui.user

import androidx.compose.runtime.Composable
import com.example.pxrioverde.model.User
import com.example.pxrioverde.viewmodel.TripViewModel

@Composable
actual fun CarBookingScreenWrapper(
    user: User,
    viewModel: TripViewModel,
    onBack: () -> Unit,
    defaultDepartment: String
) {
    CarBookingScreen(
        user = user,
        viewModel = viewModel,
        onBack = onBack,
        onTakePhoto = { /* Not supported in iOS Preview */ },
        defaultDepartment = defaultDepartment
    )
}
