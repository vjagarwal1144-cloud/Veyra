package com.vjagarwal.veyra.core.safety

import android.location.Location

class ProtectionEngine(private val destinationLat: Double, private val destinationLon: Double, private val wakeDistance: Float) {
    data class Decision(val distanceMeters: Float, val shouldAlarm: Boolean, val confidence: Float, val explanation: String)

    fun evaluate(location: Location, previous: Location?): Decision {
        val result = FloatArray(1)
        Location.distanceBetween(location.latitude, location.longitude, destinationLat, destinationLon, result)
        val distance = result[0]
        val uncertainty = location.accuracy.coerceAtLeast(5f)
        val radius = wakeDistance + uncertainty * 1.5f
        val moving = location.hasSpeed() && location.speed >= 0.8f
        val closing = if (previous != null) {
            val old = FloatArray(1)
            Location.distanceBetween(previous.latitude, previous.longitude, destinationLat, destinationLon, old)
            distance <= old[0] + 20f
        } else true
        val proximity = if (distance <= radius) 1f else (radius / distance).coerceIn(0f, 1f)
        val confidence = (proximity * 0.55f + (1f - (location.accuracy / 100f).coerceIn(0f, 1f)) * 0.30f + (if (closing) 0.1f else 0f) + (if (moving) 0.05f else 0f)).coerceIn(0f, 1f)
        val trigger = distance <= radius && confidence >= 0.60f
        return Decision(distance, trigger, confidence, if (trigger) "Within protected wake zone" else "Tracking normally")
    }
}
