package com.vjagarwal.veyra.core.safety

import android.location.Location
import kotlin.math.abs

class ProtectionEngine(
    private val destinationLat: Double,
    private val destinationLon: Double,
    private val wakeDistance: Float
) {
    data class Decision(
        val distanceMeters: Float,
        val shouldAlarm: Boolean,
        val confidence: Float,
        val explanation: String
    )

    fun evaluate(location: Location, previous: Location?): Decision {
        val result = FloatArray(1)
        Location.distanceBetween(location.latitude, location.longitude, destinationLat, destinationLon, result)
        val distance = result[0]
        val accuracy = location.accuracy.coerceIn(1f, 250f)
        val safeRadius = wakeDistance.coerceAtLeast(50f)
        val uncertaintyPenalty = (accuracy / (safeRadius * 1.5f)).coerceIn(0f, 1f)

        val oldDistance = previous?.let {
            val old = FloatArray(1)
            Location.distanceBetween(it.latitude, it.longitude, destinationLat, destinationLon, old)
            old[0]
        }
        val closing = oldDistance == null || distance <= oldDistance + 15f
        val speedValid = !location.hasSpeed() || location.speed <= 100f
        val moving = location.hasSpeed() && location.speed >= 0.5f
        val proximity = (1f - (distance / (safeRadius * 2f))).coerceIn(0f, 1f)
        val directionBonus = if (previous != null && location.hasBearing()) {
            val target = Location("destination").apply {
                latitude = destinationLat
                longitude = destinationLon
            }
            val bearingToDestination = location.bearingTo(target)
            val delta = abs((location.bearing - bearingToDestination + 540f) % 360f - 180f)
            if (delta <= 60f) 0.10f else 0f
        } else 0f

        val confidence = (
            proximity * 0.50f +
                (1f - uncertaintyPenalty) * 0.25f +
                (if (closing) 0.15f else 0f) +
                (if (moving) 0.05f else 0f) +
                directionBonus
            ).coerceIn(0f, 1f)

        val zoneReached = distance <= safeRadius
        val trigger = zoneReached && speedValid && confidence >= 0.62f
        val explanation = when {
            trigger -> "Protected wake zone reached"
            !speedValid -> "Movement data rejected as implausible"
            distance <= safeRadius * 1.5f -> "Near destination; validating GPS confidence"
            else -> "Tracking normally"
        }
        return Decision(distance, trigger, confidence, explanation)
    }
}
