package com.vjagarwal.veyra.ui

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
import androidx.compose.ui.unit.dp

@Composable fun HomeScreen(model: MainViewModel, start: () -> Unit, history: () -> Unit, settings: () -> Unit) {
    val safety by model.safety.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Veyra")
        Text("Never miss your stop.")
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Protection health"); Text(safety) } }
        Button(start, Modifier.fillMaxWidth()) { Text("Start Journey") }
        OutlinedButton(history, Modifier.fillMaxWidth()) { Text("Journey History") }
        OutlinedButton(settings, Modifier.fillMaxWidth()) { Text("Settings & Diagnostics") }
    }
}

@Composable fun PlanJourneyScreen(model: MainViewModel, back: () -> Unit, started: () -> Unit) {
    val p by model.plan.collectAsState(); var local by remember { mutableStateOf(p) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Plan protected journey")
        OutlinedTextField(local.name, { local = local.copy(name = it); model.update(local) }, label = { Text("Destination name") })
        OutlinedTextField(local.lat, { local = local.copy(lat = it); model.update(local) }, label = { Text("Latitude") })
        OutlinedTextField(local.lon, { local = local.copy(lon = it); model.update(local) }, label = { Text("Longitude") })
        OutlinedTextField(local.wake, { local = local.copy(wake = it); model.update(local) }, label = { Text("Wake distance (meters)") })
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(back) { Text("Cancel") }; Button({ model.update(local); model.startJourney(started) }) { Text("Start Protected Journey") } }
    }
}

@Composable fun JourneyScreen(model: MainViewModel, done: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { Text("Protected Journey"); Text("Tracking Guardian: ACTIVE"); Text("Alarm Guardian: ARMED"); Text("Location, accuracy and movement are validated on-device."); Button(done, Modifier.fillMaxWidth()) { Text("End Journey") } }
}

@Composable fun HistoryScreen(model: MainViewModel, back: () -> Unit) {
    val list by model.history.collectAsState(initial = emptyList())
    Column(Modifier.fillMaxSize().padding(20.dp)) { Text("Journey History"); Spacer(Modifier.height(12.dp)); LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(list) { j -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(j.destinationName); Text("${j.transport} · ${j.wakeDistanceMeters} m · ${if (j.alarmTriggered) "Alarm triggered" else "Completed/active"}") } } } }; Spacer(Modifier.height(12.dp)); OutlinedButton(back) { Text("Back") } }
}

@Composable fun SettingsScreen(openDiagnostics: () -> Unit) { Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Settings"); Text("Tracking frequency: adaptive"); Text("Location validation: enabled"); Text("Geofence backup: enabled in release configuration"); Text("Alarm volume: system alarm volume"); Text("Offline-first protection: enabled"); Button(openDiagnostics) { Text("Advanced diagnostics") } } }

@Composable fun DiagnosticsScreen(back: () -> Unit) { Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Diagnostics"); Text("GPS watchdog: ready"); Text("Foreground service: managed by Android"); Text("Alarm fallback: AlarmManager"); Text("Network: optional"); Text("A future release can add GPS test, sensor test and journey simulator here."); OutlinedButton(back) { Text("Back") } } }
