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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vjagarwal.veyra.VeyraApplication
import com.vjagarwal.veyra.core.alarm.AlarmNotifier
import com.vjagarwal.veyra.core.alarm.AlarmScheduler
import com.vjagarwal.veyra.core.common.JourneyPrefs
import com.vjagarwal.veyra.core.safety.ProtectionEngine
import com.vjagarwal.veyra.data.JourneyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TrackingService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var client: FusedLocationProviderClient
    private lateinit var repository: JourneyRepository
    private lateinit var prefs: JourneyPrefs
    private var previous: Location? = null
    private var engine: ProtectionEngine? = null
    private var journeyId: String? = null
    private var currentWakeDistance = 1_000f

    override fun onCreate() {
        super.onCreate()
        client = LocationServices.getFusedLocationProviderClient(this)
        repository = JourneyRepository((application as VeyraApplication).database.journeyDao())
        prefs = JourneyPrefs(this)
        createChannel()
        startTrackingNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            scope.launch {
                journeyId?.let { repository.finish(it, false) }
                prefs.clear()
                AlarmScheduler.cancel(this@TrackingService)
                stopSelf()
            }
            return START_NOT_STICKY
        }

        journeyId = intent?.getStringExtra("journey_id") ?: prefs.activeId()
        val id = journeyId ?: return START_NOT_STICKY
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return START_NOT_STICKY
        }

        scope.launch {
            val journey = repository.find(id) ?: repository.active()
            if (journey == null || !journey.active) {
                prefs.clear()
                stopSelf()
                return@launch
            }
            currentWakeDistance = journey.wakeDistanceMeters
            engine = ProtectionEngine(journey.latitude, journey.longitude, journey.wakeDistanceMeters)
            requestUpdates()
        }
        return START_STICKY
    }

    private fun requestUpdates() {
        val interval = if (currentWakeDistance <= 1_000f) 3_000L else 7_500L
        val minInterval = if (currentWakeDistance <= 1_000f) 1_500L else 4_000L
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateIntervalMillis(minInterval)
            .setWaitForAccurateLocation(true)
            .build()
        client.requestLocationUpdates(request, callback, mainLooper)
    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val old = previous
            val validation = LocationValidator.validate(location, old)
            if (!validation.accepted) return

            val decision = engine?.evaluate(location, old) ?: return
            previous = location
            val id = journeyId ?: return
            scope.launch {
                repository.updateTelemetry(id, decision.distanceMeters, location.accuracy, location.time)
            }

            if (decision.distanceMeters <= currentWakeDistance * 3f) {
                // Increase tracking density near the destination without making the whole journey high-power.
                client.removeLocationUpdates(this)
                requestUpdates()
            }

            if (decision.shouldAlarm) {
                client.removeLocationUpdates(this)
                scope.launch {
                    repository.finish(id, true)
                    prefs.clear()
                    AlarmScheduler.cancel(this@TrackingService)
                    AlarmNotifier.show(this@TrackingService, decision.explanation)
                    stopSelf()
                }
            }
        }
    }

    private fun startTrackingNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Veyra protection active")
            .setContentText("Tracking locally; internet is optional")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            startForeground(21, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(21, notification)
        }
    }

    private fun createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, "Journey Tracking", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onDestroy() {
        client.removeLocationUpdates(callback)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.vjagarwal.veyra.STOP_TRACKING"
        const val CHANNEL = "veyra_tracking"
    }
}
