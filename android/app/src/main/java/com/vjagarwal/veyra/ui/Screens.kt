package com.vjagarwal.veyra.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diagnostics
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.vjagarwal.veyra.core.destination.DestinationSearch
import com.vjagarwal.veyra.data.JourneyEntity

@Composable
private fun PageColumn(content: @Composable Column.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    model: MainViewModel,
    start: () -> Unit,
    history: () -> Unit,
    saved: () -> Unit,
    settings: () -> Unit
) {
    val safety by model.safety.collectAsState()
    val savedPlaces by model.savedPlaces.collectAsState()
    val journeys by model.history.collectAsState(initial = emptyList())
    val active = journeys.firstOrNull { it.active }

    PageColumn {
        Text("Veyra", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("Never miss your stop.", style = MaterialTheme.typography.titleMedium)

        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Protection status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                Text(safety)
                Text("Core tracking and alarm logic runs on-device. Internet is an enhancement, not the safety-critical dependency.")
            }
        }

        if (active != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Journey in progress", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(active.destinationName, style = MaterialTheme.typography.titleMedium)
                    Text("Alarm armed · ${active.wakeDistanceMeters.toInt()} m wake zone · ${active.transport}")
                    Button({ start() }, Modifier.fillMaxWidth()) { Text("Open Journey") }
                }
            }
        } else {
            Button(start, Modifier.fillMaxWidth().height(56.dp)) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Plan Protected Journey")
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction(Icons.Default.Bookmark, "Saved (${savedPlaces.size})", saved, Modifier.weight(1f))
            QuickAction(Icons.Default.History, "History", history, Modifier.weight(1f))
            QuickAction(Icons.Default.Settings, "Settings", settings, Modifier.weight(1f))
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("How Veyra protects you", style = MaterialTheme.typography.titleLarge)
                InfoRow(Icons.Default.LocationOn, "Location Guardian", "Checks freshness, accuracy, movement and proximity")
                InfoRow(Icons.Default.Bolt, "Alarm Guardian", "Uses a local alarm plus time fallback")
                InfoRow(Icons.Default.Map, "Smart destination", "Search and map tools help you confirm the stop")
            }
        }
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(72.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(icon, null)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanJourneyScreen(
    model: MainViewModel,
    back: () -> Unit,
    safetyCheck: () -> Unit,
    saved: () -> Unit
) {
    val current by model.plan.collectAsState()
    val results by model.searchResults.collectAsState()
    val searching by model.searching.collectAsState()
    val savedPlaces by model.savedPlaces.collectAsState()
    var local by remember(current) { mutableStateOf(current) }
    var query by remember { mutableStateOf("") }
    var showWakeDialog by remember { mutableStateOf(false) }
    var showFallbackDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(back) { Icon(Icons.Default.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) {
                Text("Plan journey", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Set your destination and protection profile")
            }
            IconButton(saved) { Icon(Icons.Default.Bookmark, "Saved places") }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Destination", style = MaterialTheme.typography.titleLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        label = { Text("Search destination") }
                    )
                    Button(
                        onClick = { model.searchDestinations(query) },
                        enabled = query.trim().length >= 2 && !searching,
                        modifier = Modifier.height(56.dp)
                    ) {
                        if (searching) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Find")
                    }
                }

                if (results.isNotEmpty()) {
                    results.take(6).forEach { result ->
                        DestinationResultRow(result) {
                            model.selectDestination(result)
                            local = local.copy(name = result.name, lat = result.latitude.toString(), lon = result.longitude.toString())
                            query = result.name
                        }
                    }
                }

                if (savedPlaces.isNotEmpty()) {
                    Text("Recent favourites", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        savedPlaces.take(3).forEach { place ->
                            AssistChip(onClick = { model.selectSavedPlace(place); local = model.plan.value }, label = { Text(place.name) })
                        }
                    }
                }
            }
        }

        if (local.lat.toDoubleOrNull() != null && local.lon.toDoubleOrNull() != null) {
            DestinationMapPreview(local.lat.toDouble(), local.lon.toDouble(), local.name.ifBlank { "Destination" })
        }

        OutlinedTextField(
            local.name,
            { local = local.copy(name = it) },
            Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Destination name") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null) }
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(local.lat, { local = local.copy(lat = it) }, Modifier.weight(1f), label = { Text("Latitude") })
            OutlinedTextField(local.lon, { local = local.copy(lon = it) }, Modifier.weight(1f), label = { Text("Longitude") })
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Wake distance", style = MaterialTheme.typography.titleMedium)
                Button({ showWakeDialog = true }, Modifier.fillMaxWidth()) { Text("${local.wake} m · change") }
                Text("The alarm zone is a decision signal, not a guarantee of exact physical position.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Text("Transport", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Auto", "Train", "Metro", "Bus", "Cab").forEach { mode ->
                FilterChip(selected = local.mode == mode, onClick = { local = local.copy(mode = mode) }, label = { Text(mode) })
            }
        }

        Text("Protection", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Battery Saver", "Balanced", "Maximum").forEach { profile ->
                FilterChip(selected = local.protection == profile, onClick = { local = local.copy(protection = profile) }, label = { Text(profile) })
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Backup alarm", style = MaterialTheme.typography.titleMedium)
                Button({ showFallbackDialog = true }, Modifier.fillMaxWidth()) { Text("${local.fallbackMinutes} minutes · change") }
                Text("This is only a time estimate and is never the primary location trigger.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton({ model.saveCurrentPlace(); saved() }, Modifier.weight(1f), enabled = local.name.isNotBlank()) {
                Icon(Icons.Default.Save, null); Spacer(Modifier.width(6.dp)); Text("Save Place")
            }
            OutlinedButton({ model.clearPlan(); local = model.plan.value }, Modifier.weight(1f)) {
                Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Reset")
            }
        }

        Button({ model.update(local); safetyCheck() }, Modifier.fillMaxWidth().height(56.dp), enabled = local.name.isNotBlank()) {
            Icon(Icons.Default.Shield, null)
            Spacer(Modifier.width(8.dp))
            Text("Continue to Safety Check")
        }
        OutlinedButton({ context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }, Modifier.fillMaxWidth()) {
            Icon(Icons.Default.LocationOn, null); Spacer(Modifier.width(6.dp)); Text("Open device location")
        }
    }

    if (showWakeDialog) {
        ChoiceDialog("Wake distance", listOf(500, 1000, 2000, 5000), local.wake.toIntOrNull() ?: 1000, { value ->
            local = local.copy(wake = value.toString()); model.setWakeDistance(value); showWakeDialog = false
        }, { showWakeDialog = false })
    }
    if (showFallbackDialog) {
        ChoiceDialog("Backup alarm", listOf(30, 60, 90, 120, 180), local.fallbackMinutes.toIntOrNull() ?: 90, { value ->
            local = local.copy(fallbackMinutes = value.toString()); model.setFallbackMinutes(value); showFallbackDialog = false
        }, { showFallbackDialog = false })
    }
}

