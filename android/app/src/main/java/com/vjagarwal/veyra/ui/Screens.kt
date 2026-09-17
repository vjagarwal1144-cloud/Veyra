package com.vjagarwal.veyra.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(model: MainViewModel, start: () -> Unit, history: () -> Unit, settings: () -> Unit) {
    val safety by model.safety.collectAsState()
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
    var local by remember(current) { mutableStateOf(current) }
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Plan protected journey", style = MaterialTheme.typography.headlineSmall)
        Text("Use a destination coordinate, or later connect the optional Places/Maps search backend.")
        OutlinedTextField(local.name, { local = local.copy(name = it) }, Modifier.fillMaxWidth(), label = { Text("Destination name") })
        OutlinedTextField(local.lat, { local = local.copy(lat = it) }, Modifier.fillMaxWidth(), label = { Text("Latitude") })
        OutlinedTextField(local.lon, { local = local.copy(lon = it) }, Modifier.fillMaxWidth(), label = { Text("Longitude") })
        OutlinedTextField(local.wake, { local = local.copy(wake = it) }, Modifier.fillMaxWidth(), label = { Text("Wake distance (m)") })
        OutlinedTextField(local.fallbackMinutes, { local = local.copy(fallbackMinutes = it) }, Modifier.fillMaxWidth(), label = { Text("Fallback alarm after (minutes)") })
        OutlinedTextField(local.mode, { local = local.copy(mode = it) }, Modifier.fillMaxWidth(), label = { Text("Transport mode") })
        OutlinedTextField(local.protection, { local = local.copy(protection = it) }, Modifier.fillMaxWidth(), label = { Text("Protection profile") })
        Spacer(Modifier.height(4.dp))
        Text("Fallback time is only a backup estimate; GPS-based protection remains primary.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(back, Modifier.weight(1f)) { Text("Cancel") }
            Button({ model.update(local); model.startJourney(started) }, Modifier.weight(1f)) { Text("Start") }
        }
        OutlinedButton({ context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }) { Text("Check device location") }
    }
}

@Composable
fun JourneyScreen(model: MainViewModel, done: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Protected Journey", style = MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tracking Guardian: ACTIVE")
                Text("Alarm Guardian: ARMED")
                Text("GPS fixes are checked for freshness, accuracy and implausible jumps before they can influence the alarm.")
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
        Text("Tracking: adaptive, high accuracy near destination")
        Text("Location validation: enabled")
        Text("Offline-first protection: enabled")
        Text("Alarm: system alarm stream + vibration")
        OutlinedButton({ context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)) }) { Text("Exact alarm access") }
        OutlinedButton({ context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }) { Text("Battery optimization settings") }
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
        Text("OEM force-stop or power loss can still prevent application-controlled actions; Veyra cannot override Android system controls.")
        OutlinedButton(back) { Text("Back") }
    }
}
