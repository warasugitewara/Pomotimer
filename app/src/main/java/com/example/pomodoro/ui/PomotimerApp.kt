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
fun PomotimerApp(
    uiState: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSetWorkDuration: (Int) -> Unit,
    onSetBreakDuration: (Int) -> Unit
) {
    val minutes = uiState.remainingSeconds / 60
    val seconds = uiState.remainingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)
    val modeText = if (uiState.isWorkMode) "作業中" else "休憩中"

    var workInput by remember { mutableStateOf(uiState.preferredWorkDurationMinutes.toString()) }
    var breakInput by remember { mutableStateOf(uiState.preferredBreakDurationMinutes.toString()) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = modeText, fontSize = 22.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = timeText, fontSize = 72.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "ラップ ${uiState.currentLap}  完了 ${uiState.completedLaps}", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState.isRunning) {
                    Button(onClick = onPause) { Text("一時停止") }
                } else {
                    Button(onClick = onStart) { Text("スタート") }
                }
                OutlinedButton(onClick = onReset) { Text("リセット") }
            }

            Spacer(modifier = Modifier.height(40.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))

            Text("設定", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("作業時間 (分):")
                OutlinedTextField(
                    value = workInput,
                    onValueChange = { workInput = it },
                    modifier = Modifier.width(80.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Button(onClick = {
                    workInput.toIntOrNull()?.takeIf { it in 1..120 }?.let(onSetWorkDuration)
                }) { Text("設定") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("休憩時間 (分):")
                OutlinedTextField(
                    value = breakInput,
                    onValueChange = { breakInput = it },
                    modifier = Modifier.width(80.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Button(onClick = {
                    breakInput.toIntOrNull()?.takeIf { it in 1..60 }?.let(onSetBreakDuration)
                }) { Text("設定") }
            }

            Spacer(modifier = Modifier.height(24.dp))
            val totalWorkMin = uiState.totalWorkSecondsToday / 60
            Text("本日の作業時間: ${totalWorkMin}分", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
