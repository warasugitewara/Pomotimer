package com.example.pomodoro.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.pomodoro.data.DailyStat
import com.example.pomodoro.data.WorkLog
import com.example.pomodoro.ui.theme.JetBrainsMono
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkLogScreen(
    selectedDate: String,
    logs: List<WorkLog>,
    availableDates: List<String>,
    dailyStats: List<DailyStat>,
    totalPomodoros: Int,
    totalWorkSeconds: Long,
    streak: Int,
    statsRangeDays: Int,
    longestFocusSeconds: Long,
    statsLast30Days: List<DailyStat>,
    onSetStatsRange: (Int) -> Unit,
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
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── 累計サマリ + グラフ ───────────────────
            item {
                OverviewSection(
                    dailyStats = dailyStats,
                    totalPomodoros = totalPomodoros,
                    totalWorkSeconds = totalWorkSeconds,
                    streak = streak,
                    statsRangeDays = statsRangeDays,
                    longestFocusSeconds = longestFocusSeconds,
                    statsLast30Days = statsLast30Days,
                    onSetStatsRange = onSetStatsRange
                )
            }

            // ── セクション見出し ───────────────────────
            item {
                SectionHeader("日別の記録", Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp))
            }

            // ── 日付ナビゲーションバー ─────────────────
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { nextDate?.let(onSelectDate) }, enabled = nextDate != null) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "次の日")
                        }
                    }
                }
            }

            if (logs.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Text("この日の記録はありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                item {
                    DaySummaryCard(logs = logs, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    Spacer(Modifier.height(8.dp))
                }
                items(logs, key = { it.id }) { log ->
                    LogItem(log = log, onDelete = { onDeleteLog(log.id) })
                }
            }
        }
    }
}

@Composable
private fun OverviewSection(
    dailyStats: List<DailyStat>,
    totalPomodoros: Int,
    totalWorkSeconds: Long,
    streak: Int,
    statsRangeDays: Int,
    longestFocusSeconds: Long,
    statsLast30Days: List<DailyStat>,
    onSetStatsRange: (Int) -> Unit
) {
    var groupByWeekday by remember { mutableStateOf(false) }

    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // 期間別サマリ（今日/今週/今月/累計）
        val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
        val weekAgoKey = remember { dateKeyDaysAgo(7) }
        val todayPomos = statsLast30Days.firstOrNull { it.date == todayKey }?.pomodoros ?: 0
        val weekPomos  = statsLast30Days.filter { it.date >= weekAgoKey }.sumOf { it.pomodoros }
        val monthPomos = statsLast30Days.sumOf { it.pomodoros }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodTile("今日", todayPomos, Modifier.weight(1f))
            PeriodTile("今週", weekPomos, Modifier.weight(1f))
            PeriodTile("今月", monthPomos, Modifier.weight(1f))
            PeriodTile("累計", totalPomodoros, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        // 累計スタッツ（mono数値）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(Icons.Default.LocalFireDepartment, streak.toString(), "日連続", Modifier.weight(1f), MaterialTheme.colorScheme.primary)
            StatTile(Icons.Default.Schedule, "${totalWorkSeconds / 3600}h", "総作業", Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
            StatTile(Icons.Default.EmojiEvents, "${longestFocusSeconds / 60}min", "最長集中", Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
        }

        Spacer(Modifier.height(16.dp))

        // グラフカード
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ポモドーロ推移", style = MaterialTheme.typography.titleSmall)
                    SingleChoiceSegmentedButtonRow {
                        listOf(7, 30).forEachIndexed { idx, days ->
                            SegmentedButton(
                                selected = statsRangeDays == days,
                                onClick = { onSetStatsRange(days) },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2)
                            ) { Text("${days}日") }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    SingleChoiceSegmentedButtonRow {
                        listOf("日別" to false, "曜日別" to true).forEachIndexed { idx, (label, value) ->
                            SegmentedButton(
                                selected = groupByWeekday == value,
                                onClick = { groupByWeekday = value },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2)
                            ) { Text(label, fontSize = 12.sp) }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (dailyStats.all { it.pomodoros == 0 }) {
                    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Text("まだデータがありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val chartStats = if (groupByWeekday) aggregateByWeekday(dailyStats) else dailyStats
                    PomodoroBarChart(stats = chartStats)
                }
            }
        }
    }
}

@Composable
private fun PeriodTile(label: String, pomodoros: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(pomodoros.toString(), fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun dateKeyDaysAgo(days: Int): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(days - 1)) }
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
}

/** 曜日（月〜日）ごとにポモドーロ数・作業秒を合算する。date欄には曜日名を入れる。 */
private fun aggregateByWeekday(stats: List<DailyStat>): List<DailyStat> {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val weekdayFmt = SimpleDateFormat("E", Locale.JAPANESE)
    val order = listOf("月", "火", "水", "木", "金", "土", "日")
    val buckets = LinkedHashMap<String, Pair<Int, Long>>()
    order.forEach { buckets[it] = 0 to 0L }

    stats.forEach { stat ->
        val date = try { fmt.parse(stat.date) } catch (_: Exception) { null } ?: return@forEach
        val label = weekdayFmt.format(date).removeSuffix("曜日").let { if (it.length > 1) it.take(1) else it }
        val key = order.firstOrNull { it == label } ?: return@forEach
        val (pomos, secs) = buckets[key] ?: (0 to 0L)
        buckets[key] = (pomos + stat.pomodoros) to (secs + stat.workSeconds)
    }

    return order.map { day -> DailyStat(day, buckets[day]?.first ?: 0, buckets[day]?.second ?: 0L) }
}

@Composable
private fun StatTile(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Text(value, fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = modifier
    )
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
                Text(
                    log.taskName?.takeIf { it.isNotBlank() }
                        ?: if (isWork) "Work Session" else "Break Session",
                    fontWeight = FontWeight.SemiBold
                )
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
