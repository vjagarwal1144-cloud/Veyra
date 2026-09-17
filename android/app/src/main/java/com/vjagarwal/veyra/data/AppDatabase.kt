package com.vjagarwal.veyra.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [JourneyEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journeyDao(): JourneyDao
}
