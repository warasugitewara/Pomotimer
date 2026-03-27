package com.example.pomodoro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
    onSetWorkDuration: (Int) -> Unit,
    onSetBreakDuration: (Int) -> Unit
) {
    val minutes  = uiState.remainingSeconds / 60
    val seconds  = uiState.remainingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)
    val modeText = if (uiState.isWorkMode) "🍅 作業中" else "☕ 休憩中"
    val progress = (uiState.totalSeconds - uiState.remainingSeconds).toFloat() /
                   uiState.totalSeconds.coerceAtLeast(1L)

    var workInput  by remember { mutableStateOf(uiState.preferredWorkDurationMinutes.toString()) }
    var breakInput by remember { mutableStateOf(uiState.preferredBreakDurationMinutes.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Text(text = modeText, fontSize = 22.sp, fontWeight = FontWeight.Medium,
            color = if (uiState.isWorkMode) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary)

        Text(text = timeText, fontSize = 80.sp, fontWeight = FontWeight.Bold)

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = if (uiState.isWorkMode) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
        )

        Text(
            "ラップ ${uiState.currentLap}  ／  完了 ${uiState.completedLaps} ポモドーロ",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val totalMin = uiState.totalWorkSecondsToday / 60
        Text("本日の作業時間: ${totalMin}分", fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary)

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

        Text("設定", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("作業時間（分）", Modifier.width(100.dp), fontSize = 13.sp)
            OutlinedTextField(
                value = workInput,
                onValueChange = { workInput = it },
                modifier = Modifier.width(72.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            FilledTonalButton(onClick = {
                workInput.toIntOrNull()?.takeIf { it in 1..120 }?.let(onSetWorkDuration)
            }) { Text("設定") }
        }

        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("休憩時間（分）", Modifier.width(100.dp), fontSize = 13.sp)
            OutlinedTextField(
                value = breakInput,
                onValueChange = { breakInput = it },
                modifier = Modifier.width(72.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            FilledTonalButton(onClick = {
                breakInput.toIntOrNull()?.takeIf { it in 1..60 }?.let(onSetBreakDuration)
            }) { Text("設定") }
        }
    }
}
