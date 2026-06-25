package com.example.pomodoro.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
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
import com.example.pomodoro.util.ApkInstaller
import com.example.pomodoro.util.UpdateInfo
import com.example.pomodoro.viewmodel.TimerViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String) {
    object Timer   : Screen("timer",    "Timer")
    object WorkLog : Screen("worklog",  "Log")
    object Settings: Screen("settings", "Settings")
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
    val dailyStats   by vm.dailyStats.collectAsStateWithLifecycle(emptyList())
    val totalPomos   by vm.totalPomodoros.collectAsStateWithLifecycle(0)
    val totalWorkSec by vm.totalWorkSeconds.collectAsStateWithLifecycle(0L)
    val streak       by vm.currentStreak.collectAsStateWithLifecycle(0)
    val statsRange   by vm.statsRangeDays.collectAsStateWithLifecycle()
    val longestFocusSec by vm.longestFocusSeconds.collectAsStateWithLifecycle(0L)
    val statsLast30  by vm.statsLast30Days.collectAsStateWithLifecycle(emptyList())

    val appThemeName  by vm.settings.appTheme.collectAsStateWithLifecycle("LIGHT")
    val customBg      by vm.settings.customBgColor.collectAsStateWithLifecycle("#FAFAFA")
    val customText    by vm.settings.customTextColor.collectAsStateWithLifecycle("#212121")
    val customAccent  by vm.settings.customAccentColor.collectAsStateWithLifecycle("#E53935")

    val discordRpcEnabled  by vm.discordRpcEnabled.collectAsStateWithLifecycle(false)
    val discordBridgeUrl   by vm.discordBridgeUrl.collectAsStateWithLifecycle("")
    val discordBridgeToken by vm.discordBridgeToken.collectAsStateWithLifecycle("")
    val connectionTestResult by vm.connectionTestResult.collectAsStateWithLifecycle(null)
    val autoStartBreak by vm.autoStartBreak.collectAsStateWithLifecycle(false)
    val autoStartWork  by vm.autoStartWork.collectAsStateWithLifecycle(false)
    val appTheme = AppTheme.entries.find { it.name == appThemeName } ?: AppTheme.LIGHT

    val updateInfo by vm.updateInfo.collectAsStateWithLifecycle(null)
    var updateDismissed by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    PomotimerTheme(
        theme        = appTheme,
        customBg     = customBg,
        customText   = customText,
        customAccent = customAccent
    ) {
        // ── アップデート通知ダイアログ ────────────────────────
        val info = updateInfo
        if (info != null && !updateDismissed) {
            UpdateDialog(
                info = info,
                onDismiss = { updateDismissed = true },
                onOpenReleasePage = {
                    uriHandler.openUri(com.example.pomodoro.util.RELEASES_PAGE)
                }
            )
        }

        val navItems = listOf(
            Triple(Screen.Timer,    Icons.Default.Timer,    "Timer"),
            Triple(Screen.WorkLog,  Icons.Default.History,  "Log"),
            Triple(Screen.Settings, Icons.Default.Settings, "Settings"),
        )

        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val current = navBackStackEntry?.destination
                    navItems.forEach { (screen, icon, label) ->
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
            NavHost(
                navController = navController,
                startDestination = Screen.Timer.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(
                    route = Screen.Timer.route,
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() }
                ) {
                    TimerScreen(
                        uiState                = uiState,
                        onStart                = vm::startTimer,
                        onPause                = vm::pauseTimer,
                        onReset                = vm::resetTimer,
                        onStopAlarm            = vm::stopAlarm,
                        onOpenStats            = {
                            navController.navigate(Screen.WorkLog.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        onSetWorkDuration      = vm::setWorkDuration,
                        onSetBreakDuration     = vm::setBreakDuration,
                        onSetLongBreakDuration = vm::setLongBreakDuration,
                        onSetLongBreakInterval = vm::setLongBreakInterval,
                        onSetTaskName          = vm::setTaskName
                    )
                }

                composable(
                    route = Screen.WorkLog.route,
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() }
                ) {
                    WorkLogScreen(
                        selectedDate     = selectedDate,
                        logs             = logs,
                        availableDates   = allDates,
                        dailyStats       = dailyStats,
                        totalPomodoros   = totalPomos,
                        totalWorkSeconds = totalWorkSec,
                        streak           = streak,
                        statsRangeDays   = statsRange,
                        longestFocusSeconds = longestFocusSec,
                        statsLast30Days  = statsLast30,
                        onSetStatsRange  = vm::setStatsRange,
                        onSelectDate     = vm::setSelectedDate,
                        onDeleteLog      = vm::deleteLog,
                        onDeleteDay      = vm::deleteLogsForDate,
                        onDeleteAll      = vm::deleteAllLogs
                    )
                }

                composable(
                    route = Screen.Settings.route,
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() }
                ) {
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
                        onCustomAccentChange = vm::setCustomAccentColor,
                        discordRpcEnabled       = discordRpcEnabled,
                        discordBridgeUrl        = discordBridgeUrl,
                        discordBridgeToken      = discordBridgeToken,
                        connectionTestResult    = connectionTestResult,
                        onDiscordRpcToggle          = vm::setDiscordRpcEnabled,
                        onDiscordBridgeUrlChange    = vm::setDiscordBridgeUrl,
                        onDiscordBridgeTokenChange  = vm::setDiscordBridgeToken,
                        onTestDiscordConnection     = vm::testDiscordConnection,
                        autoStartBreak              = autoStartBreak,
                        autoStartWork                = autoStartWork,
                        onAutoStartBreakToggle       = vm::setAutoStartBreak,
                        onAutoStartWorkToggle        = vm::setAutoStartWork
                    )
                }
            }
        }
    }
}

