package com.vjagarwal.veyra.core.common

import android.content.Context

class VeyraSettingsPrefs(context: Context) {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var adaptiveTracking: Boolean
        get() = prefs.getBoolean(KEY_ADAPTIVE, true)
        set(value) = prefs.edit().putBoolean(KEY_ADAPTIVE, value).apply()

    var highAccuracy: Boolean
        get() = prefs.getBoolean(KEY_HIGH_ACCURACY, true)
        set(value) = prefs.edit().putBoolean(KEY_HIGH_ACCURACY, value).apply()

    var geofenceBackup: Boolean
        get() = prefs.getBoolean(KEY_GEOFENCE, true)
        set(value) = prefs.edit().putBoolean(KEY_GEOFENCE, value).apply()

    var motionDetection: Boolean
        get() = prefs.getBoolean(KEY_MOTION, true)
        set(value) = prefs.edit().putBoolean(KEY_MOTION, value).apply()

    var repeatAlarm: Boolean
        get() = prefs.getBoolean(KEY_REPEAT_ALARM, true)
        set(value) = prefs.edit().putBoolean(KEY_REPEAT_ALARM, value).apply()

    var batteryWarnings: Boolean
        get() = prefs.getBoolean(KEY_BATTERY_WARNING, true)
        set(value) = prefs.edit().putBoolean(KEY_BATTERY_WARNING, value).apply()

    var internetWarnings: Boolean
        get() = prefs.getBoolean(KEY_INTERNET_WARNING, false)
        set(value) = prefs.edit().putBoolean(KEY_INTERNET_WARNING, value).apply()

    var earphoneAlarm: Boolean
        get() = prefs.getBoolean(KEY_EARPHONE, true)
        set(value) = prefs.edit().putBoolean(KEY_EARPHONE, value).apply()

    var vibration: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var wakeDistance: Int
        get() = prefs.getInt(KEY_WAKE, 1000)
        set(value) = prefs.edit().putInt(KEY_WAKE, value).apply()

    var fallbackMinutes: Int
        get() = prefs.getInt(KEY_FALLBACK, 90)
        set(value) = prefs.edit().putInt(KEY_FALLBACK, value).apply()

    companion object {
        private const val FILE = "veyra_settings"
        private const val KEY_ADAPTIVE = "adaptive"
        private const val KEY_HIGH_ACCURACY = "high_accuracy"
        private const val KEY_GEOFENCE = "geofence_backup"
        private const val KEY_MOTION = "motion_detection"
        private const val KEY_REPEAT_ALARM = "repeat_alarm"
        private const val KEY_BATTERY_WARNING = "battery_warning"
        private const val KEY_INTERNET_WARNING = "internet_warning"
        private const val KEY_EARPHONE = "earphone_alarm"
        private const val KEY_VIBRATION = "vibration"
        private const val KEY_WAKE = "wake_distance"
        private const val KEY_FALLBACK = "fallback_minutes"
    }
}
