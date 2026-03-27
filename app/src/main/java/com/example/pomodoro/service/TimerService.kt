package com.example.pomodoro.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.pomodoro.MainActivity
import com.example.pomodoro.R
import com.example.pomodoro.data.AppDatabase
import com.example.pomodoro.data.SettingsRepository
import com.example.pomodoro.data.WorkLog
import com.example.pomodoro.model.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimerService : LifecycleService() {

    companion object {
        const val ACTION_START              = "com.example.pomodoro.START"
        const val ACTION_PAUSE              = "com.example.pomodoro.PAUSE"
        const val ACTION_STOP               = "com.example.pomodoro.STOP"
        const val ACTION_RESET              = "com.example.pomodoro.RESET"
        const val ACTION_STOP_ALARM         = "com.example.pomodoro.STOP_ALARM"
        const val ACTION_SET_WORK           = "com.example.pomodoro.SET_WORK"
        const val ACTION_SET_BREAK          = "com.example.pomodoro.SET_BREAK"
        const val ACTION_SET_LONG_BREAK     = "com.example.pomodoro.SET_LONG_BREAK"
        const val ACTION_SET_LB_INTERVAL    = "com.example.pomodoro.SET_LB_INTERVAL"

        const val CHANNEL_TIMER  = "timer_progress"
        const val CHANNEL_ALERT  = "timer_alert"
        const val NOTIF_ID_TIMER = 1
        const val NOTIF_ID_ALERT = 2

        private val _uiState = MutableStateFlow(TimerState())
        val uiState: StateFlow<TimerState> = _uiState.asStateFlow()

        fun startTimer(ctx: Context)       = ctx.startService(svc(ctx, ACTION_START))
        fun pauseTimer(ctx: Context)       = ctx.startService(svc(ctx, ACTION_PAUSE))
        fun stopService(ctx: Context)      = ctx.startService(svc(ctx, ACTION_STOP))
        fun resetTimer(ctx: Context)       = ctx.startService(svc(ctx, ACTION_RESET))
        fun stopAlarm(ctx: Context)        = ctx.startService(svc(ctx, ACTION_STOP_ALARM))
        fun setWorkDuration(ctx: Context, minutes: Int) =
            ctx.startService(svc(ctx, ACTION_SET_WORK).putExtra("minutes", minutes))
        fun setBreakDuration(ctx: Context, minutes: Int) =
            ctx.startService(svc(ctx, ACTION_SET_BREAK).putExtra("minutes", minutes))
        fun setLongBreakDuration(ctx: Context, minutes: Int) =
            ctx.startService(svc(ctx, ACTION_SET_LONG_BREAK).putExtra("minutes", minutes))
        fun setLongBreakInterval(ctx: Context, count: Int) =
            ctx.startService(svc(ctx, ACTION_SET_LB_INTERVAL).putExtra("count", count))

        private fun svc(ctx: Context, action: String) =
            Intent(ctx, TimerService::class.java).apply { this.action = action }
    }

    private lateinit var notifManager: NotificationManager
    private lateinit var settings: SettingsRepository
    private lateinit var db: AppDatabase
    private var timerJob: Job? = null
    private var lastTickTime = 0L
    private var sessionStartTime = 0L
    private var sessionStartRemaining = 0L
    private var alarmPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        notifManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        settings = SettingsRepository(this)
        db = AppDatabase.getInstance(this)
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START           -> startTimer()
            ACTION_PAUSE           -> pauseTimer()
            ACTION_STOP            -> stopAll()
            ACTION_RESET           -> resetTimer()
            ACTION_STOP_ALARM      -> stopAlarm()
            ACTION_SET_WORK        -> setWorkDuration(intent.getIntExtra("minutes", 25))
            ACTION_SET_BREAK       -> setBreakDuration(intent.getIntExtra("minutes", 5))
            ACTION_SET_LONG_BREAK  -> _uiState.update { it.copy(preferredLongBreakDurationMinutes = intent.getIntExtra("minutes", 15)) }
            ACTION_SET_LB_INTERVAL -> _uiState.update { it.copy(longBreakInterval = intent.getIntExtra("count", 4)) }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? { super.onBind(intent); return null }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    // ────────────── Timer control ──────────────

    private fun startTimer() {
        if (_uiState.value.isRunning) return
        sessionStartTime = System.currentTimeMillis()
        sessionStartRemaining = _uiState.value.remainingSeconds
        _uiState.update { it.copy(isRunning = true) }
        lastTickTime = System.currentTimeMillis()
        startForeground(NOTIF_ID_TIMER, buildTimerNotification())
        timerJob = lifecycleScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(500L)
                val now = System.currentTimeMillis()
                val elapsed = (now - lastTickTime) / 1000
                if (elapsed >= 1) {
                    _uiState.update { s ->
                        s.copy(remainingSeconds = (s.remainingSeconds - elapsed).coerceAtLeast(0L))
                    }
                    lastTickTime += elapsed * 1000
                    notifManager.notify(NOTIF_ID_TIMER, buildTimerNotification())
                }
                if (_uiState.value.remainingSeconds <= 0L) break
            }
            if (_uiState.value.isRunning) onTimerFinished()
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
        notifManager.notify(NOTIF_ID_TIMER, buildTimerNotification())
    }

    private fun resetTimer() {
        timerJob?.cancel()
        stopAlarm()
        _uiState.update { s -> s.copy(remainingSeconds = s.totalSeconds, isRunning = false) }
        notifManager.notify(NOTIF_ID_TIMER, buildTimerNotification())
    }

    private fun setWorkDuration(minutes: Int) {
        timerJob?.cancel()
        val secs = minutes * 60L
        _uiState.update { s ->
            s.copy(
                preferredWorkDurationMinutes = minutes,
                totalSeconds = secs, remainingSeconds = secs,
                isRunning = false, isWorkMode = true
            )
        }
        notifManager.notify(NOTIF_ID_TIMER, buildTimerNotification())
    }

    private fun setBreakDuration(minutes: Int) {
        _uiState.update { it.copy(preferredBreakDurationMinutes = minutes) }
    }

    private fun stopAll() {
        timerJob?.cancel()
        stopAlarm()
        _uiState.value = TimerState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopAlarm() {
        try { alarmPlayer?.let { if (it.isPlaying) it.stop(); it.release() } } catch (_: Exception) {}
        alarmPlayer = null
        _uiState.update { it.copy(isAlarmPlaying = false) }
        notifManager.cancel(NOTIF_ID_ALERT)
    }

    // ────────────── Timer finished ──────────────

    private fun onTimerFinished() {
        val state = _uiState.value
        val wasWork = state.isWorkMode
        val actual  = sessionStartRemaining - state.remainingSeconds

        lifecycleScope.launch {
            db.workLogDao().insert(WorkLog(
                sessionType    = if (wasWork) "WORK" else if (state.isLongBreak) "LONG_BREAK" else "BREAK",
                plannedSeconds = state.totalSeconds,
                actualSeconds  = actual,
                completed      = true,
                lapNumber      = state.currentLap
            ))
        }

        // 次モードを計算
        val newPomosInCycle = if (wasWork) state.pomodorosInCycle + 1 else state.pomodorosInCycle
        val takeLongBreak   = wasWork && (newPomosInCycle >= state.longBreakInterval)
        val nextIsWork      = !wasWork
        val nextIsLongBreak = !nextIsWork && takeLongBreak
        val nextPomosInCycle = if (takeLongBreak) 0 else newPomosInCycle
        val nextDurMin = when {
            nextIsWork      -> state.preferredWorkDurationMinutes
            nextIsLongBreak -> state.preferredLongBreakDurationMinutes
            else            -> state.preferredBreakDurationMinutes
        }

        _uiState.update { s ->
            s.copy(
                isRunning             = false,
                completedLaps         = if (wasWork) s.completedLaps + 1 else s.completedLaps,
                totalWorkSecondsToday = if (wasWork) s.totalWorkSecondsToday + s.totalSeconds else s.totalWorkSecondsToday,
                isWorkMode            = nextIsWork,
                isLongBreak           = nextIsLongBreak,
                totalSeconds          = nextDurMin * 60L,
                remainingSeconds      = nextDurMin * 60L,
                currentLap            = if (nextIsWork) s.currentLap + 1 else s.currentLap,
                pomodorosInCycle      = nextPomosInCycle
            )
        }

        lifecycleScope.launch {
            val notifOn = settings.notificationEnabled.first()
            val soundOn = settings.soundEnabled.first()
            val vibOn   = settings.vibrationEnabled.first()

            if (soundOn)  playAlarmSound()
            if (vibOn)    vibrate()

            if (notifOn) {
                val label = when {
                    wasWork && takeLongBreak -> "お疲れ様！長休憩の時間です 🌙"
                    wasWork                  -> "作業時間終了！休憩しましょう ☕"
                    else                     -> "休憩終了！作業を再開しましょう 🍅"
                }
                notifManager.notify(NOTIF_ID_ALERT,
                    NotificationCompat.Builder(this@TimerService, CHANNEL_ALERT)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(label)
                        .setContentText("タップして確認")
                        .setAutoCancel(false)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(openAppIntent())
                        .addAction(android.R.drawable.ic_delete, "アラームを停止",
                            actionIntent(ACTION_STOP_ALARM))
                        .build()
                )
            }
            notifManager.notify(NOTIF_ID_TIMER, buildTimerNotification())
        }
    }

    // ────────────── Notification ──────────────

    private fun createNotificationChannels() {
        val timerCh = NotificationChannel(CHANNEL_TIMER, "タイマー進行", NotificationManager.IMPORTANCE_LOW)
            .apply { description = "タイマーの残り時間を表示します" }

        val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val alertCh  = NotificationChannel(CHANNEL_ALERT, "タイマー終了通知", NotificationManager.IMPORTANCE_HIGH)
            .apply {
                description = "作業・休憩の終了を通知します"
                setSound(alertUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                enableVibration(true)
            }

        notifManager.createNotificationChannel(timerCh)
        notifManager.createNotificationChannel(alertCh)
    }

    private fun buildTimerNotification(): Notification {
        val state    = _uiState.value
        val mins     = state.remainingSeconds / 60
        val secs     = state.remainingSeconds % 60
        val timeStr  = "%02d:%02d".format(mins, secs)
        val modeStr  = when {
            state.isWorkMode   -> "🍅 作業中"
            state.isLongBreak  -> "🌙 長休憩中"
            else               -> "☕ 休憩中"
        }
        val progress = ((state.totalSeconds - state.remainingSeconds).toFloat() /
                        state.totalSeconds.coerceAtLeast(1L) * 100).toInt()
        val lapStr   = "ラップ ${state.currentLap}  ／  完了 ${state.completedLaps} ポモドーロ"

        val pauseOrStart = if (state.isRunning)
            NotificationCompat.Action(android.R.drawable.ic_media_pause, "一時停止", actionIntent(ACTION_PAUSE))
        else
            NotificationCompat.Action(android.R.drawable.ic_media_play, "スタート", actionIntent(ACTION_START))
        val stopAction = NotificationCompat.Action(android.R.drawable.ic_delete, "停止", actionIntent(ACTION_STOP))

        return NotificationCompat.Builder(this, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$modeStr  $timeStr")
            .setContentText(lapStr)
            .setProgress(100, progress, false)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$timeStr\n$lapStr")
                .setBigContentTitle(modeStr))
            .addAction(pauseOrStart)
            .addAction(stopAction)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openAppIntent())
            .build()
    }

    private fun actionIntent(action: String): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
        return PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    // ────────────── Sound & Vibration ──────────────

    private fun playAlarmSound() {
        stopAlarm()
        _uiState.update { it.copy(isAlarmPlaying = true) }
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            alarmPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                setDataSource(applicationContext, uri)
                isLooping = false
                prepare()
                start()
                setOnCompletionListener {
                    release()
                    alarmPlayer = null
                    _uiState.update { it.copy(isAlarmPlaying = false) }
                    notifManager.cancel(NOTIF_ID_ALERT)
                }
            }
        } catch (e: Exception) {
            try {
                RingtoneManager.getRingtone(applicationContext,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))?.play()
            } catch (_: Exception) {}
            _uiState.update { it.copy(isAlarmPlaying = false) }
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        val pattern = longArrayOf(0, 400, 150, 400, 150, 700)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator
                .vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            else
                v.vibrate(pattern, -1)
        }
    }
}