@Composable
private fun ChoiceDialog(title: String, values: List<Int>, selected: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                values.forEach { value ->
                    val label = if (title.contains("distance", true)) "${value} m" else "${value} min"
                    Button(onClick = { onSelect(value) }, Modifier.fillMaxWidth()) { Text(if (value == selected) "✓ $label" else label) }
                }
            }
        },
        confirmButton = {}
    )
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
    val html = remember(latitude, longitude, title) {
        val safeTitle = title.replace("\\", "\\\\").replace("'", "\\'")
        """
        <!doctype html><html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
        <style>html,body,#map{margin:0;width:100%;height:100%;}body{overflow:hidden}</style>
        </head><body><div id="map"></div>
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <script>
        const lat=$latitude, lon=$longitude;
        const map=L.map('map',{zoomControl:true}).setView([lat,lon],15);
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; OpenStreetMap contributors'}).addTo(map);
        L.marker([lat,lon]).addTo(map).bindPopup('$safeTitle').openPopup();
        </script></body></html>
        """.trimIndent()
    }
    AndroidView(
        factory = {
            WebView(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                webViewClient = WebViewClient()
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
                }
                loadDataWithBaseURL("https://appassets.androidplatform.net/", html, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxWidth().height(230.dp)
    )
}

@Composable
fun SafetyCheckScreen(model: MainViewModel, back: () -> Unit, start: () -> Unit) {
    val items by model.safetyItems.collectAsState()
    val safety by model.safety.collectAsState()
    val valid = model.planValid()

    LaunchedEffect(Unit) { model.refreshSafetyState() }

    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(back) { Icon(Icons.Default.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) {
                Text("Safety check", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Confirm the device is ready before arming protection")
            }
        }

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(safety, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${items.count { it.ok }} of ${items.size} checks currently pass")
            }
        }

        items.forEach { CheckRow(it.title, it.ok, it.detail) }
        CheckRow("Destination", valid, if (valid) "Valid destination and protection settings" else "Return to planning and complete destination fields")

        if (!valid || items.any { !it.ok && it.title in setOf("Precise location", "Device location", "Notifications", "Full-screen alarm", "Alarm volume") }) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Do not start yet", fontWeight = FontWeight.Bold)
                    Text("Resolve the required checks first. Veyra cannot guarantee operation after force-stop, battery loss, revoked permissions or OEM restrictions.")
                    OutlinedButton({ model.refreshSafetyState() }) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Recheck") }
                }
            }
        }

        Button(start, Modifier.fillMaxWidth().height(56.dp), enabled = valid && items.filter { it.title in setOf("Precise location", "Device location", "Notifications", "Full-screen alarm", "Alarm volume") }.all { it.ok }) {
            Icon(Icons.Default.Lock, null); Spacer(Modifier.width(8.dp)); Text("Start Protected Journey")
        }
    }
}

