package com.example.pxrioverde.ui.user

import androidx.compose.runtime.Composable
import com.example.pxrioverde.model.User
import com.example.pxrioverde.viewmodel.ProfileViewModel
import javax.swing.JFileChooser
import java.io.File

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
            val chooser = JFileChooser()
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val file = chooser.selectedFile
                viewModel.uploadProfilePicture(user.id, file.readBytes())
            }
        },
        onLogout = onLogout
    )
}
