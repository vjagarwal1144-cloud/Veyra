package com.vjagarwal.veyra.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_WAKE) {
            AlarmNotifier.show(context, "Time-based fallback alarm: GPS protection may need attention")
        }
    }

    companion object {
        const val ACTION_WAKE = "com.vjagarwal.veyra.WAKE"
    }
}
