package com.example.pxrioverde.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.model.TicketStatus
import com.example.pxrioverde.model.TicketComment
import kotlinx.serialization.Serializable

@Entity(tableName = "tickets")
@Serializable
data class TicketEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String,
    val userId: String,
    val userName: String,
    val computerName: String? = null,
    val sector: String? = null,
    val createdAt: Long = 0L,
    val imageUrl: String? = null,
    val rating: Int? = null,
    val ratingComment: String? = null
)

fun TicketEntity.toDomain() = Ticket(
    id = id,
    title = title,
    description = description,
    status = TicketStatus.valueOf(status),
    userId = userId,
    userName = userName,
    computerName = computerName ?: "",
    sector = sector ?: "",
    imageUrl = imageUrl,
    rating = rating,
    ratingComment = ratingComment
)

fun Ticket.toEntity() = TicketEntity(
    id = id ?: "",
    title = title,
    description = description,
    status = status.name,
    userId = userId,
    userName = userName,
    computerName = computerName,
    sector = sector,
    imageUrl = imageUrl,
    rating = rating,
    ratingComment = ratingComment
)
