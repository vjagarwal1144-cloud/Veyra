package com.vjagarwal.veyra.core.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vjagarwal.veyra.core.alarm.AlarmScheduler
import com.vjagarwal.veyra.core.common.JourneyPrefs

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = JourneyPrefs(context)
        val fallback = prefs.fallbackAlarmAt() ?: return
        if (!prefs.isJourneyActive()) return
        if (fallback <= System.currentTimeMillis()) return
        AlarmScheduler.scheduleBackup(context, fallback)
    }
}
