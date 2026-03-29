package com.example.pomodoro.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    val (modeText, modeColor) = when {
        uiState.isWorkMode  -> "作業中"    to MaterialTheme.colorScheme.primary
        uiState.isLongBreak -> "長休憩中"  to MaterialTheme.colorScheme.tertiary
        else                -> "休憩中"    to MaterialTheme.colorScheme.secondary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // アラームバナー (洗練されたデザイン)
        AnimatedVisibility(
            visible = uiState.isAlarmPlaying,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("タイマー終了", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = onStopAlarm) {
                        Text("アラームを停止", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // 円形タイマー
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            val progress = (uiState.remainingSeconds.toFloat() / uiState.totalSeconds.coerceAtLeast(1L))
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 1000),
                label = "TimerProgress"
            )

            // 背景の円
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = modeColor.copy(alpha = 0.1f),
                    style = Stroke(width = 12.dp.toPx())
                )
            }

            // 進捗の円
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = modeColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = modeText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = modeColor.copy(alpha = 0.8f)
                )
                val minutes = uiState.remainingSeconds / 60
                val seconds = uiState.remainingSeconds % 60
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = "Laps ${uiState.currentLap} / ${uiState.completedLaps}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        // コントロールボタン
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onReset,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            LargeFloatingActionButton(
                onClick = { if (uiState.isRunning) onPause() else onStart() },
                containerColor = modeColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp)
                )
            }

            // 統計などのショートカット用（将来用）
            IconButton(
                onClick = { /* TODO: Open Quick Stats */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.BarChart, contentDescription = "Stats", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(40.dp))

        // クイック設定 (整理されたデザイン)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("クイック設定", fontWeight = FontWeight.Bold)
                }

                QuickDurationSlider(
                    label = "作業",
                    value = uiState.preferredWorkDurationMinutes,
                    onValueChange = onSetWorkDuration,
                    color = MaterialTheme.colorScheme.primary,
                    range = 5f..60f
                )

                QuickDurationSlider(
                    label = "休憩",
                    value = uiState.preferredBreakDurationMinutes,
                    onValueChange = onSetBreakDuration,
                    color = MaterialTheme.colorScheme.secondary,
                    range = 1f..30f
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun QuickDurationSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    color: Color,
    range: ClosedFloatingPointRange<Float>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("${value}分", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            )
        )
    }
}
