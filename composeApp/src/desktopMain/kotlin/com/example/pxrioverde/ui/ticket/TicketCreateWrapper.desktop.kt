package com.example.pxrioverde.ui.ticket

import androidx.compose.runtime.*
import com.example.pxrioverde.model.User
import com.example.pxrioverde.viewmodel.TicketViewModel
import javax.swing.JFileChooser

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
    
    TicketCreateScreen(
        user = user,
        viewModel = viewModel,
        onBack = onBack,
        onPickFile = {
            val chooser = JFileChooser()
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val file = chooser.selectedFile
                selectedFileName = file.name
                viewModel.setAttachment(file.readBytes(), file.name)
            }
        },
        onTakeNoFoto = {
            // Desktop fallback
        },
        selectedFileName = selectedFileName,
        preselectedSector = preselectedSector,
        defaultSector = defaultSector,
        defaultComputer = defaultComputer
    )
}
