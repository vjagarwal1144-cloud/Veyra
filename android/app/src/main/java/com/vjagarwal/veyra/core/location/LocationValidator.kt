package com.vjagarwal.veyra.core.location

import android.location.Location

object LocationValidator {
    data class Result(val accepted: Boolean, val reason: String)

    fun validate(location: Location, previous: Location?): Result {
        if (location.latitude !in -90.0..90.0 || location.longitude !in -180.0..180.0) {
            return Result(false, "Invalid coordinates")
        }
        if (!location.hasAccuracy() || location.accuracy <= 0f || location.accuracy > 200f) {
            return Result(false, "Poor GPS accuracy")
        }
        val now = System.currentTimeMillis()
        val ageMs = now - location.time
        if (ageMs !in 0..20_000) return Result(false, "Stale location")
        if (android.os.Build.VERSION.SDK_INT >= 31 && location.isMock) return Result(false, "Mock location rejected")

        previous ?: return Result(true, "First valid fix")
        val elapsedMs = location.time - previous.time
        if (elapsedMs <= 0L) return Result(false, "Non-increasing location time")
        val elapsedSeconds = elapsedMs / 1000.0
        val travelledMeters = previous.distanceTo(location)
        val impliedSpeed = travelledMeters / elapsedSeconds
        if (impliedSpeed > 120.0) return Result(false, "Impossible movement jump")
        return Result(true, "OK")
    }
}
