package com.vjagarwal.veyra.ui

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.vjagarwal.veyra.core.destination.DestinationSearch
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun HomeScreen(model: MainViewModel, start: () -> Unit, history: () -> Unit, settings: () -> Unit) {
    val safety by model.safety.collectAsState()
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Veyra", style = MaterialTheme.typography.headlineLarge)
        Text("Never miss your stop.", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Protection health", style = MaterialTheme.typography.titleMedium)
                Text(safety)
                Text("Tracking and alarm protection are designed to work locally without internet.")
            }
        }
        Button(start, Modifier.fillMaxWidth()) { Text("Start Protected Journey") }
        OutlinedButton(history, Modifier.fillMaxWidth()) { Text("Journey History") }
        OutlinedButton(settings, Modifier.fillMaxWidth()) { Text("Settings & Diagnostics") }
    }
}

@Composable
fun PlanJourneyScreen(model: MainViewModel, back: () -> Unit, started: () -> Unit) {
    val current by model.plan.collectAsState()
    val results by model.searchResults.collectAsState()
    val searching by model.searching.collectAsState()
    var local by remember(current) { mutableStateOf(current) }
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Plan protected journey", style = MaterialTheme.typography.headlineSmall)
        Text("Search an Indian destination, confirm it on the free map, then arm local protection.")

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Search destination") }
            )
            Button(
                onClick = { model.searchDestinations(query) },
                enabled = query.trim().length >= 2 && !searching,
                modifier = Modifier.size(width = 92.dp, height = 56.dp)
            ) {
                if (searching) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp)) else Text("Search")
            }
        }

        if (results.isNotEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    results.forEach { result ->
                        DestinationResultRow(result) {
                            model.selectDestination(result)
                            local = local.copy(
                                name = result.name,
                                lat = result.latitude.toString(),
                                lon = result.longitude.toString()
                            )
                            query = result.name
                        }
                    }
                }
            }
        }

        if (local.lat.toDoubleOrNull() != null && local.lon.toDoubleOrNull() != null) {
            DestinationMapPreview(
                latitude = local.lat.toDoubleOrNull() ?: 0.0,
                longitude = local.lon.toDoubleOrNull() ?: 0.0,
                title = local.name.ifBlank { "Destination" }
            )
        }

        OutlinedTextField(local.name, { local = local.copy(name = it) }, Modifier.fillMaxWidth(), label = { Text("Destination name") })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(local.lat, { local = local.copy(lat = it) }, Modifier.weight(1f), label = { Text("Latitude") })
            OutlinedTextField(local.lon, { local = local.copy(lon = it) }, Modifier.weight(1f), label = { Text("Longitude") })
        }
        OutlinedTextField(local.wake, { local = local.copy(wake = it) }, Modifier.fillMaxWidth(), label = { Text("Wake distance (m)") })
        OutlinedTextField(local.fallbackMinutes, { local = local.copy(fallbackMinutes = it) }, Modifier.fillMaxWidth(), label = { Text("Backup alarm after (minutes)") })
        OutlinedTextField(local.mode, { local = local.copy(mode = it) }, Modifier.fillMaxWidth(), label = { Text("Transport mode") })
        OutlinedTextField(local.protection, { local = local.copy(protection = it) }, Modifier.fillMaxWidth(), label = { Text("Protection profile") })
        Text("Backup time is only an estimate; GPS-based local protection remains primary.")

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(back, Modifier.weight(1f)) { Text("Cancel") }
            Button({ model.update(local); model.startJourney(started) }, Modifier.weight(1f)) { Text("Start Protection") }
        }
        OutlinedButton({ context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }) { Text("Check device location") }
    }
}

@Composable
private fun DestinationResultRow(result: DestinationSearch.Result, onSelect: () -> Unit) {
    TextButton(onClick = onSelect, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Text(result.name, style = MaterialTheme.typography.titleSmall)
            Text(result.displayName, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
    }
}

@Composable
private fun DestinationMapPreview(latitude: Double, longitude: Double, title: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestLatitude by rememberUpdatedState(latitude)
    val latestLongitude by rememberUpdatedState(longitude)
    val latestTitle by rememberUpdatedState(title)
    val mapView = remember(context) {
        MapLibre.getInstance(context)
        MapView(context)
    }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapView.onCreate(null)
                mapView.getMapAsync { readyMap ->
                    map = readyMap
                    readyMap.setStyle("https://tiles.openfreemap.org/styles/liberty") {
                        readyMap.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(latestLatitude, latestLongitude))
                            .zoom(15.0)
                            .build()
                        marker = readyMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(latestLatitude, latestLongitude))
                                .title(latestTitle)
                        )
                    }
                }
            }

            override fun onStart(owner: LifecycleOwner) = mapView.onStart()
            override fun onResume(owner: LifecycleOwner) = mapView.onResume()
            override fun onPause(owner: LifecycleOwner) = mapView.onPause()
            override fun onStop(owner: LifecycleOwner) = mapView.onStop()
            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(latitude, longitude, title) {
        val readyMap = map ?: return@LaunchedEffect
        val position = LatLng(latitude, longitude)
        readyMap.cameraPosition = CameraPosition.Builder().target(position).zoom(15.0).build()
        marker?.let { readyMap.removeMarker(it) }
        marker = readyMap.addMarker(MarkerOptions().position(position).title(title))
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxWidth().height(230.dp)
    )
}

@Composable
fun JourneyScreen(model: MainViewModel, done: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Protected Journey", style = MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tracking Guardian: ACTIVE")
                Text("Alarm Guardian: ARMED")
                Text("GPS fixes are checked for freshness, accuracy and implausible jumps before they can influence the alarm.")
                Text("Internet is optional for the safety-critical path.")
            }
        }
        Button({ model.finishActive(); done() }, Modifier.fillMaxWidth()) { Text("End Journey") }
    }
}

@Composable
fun HistoryScreen(model: MainViewModel, back: () -> Unit) {
    val list by model.history.collectAsState(initial = emptyList())
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Journey History", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list) { journey ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(journey.destinationName, style = MaterialTheme.typography.titleMedium)
                        Text("${journey.transport} · wake ${journey.wakeDistanceMeters} m")
                        Text(if (journey.alarmTriggered) "Alarm triggered" else if (journey.active) "Active" else "Ended")
                        journey.lastDistanceMeters?.let { Text("Last distance: ${it.toInt()} m") }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(back) { Text("Back") }
    }
}

@Composable
fun SettingsScreen(openDiagnostics: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Tracking: adaptive high accuracy near destination")
        Text("Location validation: enabled")
        Text("Offline-first protection: enabled")
        Text("Alarm: system alarm stream + vibration")
        if (Build.VERSION.SDK_INT >= 31) {
            OutlinedButton({ context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)) }) { Text("Exact alarm access") }
        }
        if (Build.VERSION.SDK_INT >= 23) {
            OutlinedButton({ context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }) { Text("Battery optimization settings") }
        }
        Button(openDiagnostics) { Text("Advanced diagnostics") }
    }
}

@Composable
fun DiagnosticsScreen(back: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Diagnostics", style = MaterialTheme.typography.headlineSmall)
        Text("GPS watchdog: active while protected journey is running")
        Text("Location age and accuracy: validated")
        Text("Impossible movement jumps: rejected")
        Text("Fallback alarm: AlarmManager")
        Text("Network dependency for alarm: none")
        Text("Map/search: MapLibre + OpenFreeMap + OpenStreetMap")
        Text("OEM force-stop, power loss, revoked permissions, or system restrictions can still prevent app-controlled actions.")
        OutlinedButton(back) { Text("Back") }
    }
}
