package com.vjagarwal.veyra.core.safety

import android.location.Location
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtectionEngineTest {
    private fun location(lat: Double, lon: Double, accuracy: Float = 5f): Location =
        Location("test").apply {
            latitude = lat
            longitude = lon
            this.accuracy = accuracy
            time = System.currentTimeMillis()
            speed = 1f
            bearing = 0f
        }

    @Test
    fun triggersInsideWellConfidentWakeZone() {
        val engine = ProtectionEngine(28.6139, 77.2090, 500f)
        val fix = location(28.61395, 77.20905)
        val decision = engine.evaluate(fix, null)
        assertTrue(decision.distanceMeters < 500f)
        assertTrue(decision.shouldAlarm)
    }

    @Test
    fun doesNotTriggerFarAway() {
        val engine = ProtectionEngine(28.6139, 77.2090, 500f)
        val fix = location(28.7039, 77.3090)
        val decision = engine.evaluate(fix, null)
        assertFalse(decision.shouldAlarm)
    }

    @Test
    fun uncertaintyCanPreventAlarmWithinZone() {
        val engine = ProtectionEngine(28.6139, 77.2090, 100f)
        val fix = location(28.61395, 77.20905, accuracy = 200f)
        val decision = engine.evaluate(fix, null)
        assertFalse(decision.shouldAlarm)
    }
}
