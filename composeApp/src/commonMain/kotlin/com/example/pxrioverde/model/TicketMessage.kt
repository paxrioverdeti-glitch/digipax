package com.example.pxrioverde.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.pxrioverde.database.MessageEntity

@Serializable
data class TicketMessage(
    val id: String? = null,
    @SerialName("ticket_id") val ticketId: String,
    @SerialName("author_name") val authorName: String,
    val text: String,
    val timestamp: Long,
    @SerialName("is_admin") val isAdmin: Boolean
)

fun TicketMessage.toEntity(syncStatus: String = "SENT") = MessageEntity(
    id = id ?: timestamp.toString(),
    ticketId = ticketId,
    authorName = authorName,
    text = text,
    timestamp = timestamp,
    isAdmin = isAdmin,
    syncStatus = syncStatus
)

fun MessageEntity.toDomain() = TicketMessage(
    id = id,
    ticketId = ticketId,
    authorName = authorName,
    text = text,
    timestamp = timestamp,
    isAdmin = isAdmin
)
