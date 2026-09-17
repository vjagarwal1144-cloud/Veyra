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
import com.vjagarwal.veyra.core.alarm.AlarmNotifier
import com.vjagarwal.veyra.core.alarm.AlarmScheduler
import com.vjagarwal.veyra.core.common.JourneyPrefs
import com.vjagarwal.veyra.core.common.SavedPlacesPrefs
import com.vjagarwal.veyra.core.common.VeyraSettingsPrefs
import com.vjagarwal.veyra.core.destination.DestinationSearch
import com.vjagarwal.veyra.core.location.TrackingService
import com.vjagarwal.veyra.data.JourneyEntity
import com.vjagarwal.veyra.data.JourneyRepository
import com.vjagarwal.veyra.data.firebase.FirebaseTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(app: Application, private val repo: JourneyRepository) : AndroidViewModel(app) {
    private val context get() = getApplication<Application>()
    private val settingsPrefs = VeyraSettingsPrefs(context)
    private val savedPrefs = SavedPlacesPrefs(context)

    data class Plan(
        val name: String = "",
        val lat: String = "",
        val lon: String = "",
        val wake: String = "1000",
        val fallbackMinutes: String = "90",
        val mode: String = "Auto",
        val protection: String = "Balanced"
    )

    data class SafetyItem(val title: String, val ok: Boolean, val detail: String)
    data class UserSettings(
        val adaptiveTracking: Boolean = settingsPrefs.adaptiveTracking,
        val highAccuracy: Boolean = settingsPrefs.highAccuracy,
        val geofenceBackup: Boolean = settingsPrefs.geofenceBackup,
        val motionDetection: Boolean = settingsPrefs.motionDetection,
        val repeatAlarm: Boolean = settingsPrefs.repeatAlarm,
        val batteryWarnings: Boolean = settingsPrefs.batteryWarnings,
        val internetWarnings: Boolean = settingsPrefs.internetWarnings,
        val earphoneAlarm: Boolean = settingsPrefs.earphoneAlarm,
        val vibration: Boolean = settingsPrefs.vibration,
        val wakeDistance: Int = settingsPrefs.wakeDistance,
        val fallbackMinutes: Int = settingsPrefs.fallbackMinutes
    )

    private val _safety = MutableStateFlow("Checking device protection…")
    val safety: StateFlow<String> = _safety.asStateFlow()

    private val _safetyItems = MutableStateFlow<List<SafetyItem>>(emptyList())
    val safetyItems: StateFlow<List<SafetyItem>> = _safetyItems.asStateFlow()

    private val _plan = MutableStateFlow(
        Plan(wake = settingsPrefs.wakeDistance.toString(), fallbackMinutes = settingsPrefs.fallbackMinutes.toString())
    )
    val plan: StateFlow<Plan> = _plan.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DestinationSearch.Result>>(emptyList())
    val searchResults: StateFlow<List<DestinationSearch.Result>> = _searchResults.asStateFlow()
    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    private val _savedPlaces = MutableStateFlow(savedPrefs.list())
    val savedPlaces: StateFlow<List<SavedPlacesPrefs.Place>> = _savedPlaces.asStateFlow()

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    val history = repo.history()

    init { refreshSafetyState() }

    fun update(plan: Plan) { _plan.value = plan }

    fun clearPlan() {
        _plan.value = Plan(wake = settingsPrefs.wakeDistance.toString(), fallbackMinutes = settingsPrefs.fallbackMinutes.toString())
        _searchResults.value = emptyList()
    }

    fun setWakeDistance(meters: Int) {
        _plan.value = _plan.value.copy(wake = meters.toString())
        settingsPrefs.wakeDistance = meters
        _settings.value = _settings.value.copy(wakeDistance = meters)
    }

    fun setFallbackMinutes(minutes: Int) {
        _plan.value = _plan.value.copy(fallbackMinutes = minutes.toString())
        settingsPrefs.fallbackMinutes = minutes
        _settings.value = _settings.value.copy(fallbackMinutes = minutes)
    }

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

    fun clearSearch() { _searchResults.value = emptyList() }

    fun selectDestination(result: DestinationSearch.Result) {
        _plan.value = _plan.value.copy(
            name = result.name,
            lat = result.latitude.toString(),
            lon = result.longitude.toString()
        )
        clearSearch()
    }

    fun selectSavedPlace(place: SavedPlacesPrefs.Place) {
        _plan.value = _plan.value.copy(
            name = place.name,
            lat = place.latitude.toString(),
            lon = place.longitude.toString()
        )
    }

    fun saveCurrentPlace() {
        val p = _plan.value
        val lat = p.lat.toDoubleOrNull() ?: return
        val lon = p.lon.toDoubleOrNull() ?: return
        if (p.name.isBlank() || lat !in -90.0..90.0 || lon !in -180.0..180.0) return
        savedPrefs.save(SavedPlacesPrefs.Place(p.name.trim(), lat, lon))
        _savedPlaces.value = savedPrefs.list()
    }

    fun removeSavedPlace(name: String) {
        savedPrefs.remove(name)
        _savedPlaces.value = savedPrefs.list()
    }

    fun updateSetting(name: String, enabled: Boolean) {
        when (name) {
            "adaptive" -> settingsPrefs.adaptiveTracking = enabled
            "accuracy" -> settingsPrefs.highAccuracy = enabled
            "geofence" -> settingsPrefs.geofenceBackup = enabled
            "motion" -> settingsPrefs.motionDetection = enabled
            "repeat" -> settingsPrefs.repeatAlarm = enabled
            "battery" -> settingsPrefs.batteryWarnings = enabled
            "internet" -> settingsPrefs.internetWarnings = enabled
            "earphone" -> settingsPrefs.earphoneAlarm = enabled
            "vibration" -> settingsPrefs.vibration = enabled
        }
        _settings.value = _settings.value.copy(
            adaptiveTracking = settingsPrefs.adaptiveTracking,
            highAccuracy = settingsPrefs.highAccuracy,
            geofenceBackup = settingsPrefs.geofenceBackup,
            motionDetection = settingsPrefs.motionDetection,
            repeatAlarm = settingsPrefs.repeatAlarm,
            batteryWarnings = settingsPrefs.batteryWarnings,
            internetWarnings = settingsPrefs.internetWarnings,
            earphoneAlarm = settingsPrefs.earphoneAlarm,
            vibration = settingsPrefs.vibration
        )
    }

    private fun fullScreenAlarmReady(): Boolean =
        Build.VERSION.SDK_INT < 34 || context.getSystemService(NotificationManager::class.java)?.canUseFullScreenIntent() == true

    fun refreshSafetyState() {
        val locationManager = context.getSystemService(LocationManager::class.java)
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val locationEnabled = locationManager?.isLocationEnabled == true
        val notifications = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val fullScreenReady = fullScreenAlarmReady()
        val exactAlarm = AlarmScheduler.canUseExactAlarm(context)
        val audio = context.getSystemService(AudioManager::class.java)
        val alarmVolumeOk = (audio?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0) > 0
        val batteryIgnoring = if (Build.VERSION.SDK_INT >= 23) {
            context.getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(context.packageName) == true
        } else true

        val items = listOf(
            SafetyItem("Precise location", fineGranted, if (fineGranted) "Granted" else "Allow precise location"),
            SafetyItem("Device location", locationEnabled, if (locationEnabled) "Location services are on" else "Turn on Location"),
            SafetyItem("Notifications", notifications, if (notifications) "Enabled" else "Allow Veyra notifications"),
            SafetyItem("Full-screen alarm", fullScreenReady, if (fullScreenReady) "Available" else "Allow full-screen alarm"),
            SafetyItem("Alarm volume", alarmVolumeOk, if (alarmVolumeOk) "Volume is above zero" else "Increase alarm volume"),
            SafetyItem("Exact alarm", exactAlarm, if (exactAlarm) "Available" else "Time fallback may be less precise"),
            SafetyItem("Battery optimization", batteryIgnoring, if (batteryIgnoring) "Veyra is allowed to run reliably" else "Consider excluding Veyra from optimization")
        )
        _safetyItems.value = items
        _safety.value = when {
            items.any { !it.ok && it.title in setOf("Precise location", "Device location", "Notifications", "Full-screen alarm", "Alarm volume") } -> "Action required before protected journey"
            items.any { !it.ok } -> "Protection ready with a reliability warning"
            else -> "Protection checks passed"
        }
    }

    fun planValid(): Boolean {
        val p = _plan.value
        val lat = p.lat.toDoubleOrNull()
        val lon = p.lon.toDoubleOrNull()
        val wake = p.wake.toFloatOrNull()
        val fallback = p.fallbackMinutes.toLongOrNull()
        return p.name.isNotBlank() && lat != null && lon != null && wake != null && fallback != null &&
            lat in -90.0..90.0 && lon in -180.0..180.0 && wake >= 50f && fallback >= 5
    }

    private fun safetyBlockReason(): String? {
        val required = _safetyItems.value.filter { it.title in setOf("Precise location", "Device location", "Notifications", "Full-screen alarm", "Alarm volume") }
        return required.firstOrNull { !it.ok }?.detail
    }

    fun startJourney(onStarted: () -> Unit) {
        refreshSafetyState()
        if (!planValid() || safetyBlockReason() != null) return
        val p = _plan.value
        val lat = p.lat.toDoubleOrNull() ?: return
        val lon = p.lon.toDoubleOrNull() ?: return
        val wake = p.wake.toFloatOrNull() ?: return
        val fallbackMinutes = p.fallbackMinutes.toLongOrNull() ?: return

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
            ContextCompat.startForegroundService(context, Intent(context, TrackingService::class.java).putExtra("journey_id", id))
            onStarted()
        }
    }

    fun finishActive() {
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

    fun testAlarm() {
        AlarmNotifier.show(context, "Veyra alarm test — wake protection is working")
    }

    fun openAlarmVolumeSettings(): Intent = Intent("android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS")

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
