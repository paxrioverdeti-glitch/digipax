package com.example.pxrioverde.ui.user

import androidx.compose.runtime.Composable
import com.example.pxrioverde.model.User
import com.example.pxrioverde.viewmodel.ProfileViewModel

@Composable
expect fun ProfileScreenWrapper(
    user: User,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
)
