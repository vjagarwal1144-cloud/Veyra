package com.vjagarwal.veyra.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun VeyraApp(model: MainViewModel) {
    val nav = rememberNavController()
    MaterialTheme { Surface { NavHost(nav, startDestination = "home") {
        composable("home") { HomeScreen(model, { nav.navigate("plan") }, { nav.navigate("history") }, { nav.navigate("settings") }) }
        composable("plan") { PlanJourneyScreen(model, { nav.popBackStack() }, { nav.navigate("journey") }) }
        composable("journey") { JourneyScreen(model) { nav.popBackStack("home", false) } }
        composable("history") { HistoryScreen(model) { nav.popBackStack() } }
        composable("settings") { SettingsScreen { nav.navigate("diagnostics") } }
        composable("diagnostics") { DiagnosticsScreen { nav.popBackStack() } }
    } } }
}
