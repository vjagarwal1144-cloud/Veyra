package com.vjagarwal.veyra.core.location

import android.location.Location

object LocationValidator {
    data class Result(val accepted: Boolean, val reason: String)

    fun validate(location: Location, previous: Location?): Result {
        if (!location.hasAccuracy() || location.accuracy <= 0f || location.accuracy > 150f) return Result(false, "Poor GPS accuracy")
        if (location.time <= 0L) return Result(false, "Invalid location time")
        if (previous != null) {
            val seconds = ((location.time - previous.time).coerceAtLeast(1L)) / 1000.0
            val meters = previous.distanceTo(location)
            val implied = meters / seconds
            if (implied > 90.0) return Result(false, "Impossible movement jump")
        }
        return Result(true, "OK")
    }
}