private enum class UpdateStage { PROMPT, DOWNLOADING, ERROR }

@Composable
private fun UpdateDialog(
    info: UpdateInfo,
    onDismiss: () -> Unit,
    onOpenReleasePage: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var stage by remember { mutableStateOf(UpdateStage.PROMPT) }
    var progress by remember { mutableFloatStateOf(0f) }

    AlertDialog(
        onDismissRequest = { if (stage != UpdateStage.DOWNLOADING) onDismiss() },
        icon = { Icon(Icons.Default.SystemUpdateAlt, null) },
        title = { Text("アップデートあり") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("新バージョン v${info.version} が公開されています。")
                when (stage) {
                    UpdateStage.DOWNLOADING -> {
                        if (progress >= 0f) {
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                            Text("ダウンロード中… ${(progress * 100).toInt()}%")
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text("ダウンロード中…")
                        }
                    }
                    UpdateStage.ERROR ->
                        Text("ダウンロードに失敗しました。リリースページから手動で更新してください。",
                            color = MaterialTheme.colorScheme.error)
                    else -> {}
                }
            }
        },
        confirmButton = {
            when (stage) {
                UpdateStage.DOWNLOADING -> {}
                UpdateStage.ERROR ->
                    Button(onClick = { onOpenReleasePage(); onDismiss() }) { Text("リリースページ") }
                else -> {
                    if (info.apkUrl != null) {
                        Button(onClick = {
                            stage = UpdateStage.DOWNLOADING
                            scope.launch {
                                try {
                                    val apk = ApkInstaller.download(context, info.apkUrl) { progress = it }
                                    ApkInstaller.install(context, apk)
                                    onDismiss()
                                } catch (_: Exception) {
                                    stage = UpdateStage.ERROR
                                }
                            }
                        }) { Text("ダウンロードしてインストール") }
                    } else {
                        Button(onClick = { onOpenReleasePage(); onDismiss() }) { Text("リリースページ") }
                    }
                }
            }
        },
        dismissButton = {
            if (stage != UpdateStage.DOWNLOADING) {
                TextButton(onClick = onDismiss) { Text("後で") }
            }
        }
    )
}
