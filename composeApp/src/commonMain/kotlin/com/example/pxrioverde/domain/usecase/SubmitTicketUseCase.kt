package com.example.pxrioverde.domain.usecase

import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.model.TicketStatus
import com.example.pxrioverde.repository.TicketRepository

class SubmitTicketUseCase(
    private val repository: TicketRepository
) {
    suspend operator fun invoke(
        userId: String,
        userName: String,
        title: String,
        computer: String,
        sector: String,
        description: String,
        attachmentBytes: ByteArray?,
        attachmentName: String?,
        onSuccess: () -> Unit
    ) {
        var imageUrl: String? = null
        
        // 1. Upload do anexo se presente
        attachmentBytes?.let { bytes ->
            attachmentName?.let { name ->
                imageUrl = repository.uploadAttachment(userId, bytes, name)
            }
        }

        // 2. Criação do Ticket
        val ticket = Ticket(
            title = title,
            description = description,
            status = TicketStatus.NA_FILA,
            userId = userId,
            userName = userName,
            computerName = computer,
            sector = sector,
            imageUrl = imageUrl
        )
        
        repository.createTicket(ticket)
        repository.syncTickets(userId)
        onSuccess()
    }
}
