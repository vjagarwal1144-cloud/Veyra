package com.vjagarwal.veyra.data

import kotlinx.coroutines.flow.Flow

class JourneyRepository(private val dao: JourneyDao) {
    fun history(): Flow<List<JourneyEntity>> = dao.observeAll()
    suspend fun insert(journey: JourneyEntity) = dao.insert(journey)
    suspend fun active(): JourneyEntity? = dao.active()
    suspend fun find(id: String): JourneyEntity? = dao.find(id)
    suspend fun updateTelemetry(id: String, distance: Float, accuracy: Float, locationAt: Long) = dao.updateTelemetry(id, distance, accuracy, locationAt)
    suspend fun finish(id: String, alarmTriggered: Boolean) = dao.finish(id, System.currentTimeMillis(), alarmTriggered)
}
