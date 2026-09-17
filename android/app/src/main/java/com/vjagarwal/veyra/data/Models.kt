package com.vjagarwal.veyra.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journeys")
data class JourneyEntity(
    @PrimaryKey val id: String,
    val destinationName: String,
    val latitude: Double,
    val longitude: Double,
    val wakeDistanceMeters: Float,
    val transport: String,
    val protection: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val alarmTriggered: Boolean = false,
    val active: Boolean = true
)
