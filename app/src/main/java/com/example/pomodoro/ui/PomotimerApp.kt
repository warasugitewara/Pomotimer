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
import com.example.pomodoro.ui.theme.AppTheme
import com.example.pomodoro.ui.theme.PomotimerTheme
import com.example.pomodoro.viewmodel.TimerViewModel

sealed class Screen(val route: String, val label: String) {
    object Timer   : Screen("timer",    "タイマー")
    object WorkLog : Screen("worklog",  "ログ")
    object Settings: Screen("settings", "設定")
}

@Composable
fun PomotimerApp(vm: TimerViewModel = viewModel()) {
    val navController = rememberNavController()
    val uiState      by vm.uiState.collectAsStateWithLifecycle()
    val notifEnabled by vm.settings.notificationEnabled.collectAsStateWithLifecycle(true)
    val soundEnabled by vm.settings.soundEnabled.collectAsStateWithLifecycle(true)
    val vibEnabled   by vm.settings.vibrationEnabled.collectAsStateWithLifecycle(true)
    val selectedDate by vm.selectedDate.collectAsStateWithLifecycle()
    val logs         by vm.logsForSelectedDate.collectAsStateWithLifecycle(emptyList())
    val allDates     by vm.availableDates.collectAsStateWithLifecycle(emptyList())

    // テーマ設定
    val appThemeName  by vm.settings.appTheme.collectAsStateWithLifecycle("LIGHT")
    val customBg      by vm.settings.customBgColor.collectAsStateWithLifecycle("#FAFAFA")
    val customText    by vm.settings.customTextColor.collectAsStateWithLifecycle("#212121")
    val customAccent  by vm.settings.customAccentColor.collectAsStateWithLifecycle("#E53935")
    val appTheme = AppTheme.entries.find { it.name == appThemeName } ?: AppTheme.LIGHT

    PomotimerTheme(
        theme        = appTheme,
        customBg     = customBg,
        customText   = customText,
        customAccent = customAccent
    ) {
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
                            icon     = { Icon(icon, contentDescription = label) },
                            label    = { Text(label) },
                            selected = current?.hierarchy?.any { it.route == screen.route } == true,
                            onClick  = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = Screen.Timer.route,
                Modifier.padding(innerPadding)) {

                composable(Screen.Timer.route) {
                    TimerScreen(
                        uiState                = uiState,
                        onStart                = vm::startTimer,
                        onPause                = vm::pauseTimer,
                        onReset                = vm::resetTimer,
                        onStopAlarm            = vm::stopAlarm,
                        onSetWorkDuration      = vm::setWorkDuration,
                        onSetBreakDuration     = vm::setBreakDuration,
                        onSetLongBreakDuration = vm::setLongBreakDuration,
                        onSetLongBreakInterval = vm::setLongBreakInterval
                    )
                }

                composable(Screen.WorkLog.route) {
                    WorkLogScreen(
                        selectedDate   = selectedDate,
                        logs           = logs,
                        availableDates = allDates,
                        onSelectDate   = vm::setSelectedDate,
                        onDeleteLog    = vm::deleteLog,
                        onDeleteDay    = vm::deleteLogsForDate,
                        onDeleteAll    = vm::deleteAllLogs
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        notificationEnabled  = notifEnabled,
                        soundEnabled         = soundEnabled,
                        vibrationEnabled     = vibEnabled,
                        appThemeName         = appThemeName,
                        customBg             = customBg,
                        customText           = customText,
                        customAccent         = customAccent,
                        onNotifToggle        = vm::setNotificationEnabled,
                        onSoundToggle        = vm::setSoundEnabled,
                        onVibrationToggle    = vm::setVibrationEnabled,
                        onThemeChange        = vm::setAppTheme,
                        onCustomBgChange     = vm::setCustomBgColor,
                        onCustomTextChange   = vm::setCustomTextColor,
                        onCustomAccentChange = vm::setCustomAccentColor
                    )
                }
            }
        }
    }
}
