package com.vjagarwal.veyra.core.common

import android.content.Context

class JourneyPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("veyra_journey", Context.MODE_PRIVATE)

    fun startJourney(id: String, fallbackAlarmAt: Long?) {
        prefs.edit()
            .putBoolean(KEY_ACTIVE, true)
            .putString(KEY_ID, id)
            .apply {
                if (fallbackAlarmAt != null) putLong(KEY_FALLBACK_AT, fallbackAlarmAt)
                else remove(KEY_FALLBACK_AT)
            }
            .apply()
    }

    fun isJourneyActive(): Boolean = prefs.getBoolean(KEY_ACTIVE, false)
    fun activeId(): String? = prefs.getString(KEY_ID, null)
    fun fallbackAlarmAt(): Long? = if (prefs.contains(KEY_FALLBACK_AT)) prefs.getLong(KEY_FALLBACK_AT, 0L) else null

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACTIVE = "active"
        private const val KEY_ID = "id"
        private const val KEY_FALLBACK_AT = "fallback_alarm_at"
    }
}
