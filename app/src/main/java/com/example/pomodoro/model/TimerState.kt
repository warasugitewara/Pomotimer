package com.example.pomodoro.model

data class TimerState(
    val remainingSeconds: Long = 25 * 60L,
    val totalSeconds: Long = 25 * 60L,
    val isRunning: Boolean = false,
    val isWorkMode: Boolean = true,
    val isLongBreak: Boolean = false,
    val isAlarmPlaying: Boolean = false,
    val currentLap: Int = 1,
    val completedLaps: Int = 0,
    val pomodorosInCycle: Int = 0,
    val totalWorkSecondsToday: Long = 0L,
    val preferredWorkDurationMinutes: Int = 25,
    val preferredBreakDurationMinutes: Int = 5,
    val preferredLongBreakDurationMinutes: Int = 15,
    val longBreakInterval: Int = 4
)