@Composable
private fun CheckRow(title: String, ok: Boolean, detail: String) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (ok) Icons.Default.CheckCircle else Icons.Default.Warning, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyScreen(model: MainViewModel, backHome: () -> Unit) {
    val journeys by model.history.collectAsState(initial = emptyList())
    val active = journeys.firstOrNull { it.active }
    val context = LocalContext.current

    if (active == null) {
        PageColumn {
            Text("No active journey", style = MaterialTheme.typography.headlineSmall)
            Text("Start a protected journey from Home to see live protection data here.")
            Button(backHome, Modifier.fillMaxWidth()) { Text("Back to Home") }
        }
        return
    }

    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(backHome) { Icon(Icons.Default.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) {
                Text("Protected journey", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${active.transport} · ${active.protection}")
            }
            AssistChip(onClick = {}, label = { Text("ARMED") })
        }

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(active.destinationName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Wake zone: ${active.wakeDistanceMeters.toInt()} m")
                Text("Journey ID: ${active.id.take(8)}…", style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(Icons.Default.Route, "Distance", active.lastDistanceMeters?.let { "${it.toInt()} m" } ?: "Waiting", Modifier.weight(1f))
            MetricCard(Icons.Default.SignalCellularAlt, "Accuracy", active.lastAccuracyMeters?.let { "${it.toInt()} m" } ?: "Waiting", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(Icons.Default.Speed, "GPS", if (active.lastLocationAt != null) "Live" else "Acquiring", Modifier.weight(1f))
            MetricCard(Icons.Default.Bolt, "Alarm", "Armed", Modifier.weight(1f))
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tracking Guardian", style = MaterialTheme.typography.titleLarge)
                Text("High-quality location fixes are validated before the alarm decision. The app rejects stale data and implausible movement.")
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("Internet status is not part of the core alarm decision.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Alarm Guardian", style = MaterialTheme.typography.titleLarge)
                Text("Primary: proximity/confidence trigger")
                Text("Backup: AlarmManager time estimate")
                Text("Escalation: vibration + alarm notification + full-screen alarm activity")
            }
        }

        OutlinedButton({ context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }, Modifier.fillMaxWidth()) {
            Icon(Icons.Default.LocationOn, null); Spacer(Modifier.width(6.dp)); Text("Open location settings")
        }
        Button({ model.finishActive(); backHome() }, Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Icon(Icons.Default.Stop, null); Spacer(Modifier.width(6.dp)); Text("End Protected Journey")
        }
    }
}

@Composable
private fun MetricCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null)
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun HistoryScreen(model: MainViewModel, back: () -> Unit) {
    val list by model.history.collectAsState(initial = emptyList())
    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(back) { Icon(Icons.Default.ArrowBack, "Back") }
            Text("Journey history", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        if (list.isEmpty()) Text("No journeys yet. Your completed and active journeys will appear here.")
        list.forEach { JourneyHistoryCard(it) }
    }
}

