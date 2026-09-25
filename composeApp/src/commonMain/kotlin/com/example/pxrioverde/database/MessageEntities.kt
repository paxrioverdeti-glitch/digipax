package com.example.pxrioverde.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "messages")
@Serializable
data class MessageEntity(
    @PrimaryKey val id: String,
    val ticketId: String,
    val authorName: String,
    val text: String,
    val timestamp: Long,
    val isAdmin: Boolean,
    val syncStatus: String = "SENT" // SENDING, SENT, ERROR
)
