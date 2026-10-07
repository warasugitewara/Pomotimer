package com.example.pomodoro.util

import com.example.pomodoro.data.SettingsRepository
import com.example.pomodoro.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs

private const val SOURCE_ID = "pomotimer-android"
private const val SOURCE_NAME = "Pomotimer"
private const val MIN_INTERVAL_MILLIS = 15_000L
/** 実行中の終了予定時刻(end_ms)の揺れをこの範囲なら同一とみなし、送信内容を安定させる。 */
private const val END_TOLERANCE_MILLIS = 3_000L

/** 接続テストの結果。経路の問題(UNREACHABLE)とトークンの問題(AUTH_FAILED)を区別する。 */
enum class ConnectionTestResult { SUCCESS, AUTH_FAILED, UNREACHABLE }

/**
 * Waras-discordRPC ブリッジ（docs/PROTOCOL.md）への presence 送信。
 * ベストエフォート：失敗は無視し、タイマー動作に影響を与えない。
 * 呼び出し側のライフサイクルに依存しないよう、専用スコープで fire-and-forget する。
 *
 * - 送信は [lock] で直列化し、古い要求は新しい要求に追い越されたら捨てる（世代番号）。
 *   並行 POST だと clear と presence の到着順が入れ替わり、停止後に表示が残ることがある。
 * - 実行中は残り時間を文字列で送らず end_ms（終了予定時刻）を送る。Discord 側が
 *   カウントダウンを描画するため、毎秒の tick では送信内容が変わらず再送も起きない。
 * - 同一内容は最短 15 秒間隔でのみ再送する（ブリッジ側 TTL 維持のためのキープアライブ）。
 *   失敗時も同じ間隔でしか再試行しないので、到達不能時に接続試行が積み上がらない。
 */
