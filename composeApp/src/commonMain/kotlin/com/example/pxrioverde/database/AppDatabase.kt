package com.example.pxrioverde.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [PendingTripSync::class, TicketEntity::class, MessageEntity::class], version = 4, exportSchema = false)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun ticketDao(): TicketDao
    abstract fun messageDao(): MessageDao
}

// Room KMP requer um construtor de banco de dados expect/actual ou via interface
// O KSP gerará a implementação 'actual' automaticamente.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun getDatabase(): AppDatabase
