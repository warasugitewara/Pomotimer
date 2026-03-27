package com.example.pomodoro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.data.AppDatabase
import com.example.pomodoro.data.SettingsRepository
import com.example.pomodoro.model.TimerState
import com.example.pomodoro.service.TimerService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimerViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getInstance(app).workLogDao()

    val uiState: StateFlow<TimerState> = TimerService.uiState

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

    // ───── 設定 ─────

    fun setNotificationEnabled(v: Boolean)  = viewModelScope.launch { settings.setNotificationEnabled(v) }
    fun setSoundEnabled(v: Boolean)         = viewModelScope.launch { settings.setSoundEnabled(v) }
    fun setVibrationEnabled(v: Boolean)     = viewModelScope.launch { settings.setVibrationEnabled(v) }
    fun setAppTheme(v: String)              = viewModelScope.launch { settings.setAppTheme(v) }
    fun setCustomBgColor(v: String)         = viewModelScope.launch { settings.setCustomBgColor(v) }
    fun setCustomTextColor(v: String)       = viewModelScope.launch { settings.setCustomTextColor(v) }
    fun setCustomAccentColor(v: String)     = viewModelScope.launch { settings.setCustomAccentColor(v) }
}
