package com.example.pomodoro.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pomodoro.model.TimerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** タスク名リストの区切り文字（ユーザー入力に現れない制御文字）。 */
private const val TASK_NAME_SEPARATOR = "\u001F"

/**
 * タイマー状態の永続化スナップショット。
 * [endAtMillis] は実行中セッションの満了予定時刻（epoch ミリ秒、停止中は 0）。
 * [sessionStartRemaining] はセッション開始時点の残り秒数（作業ログの実績計算用）。
 */
data class TimerSnapshot(
    val state: TimerState,
    val endAtMillis: Long,
    val sessionStartRemaining: Long
)

class SettingsRepository(private val context: Context) {

    companion object {
        val NOTIFICATION_ENABLED  = booleanPreferencesKey("notification_enabled")
        val SOUND_ENABLED         = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED     = booleanPreferencesKey("vibration_enabled")
        val APP_THEME             = stringPreferencesKey("app_theme")
        val CUSTOM_BG_COLOR       = stringPreferencesKey("custom_bg_color")
        val CUSTOM_TEXT_COLOR     = stringPreferencesKey("custom_text_color")
        val CUSTOM_ACCENT_COLOR   = stringPreferencesKey("custom_accent_color")
        val DISCORD_RPC_ENABLED   = booleanPreferencesKey("discord_rpc_enabled")
        val DISCORD_BRIDGE_HOST   = stringPreferencesKey("discord_bridge_host")
        val DISCORD_BRIDGE_PORT   = stringPreferencesKey("discord_bridge_port")
        val DISCORD_BRIDGE_HTTPS  = booleanPreferencesKey("discord_bridge_https")
        val DISCORD_BRIDGE_TOKEN  = stringPreferencesKey("discord_bridge_token")
        val AUTO_START_BREAK      = booleanPreferencesKey("auto_start_break")
        val AUTO_START_WORK       = booleanPreferencesKey("auto_start_work")
        val TASK_NAMES            = stringPreferencesKey("task_names")

        // ── タイマー状態スナップショット（プロセス再生成時の復元用） ──
        val TIMER_TOTAL            = longPreferencesKey("timer_total_seconds")
        val TIMER_REMAINING        = longPreferencesKey("timer_remaining_seconds")
        val TIMER_RUNNING          = booleanPreferencesKey("timer_is_running")
        val TIMER_IS_WORK          = booleanPreferencesKey("timer_is_work_mode")
        val TIMER_IS_LONG_BREAK    = booleanPreferencesKey("timer_is_long_break")
        val TIMER_LAP              = intPreferencesKey("timer_current_lap")
        val TIMER_COMPLETED_LAPS   = intPreferencesKey("timer_completed_laps")
        val TIMER_POMOS_IN_CYCLE   = intPreferencesKey("timer_pomos_in_cycle")
        val TIMER_WORK_SECS_TODAY  = longPreferencesKey("timer_work_seconds_today")
        val TIMER_PREF_WORK        = intPreferencesKey("timer_pref_work_minutes")
        val TIMER_PREF_BREAK       = intPreferencesKey("timer_pref_break_minutes")
        val TIMER_PREF_LONG_BREAK  = intPreferencesKey("timer_pref_long_break_minutes")
        val TIMER_LB_INTERVAL      = intPreferencesKey("timer_long_break_interval")
        val TIMER_TASK_NAME        = stringPreferencesKey("timer_task_name")
        val TIMER_END_AT           = longPreferencesKey("timer_end_at_millis")
        val TIMER_SESSION_START_REMAINING = longPreferencesKey("timer_session_start_remaining")

        private val TIMER_KEYS = listOf(
            TIMER_TOTAL, TIMER_REMAINING, TIMER_RUNNING, TIMER_IS_WORK, TIMER_IS_LONG_BREAK,
            TIMER_LAP, TIMER_COMPLETED_LAPS, TIMER_POMOS_IN_CYCLE, TIMER_WORK_SECS_TODAY,
            TIMER_PREF_WORK, TIMER_PREF_BREAK, TIMER_PREF_LONG_BREAK, TIMER_LB_INTERVAL,
            TIMER_TASK_NAME, TIMER_END_AT, TIMER_SESSION_START_REMAINING
        )
    }

    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATION_ENABLED] ?: true }
    val soundEnabled:        Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED]         ?: true }
    val vibrationEnabled:    Flow<Boolean> = context.dataStore.data.map { it[VIBRATION_ENABLED]     ?: true }
    val appTheme:            Flow<String>  = context.dataStore.data.map { it[APP_THEME]             ?: "LIGHT" }
    val customBgColor:       Flow<String>  = context.dataStore.data.map { it[CUSTOM_BG_COLOR]       ?: "#FAFAFA" }
    val customTextColor:     Flow<String>  = context.dataStore.data.map { it[CUSTOM_TEXT_COLOR]     ?: "#212121" }
    val customAccentColor:   Flow<String>  = context.dataStore.data.map { it[CUSTOM_ACCENT_COLOR]   ?: "#E53935" }
    val discordRpcEnabled:   Flow<Boolean> = context.dataStore.data.map { it[DISCORD_RPC_ENABLED]   ?: false }
    val discordBridgeHost:   Flow<String>  = context.dataStore.data.map { it[DISCORD_BRIDGE_HOST]   ?: "" }
    val discordBridgePort:   Flow<String>  = context.dataStore.data.map { it[DISCORD_BRIDGE_PORT]   ?: "" }
    val discordBridgeHttps:  Flow<Boolean> = context.dataStore.data.map { it[DISCORD_BRIDGE_HTTPS]  ?: false }
    val discordBridgeToken:  Flow<String>  = context.dataStore.data.map { it[DISCORD_BRIDGE_TOKEN]  ?: "" }
    val autoStartBreak:      Flow<Boolean> = context.dataStore.data.map { it[AUTO_START_BREAK]      ?: false }
    val autoStartWork:       Flow<Boolean> = context.dataStore.data.map { it[AUTO_START_WORK]       ?: false }

    suspend fun setNotificationEnabled(v: Boolean) = context.dataStore.edit { it[NOTIFICATION_ENABLED] = v }
    suspend fun setSoundEnabled(v: Boolean)        = context.dataStore.edit { it[SOUND_ENABLED]         = v }
    suspend fun setVibrationEnabled(v: Boolean)    = context.dataStore.edit { it[VIBRATION_ENABLED]     = v }
    suspend fun setAppTheme(v: String)             = context.dataStore.edit { it[APP_THEME]             = v }
    suspend fun setCustomBgColor(v: String)        = context.dataStore.edit { it[CUSTOM_BG_COLOR]       = v }
    suspend fun setCustomTextColor(v: String)      = context.dataStore.edit { it[CUSTOM_TEXT_COLOR]     = v }
    suspend fun setCustomAccentColor(v: String)    = context.dataStore.edit { it[CUSTOM_ACCENT_COLOR]   = v }
    suspend fun setDiscordRpcEnabled(v: Boolean)   = context.dataStore.edit { it[DISCORD_RPC_ENABLED]   = v }
    suspend fun setDiscordBridgeHost(v: String)    = context.dataStore.edit { it[DISCORD_BRIDGE_HOST]   = v }
    suspend fun setDiscordBridgePort(v: String)    = context.dataStore.edit { it[DISCORD_BRIDGE_PORT]   = v }
    suspend fun setDiscordBridgeHttps(v: Boolean)  = context.dataStore.edit { it[DISCORD_BRIDGE_HTTPS]  = v }
    suspend fun setDiscordBridgeToken(v: String)   = context.dataStore.edit { it[DISCORD_BRIDGE_TOKEN]  = v }
    suspend fun setAutoStartBreak(v: Boolean)      = context.dataStore.edit { it[AUTO_START_BREAK]      = v }
    suspend fun setAutoStartWork(v: Boolean)       = context.dataStore.edit { it[AUTO_START_WORK]       = v }

    // ───── タスク名プリセット ─────

    val taskNames: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[TASK_NAMES]?.split(TASK_NAME_SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun addTaskName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        context.dataStore.edit { prefs ->
            val current = prefs[TASK_NAMES]?.split(TASK_NAME_SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
            if (trimmed !in current) prefs[TASK_NAMES] = (current + trimmed).joinToString(TASK_NAME_SEPARATOR)
        }
    }

    suspend fun removeTaskName(name: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[TASK_NAMES]?.split(TASK_NAME_SEPARATOR)?.filter { it.isNotBlank() } ?: return@edit
            val next = current - name
            if (next.isEmpty()) prefs.remove(TASK_NAMES) else prefs[TASK_NAMES] = next.joinToString(TASK_NAME_SEPARATOR)
        }
    }

    // ───── タイマー状態スナップショット ─────

    suspend fun saveTimerSnapshot(state: TimerState, endAtMillis: Long, sessionStartRemaining: Long) {
        context.dataStore.edit { p ->
            p[TIMER_TOTAL]           = state.totalSeconds
            p[TIMER_REMAINING]       = state.remainingSeconds
            p[TIMER_RUNNING]         = state.isRunning
            p[TIMER_IS_WORK]         = state.isWorkMode
            p[TIMER_IS_LONG_BREAK]   = state.isLongBreak
            p[TIMER_LAP]             = state.currentLap
            p[TIMER_COMPLETED_LAPS]  = state.completedLaps
            p[TIMER_POMOS_IN_CYCLE]  = state.pomodorosInCycle
            p[TIMER_WORK_SECS_TODAY] = state.totalWorkSecondsToday
            p[TIMER_PREF_WORK]       = state.preferredWorkDurationMinutes
            p[TIMER_PREF_BREAK]      = state.preferredBreakDurationMinutes
            p[TIMER_PREF_LONG_BREAK] = state.preferredLongBreakDurationMinutes
            p[TIMER_LB_INTERVAL]     = state.longBreakInterval
            state.currentTaskName?.let { p[TIMER_TASK_NAME] = it } ?: p.remove(TIMER_TASK_NAME)
            p[TIMER_END_AT]          = endAtMillis
            p[TIMER_SESSION_START_REMAINING] = sessionStartRemaining
        }
    }

    /** 保存済みスナップショットを読む。一度も保存されていなければ null。 */
    suspend fun readTimerSnapshot(): TimerSnapshot? {
        val p = context.dataStore.data.first()
        val total = p[TIMER_TOTAL] ?: return null
        return TimerSnapshot(
            state = TimerState(
                remainingSeconds  = p[TIMER_REMAINING] ?: total,
                totalSeconds      = total,
                isRunning         = p[TIMER_RUNNING] ?: false,
                isWorkMode        = p[TIMER_IS_WORK] ?: true,
                isLongBreak       = p[TIMER_IS_LONG_BREAK] ?: false,
                currentLap        = p[TIMER_LAP] ?: 1,
                completedLaps     = p[TIMER_COMPLETED_LAPS] ?: 0,
                pomodorosInCycle  = p[TIMER_POMOS_IN_CYCLE] ?: 0,
                totalWorkSecondsToday = p[TIMER_WORK_SECS_TODAY] ?: 0L,
                preferredWorkDurationMinutes      = p[TIMER_PREF_WORK] ?: 25,
                preferredBreakDurationMinutes     = p[TIMER_PREF_BREAK] ?: 5,
                preferredLongBreakDurationMinutes = p[TIMER_PREF_LONG_BREAK] ?: 15,
                longBreakInterval = p[TIMER_LB_INTERVAL] ?: 4,
                currentTaskName   = p[TIMER_TASK_NAME]
            ),
            endAtMillis           = p[TIMER_END_AT] ?: 0L,
            sessionStartRemaining = p[TIMER_SESSION_START_REMAINING] ?: (p[TIMER_REMAINING] ?: total)
        )
    }

    suspend fun clearTimerSnapshot() {
        context.dataStore.edit { p -> TIMER_KEYS.forEach { p.remove(it) } }
    }
}
