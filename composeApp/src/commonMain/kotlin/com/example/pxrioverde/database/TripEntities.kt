package com.example.pxrioverde.database

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus { PENDING, SYNCING, COMPLETED, FAILED }

@Entity(tableName = "pending_trip_sync")
data class PendingTripSync(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: String,
    val type: String, // "CHECK_OUT" ou "CHECK_IN"
    val kmValue: Double,
    val photoPaths: String, // Caminhos locais separados por vírgula
    val timestamp: Long = System.currentTimeMillis(),
    val status: SyncStatus = SyncStatus.PENDING,
    val attempts: Int = 0
)
