package com.example.pomodoro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.model.TimerState

@Composable
fun TimerScreen(
    uiState: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onStopAlarm: () -> Unit,
    onSetWorkDuration: (Int) -> Unit,
    onSetBreakDuration: (Int) -> Unit,
    onSetLongBreakDuration: (Int) -> Unit,
    onSetLongBreakInterval: (Int) -> Unit
) {
    val minutes  = uiState.remainingSeconds / 60
    val seconds  = uiState.remainingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)
    val (modeText, modeColor) = when {
        uiState.isWorkMode  -> "🍅 作業中"    to MaterialTheme.colorScheme.error
        uiState.isLongBreak -> "🌙 長休憩中"  to MaterialTheme.colorScheme.tertiary
        else                -> "☕ 休憩中"    to MaterialTheme.colorScheme.primary
    }
    val progress = (uiState.totalSeconds - uiState.remainingSeconds).toFloat() /
                   uiState.totalSeconds.coerceAtLeast(1L)

    var workInput      by remember { mutableStateOf(uiState.preferredWorkDurationMinutes.toString()) }
    var breakInput     by remember { mutableStateOf(uiState.preferredBreakDurationMinutes.toString()) }
    var longBreakInput by remember { mutableStateOf(uiState.preferredLongBreakDurationMinutes.toString()) }
    var intervalInput  by remember { mutableStateOf(uiState.longBreakInterval.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // アラームバナー
        if (uiState.isAlarmPlaying) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🔔 アラームが鳴っています",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    FilledTonalButton(
                        onClick = onStopAlarm,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor   = MaterialTheme.colorScheme.onError
                        )
                    ) { Text("停止", fontSize = 13.sp) }
                }
            }
        }

        Text(text = modeText, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = modeColor)

        Text(text = timeText, fontSize = 80.sp, fontWeight = FontWeight.Bold)

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = modeColor
        )

        // ラップ情報
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("ラップ ${uiState.currentLap}  ／  完了 ${uiState.completedLaps} ポモドーロ",
                fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val totalMin = uiState.totalWorkSecondsToday / 60
            Text("本日の作業時間: ${totalMin}分", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            // 長休憩まであと何ポモドーロか
            val remaining = uiState.longBreakInterval - uiState.pomodorosInCycle
            if (!uiState.isWorkMode) {
                Text("次の長休憩まで: あと${remaining}ポモドーロ",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (uiState.isRunning) {
                Button(onClick = onPause, colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("⏸ 一時停止")
                }
            } else {
                Button(onClick = onStart) { Text("▶ スタート") }
            }
            OutlinedButton(onClick = onReset) { Text("↺ リセット") }
        }

        HorizontalDivider()

        Text("タイマー設定", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

        DurationRow("作業時間（分）",    workInput,      { workInput = it },      1..120) { onSetWorkDuration(it) }
        DurationRow("短休憩時間（分）",  breakInput,     { breakInput = it },     1..60)  { onSetBreakDuration(it) }
        DurationRow("長休憩時間（分）",  longBreakInput, { longBreakInput = it }, 1..120) { onSetLongBreakDuration(it) }
        DurationRow("長休憩の間隔（回）", intervalInput, { intervalInput = it },  1..20)  { onSetLongBreakInterval(it) }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DurationRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    range: IntRange,
    onApply: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(72.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        FilledTonalButton(onClick = {
            value.toIntOrNull()?.takeIf { it in range }?.let(onApply)
        }) { Text("設定") }
    }
}
