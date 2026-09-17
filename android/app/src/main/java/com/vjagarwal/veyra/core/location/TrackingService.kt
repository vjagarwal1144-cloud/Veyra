package com.vjagarwal.veyra.core.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vjagarwal.veyra.R
import com.vjagarwal.veyra.core.alarm.AlarmService
import com.vjagarwal.veyra.core.safety.ProtectionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TrackingService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var client: com.google.android.gms.location.FusedLocationProviderClient
    private var previous: Location? = null
    private var engine: ProtectionEngine? = null

    override fun onCreate() {
        super.onCreate(); client = LocationServices.getFusedLocationProviderClient(this); createChannel(); startTrackingNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val lat = intent?.getDoubleExtra("lat", 0.0) ?: 0.0
        val lon = intent?.getDoubleExtra("lon", 0.0) ?: 0.0
        val wake = intent?.getFloatExtra("wake", 1000f) ?: 1000f
        if (lat == 0.0 && lon == 0.0) return START_NOT_STICKY
        engine = ProtectionEngine(lat, lon, wake)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { stopSelf(); return START_NOT_STICKY }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).setMinUpdateIntervalMillis(2500L).setWaitForAccurateLocation(true).build()
        client.requestLocationUpdates(request, callback, mainLooper)
        return START_STICKY
    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val valid = LocationValidator.validate(location, previous)
            if (!valid.accepted) return
            previous = location
            engine?.evaluate(location, previous)?.let { decision -> if (decision.shouldAlarm) { client.removeLocationUpdates(this); ContextCompat.startForegroundService(this@TrackingService, Intent(this@TrackingService, AlarmService::class.java).setAction(AlarmService.ACTION_START)); scope.launch { /* journey is finalized by repository/UI */ } } }
        }
    }

    private fun startTrackingNotification() {
        val n = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle("Veyra protection active").setContentText("Tracking your destination locally").setOngoing(true).build()
        if (android.os.Build.VERSION.SDK_INT >= 29) startForeground(21, n, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION) else startForeground(21, n)
    }
    private fun createChannel() { if (android.os.Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL, "Journey Tracking", NotificationManager.IMPORTANCE_LOW)) }
    override fun onDestroy() { client.removeLocationUpdates(callback); scope.cancel(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object { const val CHANNEL = "veyra_tracking" }
}
