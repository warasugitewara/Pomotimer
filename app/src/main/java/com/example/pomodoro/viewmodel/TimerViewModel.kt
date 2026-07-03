package com.example.pomodoro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.BuildConfig
import com.example.pomodoro.data.AppDatabase
import com.example.pomodoro.data.SettingsRepository
import com.example.pomodoro.model.TimerState
import com.example.pomodoro.service.TimerService
import com.example.pomodoro.util.DiscordRpcReporter
import com.example.pomodoro.util.UpdateInfo
import com.example.pomodoro.util.fetchLatestRelease
import com.example.pomodoro.util.isNewerVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TimerViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getInstance(app).workLogDao()

    val uiState: StateFlow<TimerState> = TimerService.uiState

    // ───── アップデート通知 ─────

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    /** 新バージョンがある場合はリリース情報、なければ null。 */
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    init {
        viewModelScope.launch {
            val latest = fetchLatestRelease()
            if (latest != null && isNewerVersion(latest.version, BuildConfig.VERSION_NAME)) {
                _updateInfo.value = latest
            }
        }
    }

    // ───── 作業ログ（日付ナビゲーション） ─────

    val availableDates = dao.getDistinctDates()

    private val _selectedDate = MutableStateFlow(todayDateKey())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val logsForSelectedDate = _selectedDate.flatMapLatest { dao.getLogsForDate(it) }

    fun setSelectedDate(date: String) { _selectedDate.value = date }
    fun deleteLog(id: Long)              = viewModelScope.launch { dao.deleteById(id) }
    fun deleteLogsForDate(dateKey: String) = viewModelScope.launch { dao.deleteForDate(dateKey) }
    fun deleteAllLogs()                  = viewModelScope.launch { dao.deleteAll() }

    private fun todayDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // ───── 統計・グラフ ─────

    /** グラフの表示期間（日数）。7 または 30。 */
    private val _statsRangeDays = MutableStateFlow(7)
    val statsRangeDays: StateFlow<Int> = _statsRangeDays.asStateFlow()
    fun setStatsRange(days: Int) { _statsRangeDays.value = days }

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyStats = _statsRangeDays.flatMapLatest { days ->
        dao.getDailyStatsSince(startMillisDaysAgo(days))
    }

    val totalPomodoros   = dao.getTotalPomodoros()
    val totalWorkSeconds = dao.getTotalWorkSeconds()
    val longestFocusSeconds = dao.getLongestFocusSeconds()

    /** 今日（または昨日）から連続でポモドーロを記録した日数。 */
    val currentStreak = dao.getPomodoroDates().map { dates -> computeStreak(dates) }

    /** 今日/今週(直近7日)/今月(直近30日)の集計タイル用。表示トグルとは独立した固定30日分。 */
    val statsLast30Days = dao.getDailyStatsSince(startMillisDaysAgo(30))

    /** [days] 日前の0時0分の epoch ミリ秒。 */
    private fun startMillisDaysAgo(days: Int): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -(days - 1))
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /** "yyyy-MM-dd" 降順リストから連続記録日数を求める。 */
    private fun computeStreak(datesDesc: List<String>): Int {
        if (datesDesc.isEmpty()) return 0
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val set = datesDesc.toHashSet()
        val cal = Calendar.getInstance()
        val today = fmt.format(cal.time)
        // 今日まだ記録がなければ昨日起点で数える（連続が途切れない猶予）
        if (today !in set) cal.add(Calendar.DAY_OF_YEAR, -1)
        var streak = 0
        while (fmt.format(cal.time) in set) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    // ───── タイマーアクション ─────

    val settings = SettingsRepository(app)

    fun startTimer()   = TimerService.startTimer(getApplication())
    fun pauseTimer()   = TimerService.pauseTimer(getApplication())
    fun stopTimer()    = TimerService.stopService(getApplication())
    fun resetTimer()   = TimerService.resetTimer(getApplication())
    fun stopAlarm()    = TimerService.stopAlarm(getApplication())

    fun setWorkDuration(minutes: Int)      = TimerService.setWorkDuration(getApplication(), minutes)
    fun setBreakDuration(minutes: Int)     = TimerService.setBreakDuration(getApplication(), minutes)
    fun setLongBreakDuration(minutes: Int) = TimerService.setLongBreakDuration(getApplication(), minutes)
    fun setLongBreakInterval(count: Int)   = TimerService.setLongBreakInterval(getApplication(), count)
    fun setTaskName(taskName: String?)     = TimerService.setTaskName(getApplication(), taskName)

    // ───── 設定 ─────

    fun setNotificationEnabled(v: Boolean)  = viewModelScope.launch { settings.setNotificationEnabled(v) }
    fun setAutoStartBreak(v: Boolean)       = viewModelScope.launch { settings.setAutoStartBreak(v) }
    fun setAutoStartWork(v: Boolean)        = viewModelScope.launch { settings.setAutoStartWork(v) }
    fun setSoundEnabled(v: Boolean)         = viewModelScope.launch { settings.setSoundEnabled(v) }
    fun setVibrationEnabled(v: Boolean)     = viewModelScope.launch { settings.setVibrationEnabled(v) }
    fun setAppTheme(v: String)              = viewModelScope.launch { settings.setAppTheme(v) }
    fun setCustomBgColor(v: String)         = viewModelScope.launch { settings.setCustomBgColor(v) }
    fun setCustomTextColor(v: String)       = viewModelScope.launch { settings.setCustomTextColor(v) }
    fun setCustomAccentColor(v: String)     = viewModelScope.launch { settings.setCustomAccentColor(v) }

    // ───── Discord RPC連携 ─────

    val discordRpcEnabled  = settings.discordRpcEnabled
    val discordBridgeHost  = settings.discordBridgeHost
    val discordBridgePort  = settings.discordBridgePort
    val discordBridgeHttps = settings.discordBridgeHttps
    val discordBridgeToken = settings.discordBridgeToken

    // ───── サイクル自動開始 ─────

    val autoStartBreak = settings.autoStartBreak
    val autoStartWork  = settings.autoStartWork

    fun setDiscordRpcEnabled(v: Boolean)  = viewModelScope.launch { settings.setDiscordRpcEnabled(v) }
    fun setDiscordBridgeHost(v: String)   = viewModelScope.launch { settings.setDiscordBridgeHost(v) }
    fun setDiscordBridgePort(v: String)   = viewModelScope.launch { settings.setDiscordBridgePort(v) }
    fun setDiscordBridgeHttps(v: Boolean) = viewModelScope.launch { settings.setDiscordBridgeHttps(v) }
    fun setDiscordBridgeToken(v: String)  = viewModelScope.launch { settings.setDiscordBridgeToken(v) }

    private val _connectionTestResult = MutableStateFlow<Boolean?>(null)
    /** 接続テストの結果。null=未実行、true=成功、false=失敗。 */
    val connectionTestResult: StateFlow<Boolean?> = _connectionTestResult.asStateFlow()

    fun testDiscordConnection(host: String, port: String, useHttps: Boolean, token: String) {
        viewModelScope.launch {
            _connectionTestResult.value = null
            _connectionTestResult.value = DiscordRpcReporter.testConnection(host, port, useHttps, token)
        }
    }
}
