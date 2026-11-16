package com.example.pxrioverde.ui.user

import androidx.compose.runtime.Composable
import com.example.pxrioverde.model.User
import com.example.pxrioverde.util.IosImagePicker
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
            // Menu nativo do iPhone: "Tirar foto" / "Escolher da galeria".
            // 800px e qualidade 75%, igual ao Android, para a foto ficar leve.
            IosImagePicker.presentChooser(title = "Foto de perfil", maxSide = 800, quality = 0.75) { bytes ->
                if (bytes != null && bytes.isNotEmpty()) {
                    viewModel.uploadProfilePicture(user.id, bytes)
                }
            }
        },
        onLogout = onLogout
    )
}
