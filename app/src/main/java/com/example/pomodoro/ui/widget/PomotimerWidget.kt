package com.example.pomodoro.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.pomodoro.data.SettingsRepository
import com.example.pomodoro.model.TimerState
import com.example.pomodoro.service.TimerService

/**
 * ホーム画面ウィジェット。TimerService.uiState を直接読み、状態変化時は
 * TimerService 側から PomotimerWidget().updateAll(context) で再描画を要求する。
 */
class PomotimerWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(90.dp, 50.dp),
            DpSize(180.dp, 60.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val live = TimerService.uiState.value
        // プロセス死亡直後はサービスが未起動で uiState が既定値のままのため、
        // DataStore に保存されたスナップショットから復元して「25:00」誤表示を防ぐ
        val state = if (live != TimerState()) live else restoreStateFromSnapshot(context)
        provideContent {
            GlanceTheme {
                WidgetContent(state)
            }
        }
    }

    private suspend fun restoreStateFromSnapshot(context: Context): TimerState {
        val snap = SettingsRepository(context).readTimerSnapshot() ?: return TimerState()
        if (!snap.state.isRunning) return snap.state
        val remaining = ((snap.endAtMillis - System.currentTimeMillis()) / 1000).coerceAtLeast(0L)
        return snap.state.copy(isRunning = false, remainingSeconds = remaining)
    }
}

@Composable
private fun WidgetContent(state: TimerState) {
    val modeLabel = when {
        state.isAlarmPlaying -> "⏰ 終了"
        state.isWorkMode     -> "🍅 作業中"
        state.isLongBreak    -> "🌴 長休憩"
        else                  -> "☕ 休憩中"
    }
    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = modeLabel,
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontWeight = FontWeight.Medium)
        )
        Text(
            text = "%02d:%02d".format(minutes, seconds),
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        )
        Row {
            Text(
                text = if (state.isRunning) "⏸ 一時停止" else "▶ 開始",
                modifier = GlanceModifier
                    .padding(4.dp)
                    .clickable(actionRunCallback<ToggleTimerAction>()),
                style = TextStyle(color = GlanceTheme.colors.primary)
            )
            Text(
                text = "⟲ リセット",
                modifier = GlanceModifier
                    .padding(4.dp)
                    .clickable(actionRunCallback<ResetTimerAction>()),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
        }
    }
}

class ToggleTimerAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val state = TimerService.uiState.value
        if (state.isRunning) TimerService.pauseTimer(context) else TimerService.startTimer(context)
    }
}

class ResetTimerAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        TimerService.resetTimer(context)
    }
}
