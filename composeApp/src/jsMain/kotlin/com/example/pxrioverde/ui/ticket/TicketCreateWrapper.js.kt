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
    TicketCreateScreen(
        user = user,
        viewModel = viewModel,
        onBack = onBack,
        onPickFile = { },
        onTakeNoFoto = { },
        selectedFileName = "",
        preselectedSector = preselectedSector,
        defaultSector = defaultSector,
        defaultComputer = defaultComputer
    )
}
