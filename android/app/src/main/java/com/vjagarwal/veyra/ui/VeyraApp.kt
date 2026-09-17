package com.vjagarwal.veyra.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun VeyraApp(model: MainViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    val mainRoutes = setOf("home", "history", "saved", "settings")

    MaterialTheme {
        Surface {
            Scaffold(
                bottomBar = {
                    if (route in mainRoutes) {
                        VeyraBottomBar(route) { destination ->
                            if (route != destination) {
                                nav.navigate(destination) {
                                    launchSingleTop = true
                                    popUpTo("home") { saveState = true }
                                    restoreState = true
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                NavHost(nav, startDestination = "home", modifier = Modifier.padding(paddingValues)) {
                    composable("home") {
                        HomeScreen(
                            model = model,
                            start = { nav.navigate("plan") },
                            history = { nav.navigate("history") },
                            saved = { nav.navigate("saved") },
                            settings = { nav.navigate("settings") }
                        )
                    }
                    composable("plan") {
                        PlanJourneyScreen(
                            model = model,
                            back = { nav.popBackStack() },
                            safetyCheck = { nav.navigate("safety") },
                            saved = { nav.navigate("saved") }
                        )
                    }
                    composable("safety") {
                        SafetyCheckScreen(
                            model = model,
                            back = { nav.popBackStack() },
                            start = { nav.navigate("journey") }
                        )
                    }
                    composable("journey") {
                        JourneyScreen(model) { nav.navigate("home") { popUpTo("home") { inclusive = false } } }
                    }
                    composable("history") { HistoryScreen(model) { nav.navigate("home") { popUpTo("home") } } }
                    composable("saved") { SavedPlacesScreen(model, { nav.popBackStack() }, { nav.navigate("plan") }) }
                    composable("settings") { SettingsScreen(model, { nav.navigate("diagnostics") }, { nav.popBackStack() }) }
                    composable("diagnostics") { DiagnosticsScreen(model) { nav.popBackStack() } }
                }
            }
        }
    }
}