@Composable
private fun JourneyHistoryCard(journey: JourneyEntity) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Route, null)
                Spacer(Modifier.width(8.dp))
                Text(journey.destinationName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (journey.alarmTriggered) AssistChip(onClick = {}, label = { Text("Alarm") })
            }
            Text("${journey.transport} · ${journey.protection} · wake ${journey.wakeDistanceMeters.toInt()} m")
            Text(if (journey.active) "Currently active" else "Ended ${journey.endedAt?.let { java.text.DateFormat.getDateTimeInstance().format(java.util.Date(it)) } ?: ""}")
            journey.lastDistanceMeters?.let { Text("Last distance: ${it.toInt()} m") }
            journey.lastAccuracyMeters?.let { Text("Last accuracy: ${it.toInt()} m") }
        }
    }
}

@Composable
fun SavedPlacesScreen(model: MainViewModel, back: () -> Unit, plan: () -> Unit) {
    val places by model.savedPlaces.collectAsState()
    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(back) { Icon(Icons.Default.ArrowBack, "Back") }
            Text("Saved places", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        if (places.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Bookmark, null, Modifier.size(42.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No saved places yet", style = MaterialTheme.typography.titleLarge)
                    Text("Save a destination from the journey planner for one-tap reuse.")
                }
            }
        }
        places.forEach { place ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(place.name, style = MaterialTheme.typography.titleMedium)
                        Text("${place.latitude}, ${place.longitude}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton({ model.selectSavedPlace(place); plan() }) { Icon(Icons.Default.EditLocation, "Use") }
                    IconButton({ model.removeSavedPlace(place.name) }) { Icon(Icons.Default.Delete, "Delete") }
                }
            }
        }
        Button(plan, Modifier.fillMaxWidth()) { Icon(Icons.Default.AddLocation, null); Spacer(Modifier.width(6.dp)); Text("Add from planner") }
    }
}

@Composable
fun SettingsScreen(model: MainViewModel, openDiagnostics: () -> Unit, back: () -> Unit) {
    val settings by model.settings.collectAsState()
    val context = LocalContext.current
    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(back) { Icon(Icons.Default.ArrowBack, "Back") }
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        SettingsSection("Tracking") {
            SettingSwitch("Adaptive tracking", "Increase GPS frequency near the destination", settings.adaptiveTracking) { model.updateSetting("adaptive", it) }
            SettingSwitch("High accuracy", "Prefer the most accurate Android location source", settings.highAccuracy) { model.updateSetting("accuracy", it) }
            SettingSwitch("Geofence backup", "Keep redundant proximity monitoring enabled", settings.geofenceBackup) { model.updateSetting("geofence", it) }
            SettingSwitch("Motion detection", "Use movement signals to reject impossible jumps", settings.motionDetection) { model.updateSetting("motion", it) }
        }

        SettingsSection("Alarm") {
            SettingSwitch("Repeat alarm", "Keep escalating until the user stops the alarm", settings.repeatAlarm) { model.updateSetting("repeat", it) }
            SettingSwitch("Vibration", "Use device vibration when the alarm fires", settings.vibration) { model.updateSetting("vibration", it) }
            SettingSwitch("Earphone behavior", "Keep alarm audible with connected audio devices", settings.earphoneAlarm) { model.updateSetting("earphone", it) }
            Text("Alarm stream volume is controlled by Android system settings.")
            OutlinedButton({ context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS)) }, Modifier.fillMaxWidth()) { Text("Open sound settings") }
        }

        SettingsSection("Reliability") {
            SettingSwitch("Battery warnings", "Warn when OEM battery optimization may affect reliability", settings.batteryWarnings) { model.updateSetting("battery", it) }
            SettingSwitch("Internet warnings", "Show optional network availability warnings", settings.internetWarnings) { model.updateSetting("internet", it) }
            OutlinedButton({ context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }, Modifier.fillMaxWidth()) { Icon(Icons.Default.BatteryAlert, null); Spacer(Modifier.width(6.dp)); Text("Battery optimization") }
            if (Build.VERSION.SDK_INT >= 31) {
                OutlinedButton({ context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)) }, Modifier.fillMaxWidth()) { Text("Exact alarm access") }
            }
        }

        SettingsSection("Privacy & data") {
            Text("Journey records and saved places are stored locally on this device unless cloud features are explicitly connected.")
            OutlinedButton({}, Modifier.fillMaxWidth()) { Text("Local data export — coming next") }
            OutlinedButton({}, Modifier.fillMaxWidth()) { Text("Delete local journey history — coming next") }
        }

        SettingsSection("Diagnostics") {
            Button(openDiagnostics, Modifier.fillMaxWidth()) { Icon(Icons.Default.Diagnostics, null); Spacer(Modifier.width(6.dp)); Text("Open advanced diagnostics") }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable Column.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = { Text(title, style = MaterialTheme.typography.titleLarge); Divider(); content() })
    }
}

