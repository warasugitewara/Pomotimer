package com.example.pomodoro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** 日別集計の1行（date は "yyyy-MM-dd"）。 */
data class DailyStat(
    val date: String,
    val pomodoros: Int,
    val workSeconds: Long
)

@Dao
interface WorkLogDao {
    @Insert
    suspend fun insert(log: WorkLog)

    /** 直近 [days] 日の日別集計（完了WORK数・WORK合計秒、古い順）。 */
    @Query(
        """
        SELECT date(timestamp / 1000, 'unixepoch', 'localtime') AS date,
               SUM(CASE WHEN sessionType = 'WORK' AND completed = 1 THEN 1 ELSE 0 END) AS pomodoros,
               SUM(CASE WHEN sessionType = 'WORK' THEN actualSeconds ELSE 0 END) AS workSeconds
        FROM work_logs
        WHERE timestamp >= :sinceMillis
        GROUP BY date
        ORDER BY date ASC
        """
    )
    fun getDailyStatsSince(sinceMillis: Long): Flow<List<DailyStat>>

    /** 累計の完了ポモドーロ数。 */
    @Query("SELECT COUNT(*) FROM work_logs WHERE sessionType = 'WORK' AND completed = 1")
    fun getTotalPomodoros(): Flow<Int>

    /** 累計のWORK合計秒。 */
    @Query("SELECT COALESCE(SUM(actualSeconds), 0) FROM work_logs WHERE sessionType = 'WORK'")
    fun getTotalWorkSeconds(): Flow<Long>

    /** 最長の集中時間（単一セッションのWORK秒）。 */
    @Query("SELECT COALESCE(MAX(actualSeconds), 0) FROM work_logs WHERE sessionType = 'WORK'")
    fun getLongestFocusSeconds(): Flow<Long>

    /** ポモドーロを記録した日付一覧（新しい順）。ストリーク算出に用いる。 */
    @Query(
        """
        SELECT DISTINCT date(timestamp / 1000, 'unixepoch', 'localtime')
        FROM work_logs
        WHERE sessionType = 'WORK' AND completed = 1
        ORDER BY 1 DESC
        """
    )
    fun getPomodoroDates(): Flow<List<String>>

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
