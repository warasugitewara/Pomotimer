package com.example.pomodoro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.data.WorkLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkLogScreen(logs: List<WorkLog>) {
    val dateFormat  = remember { SimpleDateFormat("M/d", Locale.JAPAN) }
    val timeFormat  = remember { SimpleDateFormat("HH:mm", Locale.JAPAN) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "作業ログ",
            fontSize = 20.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("まだ記録がありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Column
        }

        // 日付ごとにグループ化
        val grouped = logs.groupBy { dateFormat.format(Date(it.timestamp)) }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            grouped.forEach { (date, dayLogs) ->
                item {
                    val workSecs  = dayLogs.filter { it.sessionType == "WORK" }.sumOf { it.actualSeconds }
                    val breakSecs = dayLogs.filter { it.sessionType == "BREAK" }.sumOf { it.actualSeconds }
                    val pomodoros = dayLogs.count { it.sessionType == "WORK" && it.completed }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(date, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                modifier = Modifier.weight(1f))
                            Text("🍅 $pomodoros  |  作業${workSecs/60}分  |  休憩${breakSecs/60}分",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                items(dayLogs) { log ->
                    LogItem(log, timeFormat)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: WorkLog, timeFormat: SimpleDateFormat) {
    val isWork   = log.sessionType == "WORK"
    val icon     = if (isWork) "🍅" else "☕"
    val typeText = if (isWork) "作業" else "休憩"
    val durMin   = log.actualSeconds / 60
    val durSec   = log.actualSeconds % 60
    val timeStr  = timeFormat.format(Date(log.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
        Column(Modifier.weight(1f)) {
            Text("$typeText  ラップ ${log.lapNumber}",
                fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("$timeStr  |  ${durMin}分${durSec}秒",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!log.completed) {
            Badge { Text("中断") }
        }
    }
}