@Composable
private fun SettingSwitch(title: String, detail: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked, onCheckedChange = onChange)
    }
}

@Composable
fun DiagnosticsScreen(model: MainViewModel, back: () -> Unit) {
    val context = LocalContext.current
    val safety by model.safety.collectAsState()
    val items by model.safetyItems.collectAsState()

    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(back) { Icon(Icons.Default.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) {
                Text("Diagnostics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("See the protection stack and test local capabilities")
            }
        }

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Overall", style = MaterialTheme.typography.titleMedium)
                Text(safety, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        items.forEach { CheckRow(it.title, it.ok, it.detail) }

        SettingsSection("Protection engine") {
            InfoRow(Icons.Default.LocationOn, "GPS watchdog", "Tracks location freshness and validates reported fixes")
            InfoRow(Icons.Default.Speed, "Movement validation", "Rejects implausible speeds and jumps")
            InfoRow(Icons.Default.Route, "Proximity engine", "Uses distance, accuracy, movement trend and direction")
            InfoRow(Icons.Default.Bolt, "Alarm fallback", "AlarmManager provides a time-based backup")
        }

        SettingsSection("Tests") {
            Button({ model.testAlarm() }, Modifier.fillMaxWidth()) { Icon(Icons.Default.NotificationsActive, null); Spacer(Modifier.width(6.dp)); Text("Test alarm notification") }
            OutlinedButton({ model.refreshSafetyState() }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Run safety checks again") }
            OutlinedButton({ context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }, Modifier.fillMaxWidth()) { Text("Open location services") }
            OutlinedButton({ context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS)) }, Modifier.fillMaxWidth()) { Text("Open sound settings") }
        }

        SettingsSection("Limitations") {
            Text("Android and phone manufacturers can stop application-controlled work after force-stop, battery depletion, revoked permission, system restrictions or severe OEM background limits.")
            Text("The app is designed for reliability, not a 100% guarantee.")
        }
    }
}

@Composable
fun VeyraBottomBar(selected: String, navigate: (String) -> Unit) {
    NavigationBar(Modifier.navigationBarsPadding()) {
        NavigationBarItem(selected == "home", { navigate("home") }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
        NavigationBarItem(selected == "history", { navigate("history") }, icon = { Icon(Icons.Default.History, null) }, label = { Text("History") })
        NavigationBarItem(selected == "saved", { navigate("saved") }, icon = { Icon(Icons.Default.Bookmark, null) }, label = { Text("Saved") })
        NavigationBarItem(selected == "settings", { navigate("settings") }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") })
    }
}
