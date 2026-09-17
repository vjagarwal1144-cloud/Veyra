package com.vjagarwal.veyra.ui

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vjagarwal.veyra.VeyraApplication
import com.vjagarwal.veyra.core.alarm.AlarmScheduler
import com.vjagarwal.veyra.core.common.JourneyPrefs
import com.vjagarwal.veyra.core.location.TrackingService
import com.vjagarwal.veyra.data.JourneyEntity
import com.vjagarwal.veyra.data.JourneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(app: Application, private val repo: JourneyRepository) : AndroidViewModel(app) {
    private val _safety = MutableStateFlow("Checking device protection…")
    val safety: StateFlow<String> = _safety.asStateFlow()
    val history = repo.history()

    data class Plan(
        val name: String = "",
        val lat: String = "",
        val lon: String = "",
        val wake: String = "1000",
        val fallbackMinutes: String = "90",
        val mode: String = "Auto",
        val protection: String = "Balanced"
    )

    private val _plan = MutableStateFlow(Plan())
    val plan: StateFlow<Plan> = _plan.asStateFlow()

    init { refreshSafetyState() }

    fun update(plan: Plan) { _plan.value = plan }

    fun refreshSafetyState() {
        val context = getApplication<Application>()
        val locationManager = context.getSystemService(LocationManager::class.java)
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val locationEnabled = locationManager?.isLocationEnabled == true
        val notifications = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val alarmPermission = AlarmScheduler.canUseExactAlarm(context)
        val audio = context.getSystemService(AudioManager::class.java)
        val alarmVolumeOk = (audio?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0) > 0
        val batteryOk = if (Build.VERSION.SDK_INT >= 23) {
            val power = context.getSystemService(PowerManager::class.java)
            power?.isIgnoringBatteryOptimizations(context.packageName) == true
        } else true

        _safety.value = when {
            !fineGranted -> "Location permission is required"
            !locationEnabled -> "Turn on device location"
            !notifications -> "Allow notifications before starting protection"
            !alarmVolumeOk -> "Increase alarm volume before starting protection"
            !alarmPermission -> "Exact-alarm access is not granted; fallback timing will be less precise"
            !batteryOk -> "Battery optimization may reduce reliability on some devices"
            else -> "Protection checks passed"
        }
    }

    fun startJourney(onStarted: () -> Unit) {
        val context = getApplication<Application>()
        val p = _plan.value
        val lat = p.lat.toDoubleOrNull()
        val lon = p.lon.toDoubleOrNull()
        val wake = p.wake.toFloatOrNull()
        val fallbackMinutes = p.fallbackMinutes.toLongOrNull()
        if (p.name.isBlank() || lat == null || lon == null || wake == null || fallbackMinutes == null) return
        if (lat !in -90.0..90.0 || lon !in -180.0..180.0 || wake < 50f || fallbackMinutes < 5L) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) return

        viewModelScope.launch {
            repo.active()?.let { repo.finish(it.id, false) }
            val id = UUID.randomUUID().toString()
            val fallbackAt = System.currentTimeMillis() + fallbackMinutes * 60_000L
            val entity = JourneyEntity(
                id = id,
                destinationName = p.name.trim(),
                latitude = lat,
                longitude = lon,
                wakeDistanceMeters = wake,
                transport = p.mode,
                protection = p.protection,
                startedAt = System.currentTimeMillis(),
                fallbackAlarmAt = fallbackAt
            )
            repo.insert(entity)
            JourneyPrefs(context).startJourney(id, fallbackAt)
            AlarmScheduler.scheduleBackup(context, fallbackAt)

            val intent = Intent(context, TrackingService::class.java)
                .putExtra("journey_id", id)
            ContextCompat.startForegroundService(context, intent)
            onStarted()
        }
    }

    fun finishActive() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            repo.active()?.let { repo.finish(it.id, false) }
            JourneyPrefs(context).clear()
            AlarmScheduler.cancel(context)
            context.stopService(Intent(context, TrackingService::class.java))
        }
    }

    companion object {
        fun factory(application: Application) = object : ViewModelProvider.AndroidViewModelFactory(application) {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val db = (application as VeyraApplication).database
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application, JourneyRepository(db.journeyDao())) as T
            }
        }
    }
}
