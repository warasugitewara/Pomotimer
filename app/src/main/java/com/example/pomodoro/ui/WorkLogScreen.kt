package com.example.pomodoro.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
        if (idx >= 0) sortedDates.getOrNull(idx + 1)
        else sortedDates.firstOrNull { it < selectedDate }
    }
    val nextDate = remember(selectedDate, sortedDates) {
        val idx = sortedDates.indexOf(selectedDate)
        if (idx >= 0) sortedDates.getOrNull(idx - 1)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("作業ログ", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "メニュー")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("この日のログを削除") },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                            onClick = { showMenu = false; showDeleteDayDialog = true },
                            enabled = logs.isNotEmpty()
                        )
                        DropdownMenuItem(
                            text = { Text("全ログを削除", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; showDeleteAllDialog = true },
                            enabled = availableDates.isNotEmpty()
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // ── 日付ナビゲーションバー ─────────────────
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { prevDate?.let(onSelectDate) }, enabled = prevDate != null) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "前の日")
                    }
                    Text(
                        formatDateDisplay(selectedDate),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { nextDate?.let(onSelectDate) }, enabled = nextDate != null) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "次の日")
                    }
                }
            }

            if (logs.isEmpty()) {
                Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Text("この日の記録はありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // ── 日次サマリー ──────────────────────────
                DaySummaryCard(logs = logs, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                Spacer(Modifier.height(8.dp))

                // ── ログ一覧 ──────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        LogItem(log = log, onDelete = { onDeleteLog(log.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySummaryCard(logs: List<WorkLog>, modifier: Modifier = Modifier) {
    val pomodoros = logs.count { it.sessionType == "WORK" && it.completed }
    val workMin   = logs.filter { it.sessionType == "WORK"  }.sumOf { it.actualSeconds } / 60
    val breakMin  = logs.filter { (it.sessionType == "BREAK" || it.sessionType == "LONG_BREAK") }.sumOf { it.actualSeconds } / 60

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryItem(Icons.Default.Timer, "$pomodoros", "Pomos", MaterialTheme.colorScheme.primary)
            SummaryItem(Icons.Default.WorkOutline, "${workMin}m", "Work", MaterialTheme.colorScheme.secondary)
            SummaryItem(Icons.Default.Coffee, "${breakMin}m", "Break", MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun SummaryItem(icon: ImageVector, value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    val isWork = log.sessionType == "WORK"
    val icon = if (isWork) Icons.Default.Timer else Icons.Default.Coffee
    val color = if (isWork) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val durMin = log.actualSeconds / 60
    val durSec = log.actualSeconds % 60
    val timeStr = timeFormat.format(Date(log.timestamp))

    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isWork) "Work Session" else "Break Session", fontWeight = FontWeight.SemiBold)
                if (!log.completed) {
                    Spacer(Modifier.width(8.dp))
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = CircleShape) {
                        Text("中断", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        supportingContent = {
            Text("$timeStr  •  ${durMin}m ${durSec}s  •  Lap ${log.lapNumber}")
        },
        leadingContent = {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
        },
        trailingContent = {
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, "削除", tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
        }
    )
}

private fun formatDateDisplay(dateKey: String): String = try {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(dateKey)!!
    SimpleDateFormat("yyyy年M月d日(E)", Locale.JAPAN).format(parsed)
} catch (e: Exception) { dateKey }
