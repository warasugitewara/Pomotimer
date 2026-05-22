package com.example.pomodoro.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

private const val RELEASES_API =
    "https://api.github.com/repos/warasugitewara/Pomotimer/releases/latest"

/** GitHub の最新リリースタグを取得する（例: "1.4.0"、失敗時は null）*/
suspend fun fetchLatestVersion(): String? = withContext(Dispatchers.IO) {
    try {
        val json = URL(RELEASES_API).readText()
        JSONObject(json).getString("tag_name").trimStart('v')
    } catch (_: Exception) {
        null
    }
}

/** latest > current なら true（セマンティックバージョン比較）*/
fun isNewerVersion(latest: String, current: String): Boolean {
    fun parts(v: String) = v.split(".").map { it.toIntOrNull() ?: 0 }
    val l = parts(latest)
    val c = parts(current)
    for (i in 0..maxOf(l.lastIndex, c.lastIndex)) {
        val diff = (l.getOrElse(i) { 0 }) - (c.getOrElse(i) { 0 })
        if (diff != 0) return diff > 0
    }
    return false
}
