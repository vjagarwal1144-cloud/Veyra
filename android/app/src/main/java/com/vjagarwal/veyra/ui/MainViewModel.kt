package com.vjagarwal.veyra.ui

import android.Manifest
import android.app.Application
import android.app.NotificationManager
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
import com.vjagarwal.veyra.core.destination.DestinationSearch
import com.vjagarwal.veyra.core.location.TrackingService
import com.vjagarwal.veyra.data.JourneyEntity
import com.vjagarwal.veyra.data.firebase.FirebaseTelemetry
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

    private val _searchResults = MutableStateFlow<List<DestinationSearch.Result>>(emptyList())
    val searchResults: StateFlow<List<DestinationSearch.Result>> = _searchResults.asStateFlow()
    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    init { refreshSafetyState() }

    fun update(plan: Plan) { _plan.value = plan }

    fun searchDestinations(query: String) {
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _searching.value = true
            _searchResults.value = runCatching { DestinationSearch.search(query) }.getOrDefault(emptyList())
            _searching.value = false
        }
    }

    fun selectDestination(result: DestinationSearch.Result) {
        _plan.value = _plan.value.copy(
            name = result.name,
            lat = result.latitude.toString(),
            lon = result.longitude.toString()
        )
        _searchResults.value = emptyList()
    }

    private fun fullScreenAlarmReady(context: Application): Boolean =
        Build.VERSION.SDK_INT < 34 || context.getSystemService(NotificationManager::class.java)?.canUseFullScreenIntent() == true

    fun refreshSafetyState() {
        val context = getApplication<Application>()
        val locationManager = context.getSystemService(LocationManager::class.java)
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val locationEnabled = locationManager?.isLocationEnabled == true
        val notifications = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val fullScreenReady = fullScreenAlarmReady(context)
        val alarmPermission = AlarmScheduler.canUseExactAlarm(context)
        val audio = context.getSystemService(AudioManager::class.java)
        val alarmVolumeOk = (audio?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0) > 0
        val batteryOptimized = if (Build.VERSION.SDK_INT >= 23) {
            val power = context.getSystemService(PowerManager::class.java)
            power?.isIgnoringBatteryOptimizations(context.packageName) != true
        } else false

        _safety.value = when {
            !fineGranted -> "Location permission is required"
            !locationEnabled -> "Turn on device location"
            !notifications -> "Allow notifications before starting protection"
            !fullScreenReady -> "Allow full-screen alarm notifications"
            !alarmVolumeOk -> "Increase alarm volume before starting protection"
            batteryOptimized -> "Protection ready; battery optimization may reduce OEM background reliability"
            !alarmPermission -> "Protection ready; exact-alarm access is not granted so the time fallback may be less precise"
            else -> "Protection checks passed"
        }
    }

    private fun safetyBlockReason(context: Application): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) return "Location permission is required"
        if (context.getSystemService(LocationManager::class.java)?.isLocationEnabled != true) return "Turn on device location"
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return "Allow notifications before starting protection"
        if (!fullScreenAlarmReady(context)) return "Allow full-screen alarm notifications"
        val audio = context.getSystemService(AudioManager::class.java)
        if ((audio?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0) <= 0) return "Increase alarm volume before starting protection"
        return null
    }

    fun startJourney(onStarted: () -> Unit) {
        val context = getApplication<Application>()
        if (safetyBlockReason(context) != null) {
            refreshSafetyState()
            return
        }

        val p = _plan.value
        val lat = p.lat.toDoubleOrNull()
        val lon = p.lon.toDoubleOrNull()
        val wake = p.wake.toFloatOrNull()
        val fallbackMinutes = p.fallbackMinutes.toLongOrNull()
        if (p.name.isBlank() || lat == null || lon == null || wake == null || fallbackMinutes == null) return
        if (lat !in -90.0..90.0 || lon !in -180.0..180.0 || wake < 50f || fallbackMinutes < 5L) return

        viewModelScope.launch {
            repo.active()?.let { repo.finish(it.id, false) }
            val id = UUID.randomUUID().toString()
            val startedAt = System.currentTimeMillis()
            val fallbackAt = startedAt + fallbackMinutes * 60_000L
            val entity = JourneyEntity(
                id = id,
                destinationName = p.name.trim(),
                latitude = lat,
                longitude = lon,
                wakeDistanceMeters = wake,
                transport = p.mode,
                protection = p.protection,
                startedAt = startedAt,
                fallbackAlarmAt = fallbackAt
            )
            repo.insert(entity)
            JourneyPrefs(context).startJourney(id, fallbackAt)
            AlarmScheduler.scheduleBackup(context, fallbackAt)
            FirebaseTelemetry.journeyStarted(context, p.mode, p.protection)

            val intent = Intent(context, TrackingService::class.java)
                .putExtra("journey_id", id)
            ContextCompat.startForegroundService(context, intent)
            onStarted()
        }
    }

    fun finishActive() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            repo.active()?.let {
                repo.finish(it.id, false)
                FirebaseTelemetry.journeyEnded(context, false)
            }
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
