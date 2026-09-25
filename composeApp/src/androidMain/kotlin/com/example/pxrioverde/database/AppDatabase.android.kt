package com.example.pxrioverde.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.pxrioverde.PxrioverdeApp

actual fun getDatabase(): AppDatabase {
    val appContext = PxrioverdeApp.instance.applicationContext
    val dbFile = appContext.getDatabasePath("px_rioverde.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
        .build()
}
