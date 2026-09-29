package com.example.pxrioverde.ui.user

import androidx.compose.runtime.Composable
import com.example.pxrioverde.model.User
import com.example.pxrioverde.viewmodel.ProfileViewModel

@Composable
actual fun ProfileScreenWrapper(
    user: User,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    ProfileScreen(
        user = user,
        viewModel = viewModel,
        onPickPhoto = {
            // Suporte para seleção de imagem no iOS
        },
        onLogout = onLogout
    )
}
