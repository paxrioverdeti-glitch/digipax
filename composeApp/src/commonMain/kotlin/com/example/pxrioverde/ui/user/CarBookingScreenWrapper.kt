package com.example.pxrioverde.ui.user

import androidx.compose.runtime.Composable
import com.example.pxrioverde.model.User
import com.example.pxrioverde.viewmodel.TripViewModel

@Composable
expect fun CarBookingScreenWrapper(
    user: User,
    viewModel: TripViewModel,
    onBack: () -> Unit,
    defaultDepartment: String = ""
)
