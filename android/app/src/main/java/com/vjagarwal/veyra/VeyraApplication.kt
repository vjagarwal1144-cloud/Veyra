package com.vjagarwal.veyra

import android.app.Application
import androidx.room.Room
import com.vjagarwal.veyra.data.AppDatabase

class VeyraApplication : Application() {
    val database: AppDatabase by lazy { Room.databaseBuilder(this, AppDatabase::class.java, "veyra.db").fallbackToDestructiveMigration().build() }
}