object DiscordRpcReporter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Mutex()
    private val generation = AtomicLong(0)

    // 以下は lock 内でのみ読み書きする。
    private var lastSentPayload: String? = null
    private var lastSentAtMillis = 0L
    private var lastAttemptPayload: String? = null
    private var lastAttemptAtMillis = 0L
    private var lastEndMillis: Long? = null

    /** 状態の節目では [force] = true で、内容が同じでも即時送信する。 */
    fun notifyState(settings: SettingsRepository, state: TimerState, force: Boolean = false) {
        val gen = generation.incrementAndGet()
        val now = System.currentTimeMillis()
        scope.launch {
            lock.withLock {
                if (gen != generation.get()) return@withLock // より新しい要求が控えている
                if (!settings.discordRpcEnabled.first()) return@withLock
                val url = buildBridgeUrl(settings) ?: return@withLock
                val token = settings.discordBridgeToken.first()
                if (token.isBlank()) return@withLock

                val body = buildPresence(state, now).toString()
                val sentAt = System.currentTimeMillis()
                val sameAsSent = body == lastSentPayload
                if (!force && sameAsSent && sentAt - lastSentAtMillis < MIN_INTERVAL_MILLIS) return@withLock
                // 失敗直後の同一内容は間隔を空けて再試行する(到達不能時に毎秒接続しない)
                if (!force && body == lastAttemptPayload && body != lastSentPayload &&
                    sentAt - lastAttemptAtMillis < MIN_INTERVAL_MILLIS) return@withLock

                lastAttemptPayload = body
                lastAttemptAtMillis = sentAt
                if (postJson(url, token, "/presence", body)) {
                    lastSentPayload = body
                    lastSentAtMillis = sentAt
                }
            }
        }
    }

    /** タイマー停止時にDiscordのプレゼンスを消す。 */
    fun notifyClear(settings: SettingsRepository) {
        val gen = generation.incrementAndGet()
        scope.launch {
            lock.withLock {
                if (gen != generation.get()) return@withLock
                if (!settings.discordRpcEnabled.first()) return@withLock
                val url = buildBridgeUrl(settings) ?: return@withLock
                val token = settings.discordBridgeToken.first()
                if (token.isBlank()) return@withLock

                val payload = JSONObject().apply { put("source_id", SOURCE_ID) }
                postJson(url, token, "/clear", payload.toString())
                // 成否に関わらずリセットする(次の開始時は必ず送信させる)
                lastSentPayload = null
                lastSentAtMillis = 0L
                lastAttemptPayload = null
                lastAttemptAtMillis = 0L
                lastEndMillis = null
            }
        }
    }

    /** 設定画面の「接続テスト」用。GET /health を叩き、経路と認証を切り分けた結果を返す。 */
    suspend fun testConnection(host: String, port: String, useHttps: Boolean, token: String): ConnectionTestResult {
        val url = buildBridgeUrl(host, port, useHttps) ?: return ConnectionTestResult.UNREACHABLE
        return try {
            val conn = URL(joinUrl(url, "/health")).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.connect()
            val code = conn.responseCode
            conn.disconnect()
            when (code) {
                in 200..299 -> ConnectionTestResult.SUCCESS
                401, 403 -> ConnectionTestResult.AUTH_FAILED // 経路は届いているがトークン不一致
                else -> ConnectionTestResult.UNREACHABLE
            }
        } catch (_: Exception) {
            ConnectionTestResult.UNREACHABLE
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
        val cleanHost = sanitizeHost(host)
        if (cleanHost.isBlank()) return null
        val scheme = if (useHttps) "https" else "http"
        val portPart = if (port.isBlank()) "" else ":$port"
        return "$scheme://$cleanHost$portPart"
    }

    /**
     * ホスト入力の揺れを吸収する。"http://192.168.1.20/" のようにスキームや
     * パス・ポートが混入していると "http://http://..." になり必ず接続失敗するため、
     * ホスト名/IP だけを取り出す。
     */
    fun sanitizeHost(raw: String): String {
        var h = raw.trim()
        h = h.replace(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://"), "") // スキーム除去
        h = h.substringBefore('/')                               // パス除去
        h = when {
            h.startsWith("[") -> h.substringBefore(']').removePrefix("[") // [IPv6]:port
            h.count { it == ':' } == 1 -> h.substringBefore(':')          // host:port 形式のポート混入
            else -> h                                                      // 生 IPv6 は温存
        }
        return h.trim()
    }

    /** PROTOCOL.md の kind="generic" presence を組み立てる。lock 内から呼ぶ。 */
    private fun buildPresence(state: TimerState, nowMillis: Long): JSONObject {
        val cycle = "Cycle ${state.pomodorosInCycle}/${state.longBreakInterval}"
        val modeLabel = when {
            state.isAlarmPlaying -> "⏰ Time's up"
            state.isWorkMode     -> state.currentTaskName?.let { "🍅 $it" } ?: "🍅 Focusing"
            state.isLongBreak    -> "🌴 Long Break"
            else                  -> "☕ Short Break"
        }

        val data = JSONObject()
        if (state.isRunning) {
            // 残り時間は Discord のカウントダウン(end_ms)で表示する
            val candidate = nowMillis + state.remainingSeconds * 1000L
            val prev = lastEndMillis
            val end = if (prev != null && abs(candidate - prev) <= END_TOLERANCE_MILLIS) prev else candidate
            lastEndMillis = end
            data.put("details", modeLabel)
            data.put("state", cycle)
            data.put("activity_type", "playing")
            data.put("end_ms", end)
        } else {
            lastEndMillis = null
            val mins = state.remainingSeconds / 60
            val secs = state.remainingSeconds % 60
            val suffix = if (state.isAlarmPlaying) "" else " (Paused)"
            data.put("details", modeLabel + suffix)
            data.put("state", "Remaining: ${mins}m ${secs}s · $cycle")
            data.put("activity_type", "playing")
        }

        return JSONObject().apply {
            put("kind", "generic")
            put("source_id", SOURCE_ID)
            put("source_name", SOURCE_NAME)
            put("data", data)
        }
    }

    private fun postJson(bridgeUrl: String, token: String, path: String, body: String): Boolean {
        return try {
            val conn = URL(joinUrl(bridgeUrl, path)).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.doOutput = true
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
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
