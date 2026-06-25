package com.example.pomodoro.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.model.TimerState
import com.example.pomodoro.ui.theme.TimerDigitStyle

@Composable
fun TimerScreen(
    uiState: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onStopAlarm: () -> Unit,
    onOpenStats: () -> Unit,
    onSetWorkDuration: (Int) -> Unit,
    onSetBreakDuration: (Int) -> Unit,
    onSetLongBreakDuration: (Int) -> Unit,
    onSetLongBreakInterval: (Int) -> Unit,
    onSetTaskName: (String?) -> Unit
) {
    val (modeText, modeColor) = when {
        uiState.isWorkMode  -> "FOCUS"  to MaterialTheme.colorScheme.primary
        uiState.isLongBreak -> "LONG BREAK" to MaterialTheme.colorScheme.tertiary
        else                -> "BREAK"  to MaterialTheme.colorScheme.secondary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── アラームバナー ────────────────────────────────
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
                    Text("タイマー終了", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = onStopAlarm) {
                        Text("アラームを停止", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── タスク名（作業開始前のみ編集可） ────────────────
        if (uiState.isWorkMode) {
            TaskNameField(
                value = uiState.currentTaskName ?: "",
                enabled = !uiState.isRunning,
                onValueChange = { onSetTaskName(it.ifBlank { null }) }
            )
            Spacer(Modifier.height(16.dp))
        }

        // ── 円形タイマー ──────────────────────────────────
        TimerRing(uiState = uiState, modeText = modeText, modeColor = modeColor)

        Spacer(Modifier.height(24.dp))

        // ── ポモドーロ・サイクル表示 ──────────────────────
        CycleIndicator(
            interval = uiState.longBreakInterval,
            done = uiState.pomodorosInCycle,
            isWorkMode = uiState.isWorkMode,
            activeColor = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(40.dp))

        // ── コントロールボタン ────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onReset, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "リセット", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            LargeFloatingActionButton(
                onClick = { if (uiState.isRunning) onPause() else onStart() },
                containerColor = modeColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isRunning) "一時停止" else "開始",
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(onClick = onOpenStats, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.BarChart, contentDescription = "統計を見る", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(40.dp))

        // ── クイック設定 ──────────────────────────────────
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
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("クイック設定", style = MaterialTheme.typography.titleSmall)
                }

                QuickDurationSlider(
                    label = "作業",
                    value = uiState.preferredWorkDurationMinutes,
                    onValueChange = onSetWorkDuration,
                    color = MaterialTheme.colorScheme.primary,
                    range = 1f..180f
                )
                QuickDurationSlider(
                    label = "休憩",
                    value = uiState.preferredBreakDurationMinutes,
                    onValueChange = onSetBreakDuration,
                    color = MaterialTheme.colorScheme.secondary,
                    range = 1f..60f
                )
                QuickDurationSlider(
                    label = "長休憩",
                    value = uiState.preferredLongBreakDurationMinutes,
                    onValueChange = onSetLongBreakDuration,
                    color = MaterialTheme.colorScheme.tertiary,
                    range = 1f..120f
                )
                QuickDurationSlider(
                    label = "長休憩までの回数",
                    value = uiState.longBreakInterval,
                    onValueChange = onSetLongBreakInterval,
                    color = MaterialTheme.colorScheme.tertiary,
                    range = 2f..10f,
                    unit = "回"
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TimerRing(uiState: TimerState, modeText: String, modeColor: Color) {
    val progress = (uiState.remainingSeconds.toFloat() / uiState.totalSeconds.coerceAtLeast(1L))
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "TimerProgress"
    )

    // 実行中だけリングがわずかに「呼吸」する（控えめ・reduce-motion配慮）
    val breathe = rememberInfiniteTransition(label = "breathe")
    val pulseAlpha by if (uiState.isRunning) {
        breathe.animateFloat(
            initialValue = 0.85f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(288.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // トラック
            drawCircle(color = modeColor.copy(alpha = 0.10f), style = Stroke(width = 14.dp.toPx()))
            // 進捗
            drawArc(
                color = modeColor.copy(alpha = pulseAlpha),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = modeText,
                style = MaterialTheme.typography.labelLarge,
                color = modeColor,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(4.dp))
            val minutes = uiState.remainingSeconds / 60
            val seconds = uiState.remainingSeconds % 60
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                style = TimerDigitStyle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "LAP ${uiState.currentLap}  ·  ${uiState.completedLaps} done",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * 長休憩インターバルに対する現在サイクルの進捗をドット列で示す。
 * 塗り＝完了済みポモドーロ、リング＝これからのポモドーロ。
 */
@Composable
private fun CycleIndicator(interval: Int, done: Int, isWorkMode: Boolean, activeColor: Color) {
    if (interval <= 1) return
    val count = interval.coerceIn(1, 12)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            val filled = i < done
            // 作業中で次に進むドットを強調
            val isCurrent = isWorkMode && i == done
            val size = if (isCurrent) 12.dp else 9.dp
            val color = when {
                filled    -> activeColor
                isCurrent -> activeColor.copy(alpha = 0.55f)
                else      -> activeColor.copy(alpha = 0.18f)
            }
            Surface(modifier = Modifier.size(size), color = color, shape = CircleShape) {}
        }
    }
}

@Composable
fun QuickDurationSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    color: Color,
    range: ClosedFloatingPointRange<Float>,
    unit: String = "min"
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "$value$unit",
                style = MaterialTheme.typography.labelLarge,
                fontFamily = com.example.pomodoro.ui.theme.JetBrainsMono,
                color = color
            )
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

@Composable
private fun TaskNameField(value: String, enabled: Boolean, onValueChange: (String) -> Unit) {
    var draft by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = draft,
        onValueChange = { draft = it; onValueChange(it) },
        enabled = enabled,
        label = { Text("タスク（任意）") },
        placeholder = { Text("例: 数学の勉強") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(0.85f)
    )
}
