package com.example.pomodoro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.data.AppDatabase
import com.example.pomodoro.data.SettingsRepository
import com.example.pomodoro.model.TimerState
import com.example.pomodoro.service.TimerService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimerViewModel(app: Application) : AndroidViewModel(app) {

    val uiState: StateFlow<TimerState> = TimerService.uiState

    val workLogs = AppDatabase.getInstance(app).workLogDao().getAllLogs()

    val settings = SettingsRepository(app)

    // ───── タイマーアクション（サービスに委譲） ─────

    fun startTimer()  = TimerService.startTimer(getApplication())
    fun pauseTimer()  = TimerService.pauseTimer(getApplication())
    fun stopTimer()   = TimerService.stopService(getApplication())
    fun resetTimer()  = TimerService.resetTimer(getApplication())

    fun setWorkDuration(minutes: Int)  = TimerService.setWorkDuration(getApplication(), minutes)
    fun setBreakDuration(minutes: Int) = TimerService.setBreakDuration(getApplication(), minutes)

    // ───── 設定（DataStore はコルーチン必須） ─────

    fun setNotificationEnabled(enabled: Boolean) = viewModelScope.launch {
        settings.setNotificationEnabled(enabled)
    }

    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch {
        settings.setSoundEnabled(enabled)
    }
}
