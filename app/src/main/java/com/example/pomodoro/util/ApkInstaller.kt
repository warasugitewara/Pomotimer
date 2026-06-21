package com.example.pomodoro.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * GitHub リリースの APK をアプリ専用 cache にダウンロードし、システムのインストーラを起動する。
 * 署名は全リリース同一鍵のため、上書き更新が可能。
 */
object ApkInstaller {

    /**
     * [apkUrl] をダウンロードして File を返す。[onProgress] は 0f..1f（不明時は -1f）。
     * 失敗時は例外を投げる。
     */
    suspend fun download(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val outFile = File(context.cacheDir, "update.apk")
        if (outFile.exists()) outFile.delete()

        var conn = URL(apkUrl).openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = true
        conn.connectTimeout = 15_000
        conn.readTimeout = 30_000
        conn.connect()

        // GitHub のリリースアセットは別ホストへリダイレクトする場合がある
        if (conn.responseCode in 300..399) {
            val location = conn.getHeaderField("Location")
            conn.disconnect()
            conn = URL(location).openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.connect()
        }

        if (conn.responseCode !in 200..299) {
            conn.disconnect()
            throw IllegalStateException("HTTP ${conn.responseCode}")
        }

        val total = conn.contentLengthLong
        conn.inputStream.use { input ->
            outFile.outputStream().use { output ->
                val buffer = ByteArray(8 * 1024)
                var downloaded = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    downloaded += read
                    onProgress(if (total > 0) downloaded.toFloat() / total else -1f)
                }
            }
        }
        conn.disconnect()
        outFile
    }

    /** ダウンロード済み APK のインストール画面を開く。 */
    fun install(context: Context, apk: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
