package com.vjagarwal.veyra.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_WAKE) {
            ContextCompat.startForegroundService(context, Intent(context, AlarmService::class.java).setAction(AlarmService.ACTION_START))
        }
    }
    companion object { const val ACTION_WAKE = "com.vjagarwal.veyra.WAKE" }
}
