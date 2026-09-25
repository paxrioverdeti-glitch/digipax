package com.example.pxrioverde.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert
    suspend fun insertPendingSync(sync: PendingTripSync)

    @Query("SELECT * FROM pending_trip_sync WHERE status = :status ORDER BY timestamp ASC")
    suspend fun getPendingUpdates(status: SyncStatus): List<PendingTripSync>

    @Query("UPDATE pending_trip_sync SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SyncStatus)

    @Delete
    suspend fun delete(sync: PendingTripSync)

    @Query("DELETE FROM pending_trip_sync")
    suspend fun clearAll()
}
