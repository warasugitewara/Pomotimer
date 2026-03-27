package com.example.pomodoro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkLogDao {
    @Insert
    suspend fun insert(log: WorkLog)

    // 日付文字列 "yyyy-MM-dd" のリスト（ログが存在する日のみ、新しい順）
    @Query("SELECT DISTINCT date(timestamp / 1000, 'unixepoch', 'localtime') FROM work_logs ORDER BY 1 DESC")
    fun getDistinctDates(): Flow<List<String>>

    // 指定日のログ一覧（新しい順）
    @Query("SELECT * FROM work_logs WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = :dateKey ORDER BY timestamp DESC")
    fun getLogsForDate(dateKey: String): Flow<List<WorkLog>>

    // 個別削除
    @Query("DELETE FROM work_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    // 指定日のログをすべて削除
    @Query("DELETE FROM work_logs WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = :dateKey")
    suspend fun deleteForDate(dateKey: String)

    // 全ログ削除
    @Query("DELETE FROM work_logs")
    suspend fun deleteAll()
}
