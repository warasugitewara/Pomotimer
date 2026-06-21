package com.example.pomodoro.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

private const val RELEASES_API =
    "https://api.github.com/repos/warasugitewara/Pomotimer/releases/latest"

const val RELEASES_PAGE =
    "https://github.com/warasugitewara/Pomotimer/releases/latest"

/** 最新リリース情報。 */
data class UpdateInfo(
    val version: String,   // 例: "1.5.0"
    val apkUrl: String?    // .apk の直リンク（見つからなければ null）
)

/** GitHub の最新リリース情報を取得する（失敗時は null）。 */
suspend fun fetchLatestRelease(): UpdateInfo? = withContext(Dispatchers.IO) {
    try {
        val json = JSONObject(URL(RELEASES_API).readText())
        val version = json.getString("tag_name").trimStart('v')
        val assets = json.optJSONArray("assets")
        var apkUrl: String? = null
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url").ifBlank { null }
                    break
                }
            }
        }
        UpdateInfo(version, apkUrl)
    } catch (_: Exception) {
        null
    }
}

/** latest > current なら true（セマンティックバージョン比較）。 */
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
