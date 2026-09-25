package com.example.pxrioverde.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Query("DELETE FROM tickets")
    suspend fun clearAll()
}
