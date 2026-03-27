package com.example.pomodoro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkLogDao {
    @Insert
    suspend fun insert(log: WorkLog)

    @Query("SELECT * FROM work_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getLogsSince(since: Long): Flow<List<WorkLog>>
}
