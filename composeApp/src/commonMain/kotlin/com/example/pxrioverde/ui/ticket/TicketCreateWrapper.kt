package com.example.pxrioverde.ui.ticket

import androidx.compose.runtime.Composable
import com.example.pxrioverde.model.User
import com.example.pxrioverde.viewmodel.TicketViewModel

/**
 * Interface expectation for KMP to handle platform-specific UI or logic.
 * On Android, this will be implemented in androidMain.
 */
@Composable
expect fun TicketCreateWrapper(
    user: User,
    viewModel: TicketViewModel,
    onBack: () -> Unit,
    preselectedSector: String? = null,
    defaultSector: String = "",
    defaultComputer: String = ""
)
