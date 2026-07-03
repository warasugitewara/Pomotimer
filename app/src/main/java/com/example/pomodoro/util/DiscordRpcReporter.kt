package com.example.pomodoro.util

import com.example.pomodoro.data.SettingsRepository
import com.example.pomodoro.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val SOURCE_ID = "pomotimer-android"
private const val SOURCE_NAME = "Pomotimer"
private const val MIN_INTERVAL_MILLIS = 15_000L

/**
 * Waras-discordRPC ブリッジ（docs/PROTOCOL.md）への presence 送信。
 * ベストエフォート：失敗は無視し、タイマー動作に影響を与えない。
 * 呼び出し側のライフサイクルに依存しないよう、専用スコープで fire-and-forget する。
 */
object DiscordRpcReporter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var lastSentAtMillis = 0L
    private var lastSignature: String? = null

    /** 状態の節目では [force] = true で即時送信。それ以外は最短15秒間隔に制限。 */
    fun notifyState(settings: SettingsRepository, state: TimerState, force: Boolean = false) {
        scope.launch {
            if (!settings.discordRpcEnabled.first()) return@launch
            val url = buildBridgeUrl(settings) ?: return@launch
            val token = settings.discordBridgeToken.first()
            if (token.isBlank()) return@launch

            val signature = "${state.isRunning}-${state.isWorkMode}-${state.isLongBreak}-${state.isAlarmPlaying}"
            val now = System.currentTimeMillis()
            if (!force && signature == lastSignature && now - lastSentAtMillis < MIN_INTERVAL_MILLIS) return@launch

            val (details, statusLine) = presenceText(state)
            val payload = JSONObject().apply {
                put("kind", "generic")
                put("source_id", SOURCE_ID)
                put("source_name", SOURCE_NAME)
                put("data", JSONObject().apply {
                    put("details", details)
                    put("state", statusLine)
                    put("activity_type", "playing")
                })
            }

            if (postJson(url, token, "/presence", payload)) {
                lastSentAtMillis = now
                lastSignature = signature
            }
        }
    }

    /** タイマー停止時にDiscordのプレゼンスを消す。 */
    fun notifyClear(settings: SettingsRepository) {
        scope.launch {
            if (!settings.discordRpcEnabled.first()) return@launch
            val url = buildBridgeUrl(settings) ?: return@launch
            val token = settings.discordBridgeToken.first()
            if (token.isBlank()) return@launch

            val payload = JSONObject().apply { put("source_id", SOURCE_ID) }
            if (postJson(url, token, "/clear", payload)) {
                lastSentAtMillis = 0L
                lastSignature = null
            }
        }
    }

    /** 設定画面の「接続テスト」用。GET /health の成否を返す。 */
    suspend fun testConnection(host: String, port: String, useHttps: Boolean, token: String): Boolean {
        val url = buildBridgeUrl(host, port, useHttps) ?: return false
        return try {
            val conn = URL(joinUrl(url, "/health")).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.connect()
            val ok = conn.responseCode in 200..299
            conn.disconnect()
            ok
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun buildBridgeUrl(settings: SettingsRepository): String? {
        val host = settings.discordBridgeHost.first()
        val port = settings.discordBridgePort.first()
        val https = settings.discordBridgeHttps.first()
        return buildBridgeUrl(host, port, https)
    }

    /** host（IPまたはホスト名）・port・httpsフラグから "http(s)://host:port" を組み立てる。host未設定はnull。 */
    fun buildBridgeUrl(host: String, port: String, useHttps: Boolean): String? {
        if (host.isBlank()) return null
        val scheme = if (useHttps) "https" else "http"
        val portPart = if (port.isBlank()) "" else ":$port"
        return "$scheme://$host$portPart"
    }

    private fun presenceText(state: TimerState): Pair<String, String> {
        val mins = state.remainingSeconds / 60
        val secs = state.remainingSeconds % 60
        val remaining = "Remaining: ${mins}m ${secs}s"
        val cycle = "Cycle ${state.pomodorosInCycle}/${state.longBreakInterval}"

        val modeLabel = when {
            state.isAlarmPlaying -> "⏰ Time's up"
            state.isWorkMode     -> state.currentTaskName?.let { "🍅 $it" } ?: "🍅 Focusing"
            state.isLongBreak    -> "🌴 Long Break"
            else                  -> "☕ Short Break"
        }
        val suffix = if (state.isRunning) "" else " (Paused)"
        return modeLabel + suffix to "$remaining · $cycle"
    }

    private fun postJson(bridgeUrl: String, token: String, path: String, payload: JSONObject): Boolean {
        return try {
            val conn = URL(joinUrl(bridgeUrl, path)).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.doOutput = true
            conn.outputStream.use { it.write(payload.toString().toByteArray()) }
            val ok = conn.responseCode in 200..299
            conn.disconnect()
            ok
        } catch (_: Exception) {
            false
        }
    }

    private fun joinUrl(base: String, path: String): String =
        base.trimEnd('/') + path
}
