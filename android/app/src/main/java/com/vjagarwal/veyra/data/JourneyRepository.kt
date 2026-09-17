package com.vjagarwal.veyra.data

import kotlinx.coroutines.flow.Flow

class JourneyRepository(private val dao: JourneyDao) {
    fun history(): Flow<List<JourneyEntity>> = dao.observeAll()
    suspend fun insert(journey: JourneyEntity) = dao.insert(journey)
    suspend fun active() = dao.active()
    suspend fun finish(id: String, alarmTriggered: Boolean) = dao.finish(id, System.currentTimeMillis(), alarmTriggered)
}
