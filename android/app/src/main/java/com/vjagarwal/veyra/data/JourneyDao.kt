package com.vjagarwal.veyra.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journey: JourneyEntity)

    @Query("SELECT * FROM journeys ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<JourneyEntity>>

    @Query("SELECT * FROM journeys WHERE active = 1 ORDER BY startedAt DESC LIMIT 1")
    suspend fun active(): JourneyEntity?

    @Query("SELECT * FROM journeys WHERE id = :id LIMIT 1")
    suspend fun find(id: String): JourneyEntity?

    @Query("UPDATE journeys SET lastDistanceMeters = :distance, lastAccuracyMeters = :accuracy, lastLocationAt = :locationAt WHERE id = :id")
    suspend fun updateTelemetry(id: String, distance: Float, accuracy: Float, locationAt: Long)

    @Query("UPDATE journeys SET active = 0, endedAt = :endedAt, alarmTriggered = :alarmTriggered WHERE id = :id")
    suspend fun finish(id: String, endedAt: Long, alarmTriggered: Boolean)
}
