package com.example.pxrioverde.ui.ticket

import androidx.compose.runtime.Composable
import com.example.pxrioverde.model.User
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
    // No iOS, por enquanto apenas mostramos a tela sem a lógica de câmara/galeria
    // (Implementação placeholder para KMP compilar)
    TicketCreateScreen(
        user = user,
        viewModel = viewModel,
        onBack = onBack,
        onPickFile = { /* TODO */ },
        onTakeNoFoto = { /* TODO */ },
        selectedFileName = "",
        preselectedSector = preselectedSector,
        defaultSector = defaultSector,
        defaultComputer = defaultComputer
    )
}
