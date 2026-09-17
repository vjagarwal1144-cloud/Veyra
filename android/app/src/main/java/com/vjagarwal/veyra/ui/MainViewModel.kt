package com.vjagarwal.veyra.ui

import android.app.Application
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vjagarwal.veyra.VeyraApplication
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

    data class Plan(val name: String = "", val lat: String = "", val lon: String = "", val wake: String = "1000", val mode: String = "Auto", val protection: String = "Balanced")
    private val _plan = MutableStateFlow(Plan())
    val plan: StateFlow<Plan> = _plan.asStateFlow()

    fun update(plan: Plan) { _plan.value = plan }
    fun refreshSafetyState() {
        val lm = getApplication<Application>().getSystemService(LocationManager::class.java)
        _safety.value = if (lm?.isLocationEnabled == true) "Location services ready" else "Turn on location services"
    }
    fun startJourney(onStarted: () -> Unit) {
        val p = _plan.value; val lat = p.lat.toDoubleOrNull(); val lon = p.lon.toDoubleOrNull(); val wake = p.wake.toFloatOrNull()
        if (p.name.isBlank() || lat == null || lon == null || wake == null) return
        viewModelScope.launch {
            val entity = JourneyEntity(UUID.randomUUID().toString(), p.name, lat, lon, wake, p.mode, p.protection, System.currentTimeMillis())
            repo.insert(entity)
            val intent = android.content.Intent(getApplication(), com.vjagarwal.veyra.core.location.TrackingService::class.java).putExtra("lat", lat).putExtra("lon", lon).putExtra("wake", wake)
            androidx.core.content.ContextCompat.startForegroundService(getApplication(), intent)
            onStarted()
        }
    }
    fun finish(id: String, alarm: Boolean = false) { viewModelScope.launch { repo.finish(id, alarm) } }

    companion object {
        fun factory(application: Application) = object : ViewModelProvider.AndroidViewModelFactory(application) {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val db = (application as VeyraApplication).database
                @Suppress("UNCHECKED_CAST") return MainViewModel(application, JourneyRepository(db.journeyDao())) as T
            }
        }
    }
}
