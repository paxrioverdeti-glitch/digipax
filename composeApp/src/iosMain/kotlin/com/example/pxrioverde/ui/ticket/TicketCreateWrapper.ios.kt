package com.example.pxrioverde.ui.ticket

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.pxrioverde.model.User
import com.example.pxrioverde.util.IosImagePicker
import com.example.pxrioverde.viewmodel.TicketViewModel

@Composable
actual fun TicketCreateWrapper(
    user: User,
    viewModel: TicketViewModel,
    onBack: () -> Unit,
    preselectedSector: String?,
    defaultSector: String,
    defaultComputer: String
) {
    var selectedFileName by remember { mutableStateOf("") }

    // 1200px e 80%, igual ao Android (um pouco maior que o perfil, para ler a mensagem de erro na foto).
    fun pick(source: IosImagePicker.Source, label: String) {
        IosImagePicker.present(source = source, maxSide = 1200, quality = 0.8) { bytes ->
            if (bytes != null && bytes.isNotEmpty()) {
                viewModel.setAttachment(bytes, "${label.lowercase().replace(" ", "_")}.jpg")
                selectedFileName = label
            }
        }
    }

    TicketCreateScreen(
        user = user,
        viewModel = viewModel,
        onBack = onBack,
        onPickFile = { pick(IosImagePicker.Source.GALLERY, "Arquivo selecionado") },
        onTakeNoFoto = { pick(IosImagePicker.Source.CAMERA, "Foto da Câmera") },
        selectedFileName = selectedFileName,
        preselectedSector = preselectedSector,
        defaultSector = defaultSector,
        defaultComputer = defaultComputer
    )
}
