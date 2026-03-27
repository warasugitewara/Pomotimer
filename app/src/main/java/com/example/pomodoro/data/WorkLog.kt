package com.example.pomodoro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_logs")
data class WorkLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionType: String,       // "WORK" or "BREAK"
    val plannedSeconds: Long,
    val actualSeconds: Long,
    val completed: Boolean,
    val lapNumber: Int
)
