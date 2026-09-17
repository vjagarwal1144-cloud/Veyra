package com.vjagarwal.veyra.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {
    @Insert suspend fun insert(journey: JourneyEntity)
    @Query("SELECT * FROM journeys ORDER BY startedAt DESC") fun observeAll(): Flow<List<JourneyEntity>>
    @Query("SELECT * FROM journeys WHERE active = 1 LIMIT 1") suspend fun active(): JourneyEntity?
    @Query("UPDATE journeys SET active = 0, endedAt = :endedAt, alarmTriggered = :alarmTriggered WHERE id = :id") suspend fun finish(id: String, endedAt: Long, alarmTriggered: Boolean)
}
