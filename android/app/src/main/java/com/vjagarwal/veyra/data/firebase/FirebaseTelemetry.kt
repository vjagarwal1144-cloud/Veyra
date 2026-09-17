package com.vjagarwal.veyra.data.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseTelemetry {
    private fun analytics(context: Context): FirebaseAnalytics? = try {
        if (FirebaseApp.getApps(context).isEmpty()) return null
        FirebaseAnalytics.getInstance(context)
    } catch (_: Exception) {
        null
    }

    fun journeyStarted(context: Context, transport: String, protection: String) {
        analytics(context)?.logEvent("journey_started", Bundle().apply {
            putString("transport", transport)
            putString("protection", protection)
        })
    }

    fun journeyEnded(context: Context, alarmTriggered: Boolean) {
        analytics(context)?.logEvent("journey_ended", Bundle().apply {
            putBoolean("alarm_triggered", alarmTriggered)
        })
    }

    fun alarmTriggered(context: Context, confidence: Float) {
        analytics(context)?.logEvent("alarm_triggered", Bundle().apply {
            putDouble("confidence", confidence.toDouble())
        })
    }

    fun providerFailure(context: Context, provider: String) {
        analytics(context)?.logEvent("provider_failure", Bundle().apply {
            putString("provider", provider)
        })
    }
}
