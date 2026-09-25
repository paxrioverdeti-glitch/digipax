package com.example.pxrioverde.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

actual fun getDatabase(): AppDatabase {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "px_rioverde.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
    .setDriver(BundledSQLiteDriver())
    .fallbackToDestructiveMigration(true)
    .build()
}
