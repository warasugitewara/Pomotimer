package com.example.pomodoro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.model.TimerState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TimerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TimerState())
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var lastTickTime: Long = 0L

    fun startTimer() {
        if (_uiState.value.isRunning) return

        _uiState.update { it.copy(isRunning = true) }
        lastTickTime = System.currentTimeMillis()

        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.remainingSeconds > 0) {
                delay(500L) // Check every 500ms for better precision than 1000ms
                val currentTime = System.currentTimeMillis()
                val elapsedSeconds = (currentTime - lastTickTime) / 1000

                if (elapsedSeconds >= 1) {
                    _uiState.update { state ->
                        val newRemaining = (state.remainingSeconds - elapsedSeconds).coerceAtLeast(0L)
                        state.copy(remainingSeconds = newRemaining)
                    }
                    lastTickTime += elapsedSeconds * 1000
                }

                if (_uiState.value.remainingSeconds <= 0L) {
                    onTimerFinished()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        pauseTimer()
        _uiState.update { state ->
            state.copy(
                remainingSeconds = state.totalSeconds,
                isRunning = false
            )
        }
    }

    fun setWorkDuration(minutes: Int) {
        val seconds = minutes * 60L
        _uiState.update { state ->
            state.copy(
                preferredWorkDurationMinutes = minutes,
                totalSeconds = seconds,
                remainingSeconds = seconds,
                isRunning = false,
                isWorkMode = true
            )
        }
        pauseTimer()
    }

    fun setBreakDuration(minutes: Int) {
        _uiState.update { state ->
            state.copy(preferredBreakDurationMinutes = minutes)
        }
    }

    private fun onTimerFinished() {
        _uiState.update { state ->
            val wasWorkMode = state.isWorkMode
            val newCompletedLaps = if (wasWorkMode) state.completedLaps + 1 else state.completedLaps
            val newTotalWorkSeconds = if (wasWorkMode) state.totalWorkSecondsToday + state.totalSeconds else state.totalWorkSecondsToday

            val nextModeIsWork = !wasWorkMode
            val nextDurationMinutes = if (nextModeIsWork) state.preferredWorkDurationMinutes else state.preferredBreakDurationMinutes
            val nextDurationSeconds = nextDurationMinutes * 60L

            state.copy(
                isRunning = false,
                completedLaps = newCompletedLaps,
                totalWorkSecondsToday = newTotalWorkSeconds,
                isWorkMode = nextModeIsWork,
                totalSeconds = nextDurationSeconds,
                remainingSeconds = nextDurationSeconds,
                currentLap = if (nextModeIsWork) state.currentLap + 1 else state.currentLap
            )
        }
    }
}
