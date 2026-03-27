package com.example.pomodoro.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pomodoro.viewmodel.TimerViewModel

sealed class Screen(val route: String, val label: String) {
    object Timer   : Screen("timer",    "タイマー")
    object WorkLog : Screen("worklog",  "ログ")
    object Settings: Screen("settings", "設定")
}

@Composable
fun PomotimerApp(vm: TimerViewModel = viewModel()) {
    val navController = rememberNavController()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val workLogs by vm.workLogs.collectAsStateWithLifecycle(emptyList())
    val notifEnabled by vm.settings.notificationEnabled.collectAsStateWithLifecycle(true)
    val soundEnabled by vm.settings.soundEnabled.collectAsStateWithLifecycle(true)

    val items = listOf(
        Triple(Screen.Timer,    Icons.Default.Timer,    "タイマー"),
        Triple(Screen.WorkLog,  Icons.Default.History,  "ログ"),
        Triple(Screen.Settings, Icons.Default.Settings, "設定"),
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val current = navBackStackEntry?.destination
                items.forEach { (screen, icon, label) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = current?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Timer.route, Modifier.padding(innerPadding)) {
            composable(Screen.Timer.route) {
                TimerScreen(
                    uiState           = uiState,
                    onStart           = vm::startTimer,
                    onPause           = vm::pauseTimer,
                    onReset           = vm::resetTimer,
                    onSetWorkDuration = vm::setWorkDuration,
                    onSetBreakDuration= vm::setBreakDuration
                )
            }
            composable(Screen.WorkLog.route) {
                WorkLogScreen(logs = workLogs)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    notificationEnabled = notifEnabled,
                    soundEnabled        = soundEnabled,
                    onNotifToggle       = vm::setNotificationEnabled,
                    onSoundToggle       = vm::setSoundEnabled
                )
            }
        }
    }
}
