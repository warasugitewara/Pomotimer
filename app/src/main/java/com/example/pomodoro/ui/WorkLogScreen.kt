package com.example.pomodoro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkLogScreen(
    selectedDate: String,
    logs: List<WorkLog>,
    availableDates: List<String>,
    onSelectDate: (String) -> Unit,
    onDeleteLog: (Long) -> Unit,
    onDeleteDay: (String) -> Unit,
    onDeleteAll: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDayDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val sortedDates = remember(availableDates) { availableDates.sortedDescending() }
    val prevDate = remember(selectedDate, sortedDates) {
        val idx = sortedDates.indexOf(selectedDate)
        if (idx >= 0) sortedDates.getOrNull(idx + 1)   // 古い日
        else sortedDates.firstOrNull { it < selectedDate }
    }
    val nextDate = remember(selectedDate, sortedDates) {
        val idx = sortedDates.indexOf(selectedDate)
        if (idx >= 0) sortedDates.getOrNull(idx - 1)   // 新しい日
        else sortedDates.firstOrNull { it > selectedDate }
    }

    if (showDeleteDayDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDayDialog = false },
            title = { Text("この日のログを削除") },
            text = { Text("${formatDateDisplay(selectedDate)} のログをすべて削除しますか？\nこの操作は取り消せません。") },
            confirmButton = {
                TextButton(onClick = { onDeleteDay(selectedDate); showDeleteDayDialog = false }) {
                    Text("削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDayDialog = false }) { Text("キャンセル") }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("全ログを削除") },
            text = { Text("すべての作業ログを削除しますか？\nこの操作は取り消せません。") },
            confirmButton = {
                TextButton(onClick = { onDeleteAll(); showDeleteAllDialog = false }) {
                    Text("削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("キャンセル") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── トップバー ──────────────────────────────
        TopAppBar(
            title = { Text("作業ログ") },
            actions = {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "メニュー")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("この日のログを削除") },
                            onClick = { showMenu = false; showDeleteDayDialog = true },
                            enabled = logs.isNotEmpty()
                        )
                        DropdownMenuItem(
                            text = {
                                Text("全ログを削除",
                                    color = if (availableDates.isNotEmpty())
                                        MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
                            },
                            onClick = { showMenu = false; showDeleteAllDialog = true },
                            enabled = availableDates.isNotEmpty()
                        )
                    }
                }
            }
        )

        // ── 日付ナビゲーションバー ─────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { prevDate?.let(onSelectDate) }, enabled = prevDate != null) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft, "前の日",
                    tint = if (prevDate != null) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
            Text(
                formatDateDisplay(selectedDate),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            IconButton(onClick = { nextDate?.let(onSelectDate) }, enabled = nextDate != null) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight, "次の日",
                    tint = if (nextDate != null) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }

        HorizontalDivider()

        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📋", fontSize = 48.sp)
                    Text("この日の記録はありません",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            // ── 日次サマリー ──────────────────────────
            DaySummaryCard(logs = logs, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))

            // ── ログ一覧 ──────────────────────────────
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(logs, key = { it.id }) { log ->
                    LogItem(log = log, onDelete = { onDeleteLog(log.id) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun DaySummaryCard(logs: List<WorkLog>, modifier: Modifier = Modifier) {
    val pomodoros = logs.count { it.sessionType == "WORK" && it.completed }
    val workMin   = logs.filter { it.sessionType == "WORK"  }.sumOf { it.actualSeconds } / 60
    val breakMin  = logs.filter { it.sessionType == "BREAK" }.sumOf { it.actualSeconds } / 60

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryItem("🍅", "$pomodoros", "ポモドーロ")
            SummaryItem("⏱", "${workMin}分", "作業時間")
            SummaryItem("☕", "${breakMin}分", "休憩時間")
        }
    }
}

@Composable
private fun SummaryItem(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(icon, fontSize = 20.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LogItem(log: WorkLog, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.JAPAN) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("ログを削除") },
            text = { Text("このログエントリを削除しますか？") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showConfirm = false }) {
                    Text("削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("キャンセル") }
            }
        )
    }

    val isWork   = log.sessionType == "WORK"
    val icon     = if (isWork) "🍅" else "☕"
    val typeText = if (isWork) "作業" else "休憩"
    val durMin   = log.actualSeconds / 60
    val durSec   = log.actualSeconds % 60
    val timeStr  = timeFormat.format(Date(log.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("$typeText  ラップ ${log.lapNumber}",
                fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("$timeStr  |  ${durMin}分${durSec}秒",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!log.completed) {
            Badge(modifier = Modifier.padding(end = 8.dp)) { Text("中断") }
        }
        IconButton(onClick = { showConfirm = true }) {
            Icon(Icons.Default.Delete, contentDescription = "削除",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp))
        }
    }
}

private fun formatDateDisplay(dateKey: String): String = try {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(dateKey)!!
    SimpleDateFormat("yyyy年M月d日(E)", Locale.JAPAN).format(parsed)
} catch (e: Exception) { dateKey }
