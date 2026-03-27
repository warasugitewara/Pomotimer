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
        const val ACTION_START  = "com.example.pomodoro.START"
        const val ACTION_PAUSE  = "com.example.pomodoro.PAUSE"
        const val ACTION_STOP   = "com.example.pomodoro.STOP"
        const val ACTION_RESET  = "com.example.pomodoro.RESET"

        const val CHANNEL_TIMER  = "timer_progress"
        const val CHANNEL_ALERT  = "timer_alert"
        const val NOTIF_ID_TIMER = 1
        const val NOTIF_ID_ALERT = 2

        private val _uiState = MutableStateFlow(TimerState())
        val uiState: StateFlow<TimerState> = _uiState.asStateFlow()

        fun startTimer(ctx: Context)  = ctx.startService(Intent(ctx, TimerService::class.java).apply { action = ACTION_START })
        fun pauseTimer(ctx: Context)  = ctx.startService(Intent(ctx, TimerService::class.java).apply { action = ACTION_PAUSE })
        fun stopService(ctx: Context) = ctx.startService(Intent(ctx, TimerService::class.java).apply { action = ACTION_STOP })
        fun resetTimer(ctx: Context)  = ctx.startService(Intent(ctx, TimerService::class.java).apply { action = ACTION_RESET })
        fun setWorkDuration(ctx: Context, minutes: Int) =
            ctx.startService(Intent(ctx, TimerService::class.java).apply {
                action = "com.example.pomodoro.SET_WORK"
                putExtra("minutes", minutes)
            })
        fun setBreakDuration(ctx: Context, minutes: Int) =
            ctx.startService(Intent(ctx, TimerService::class.java).apply {
                action = "com.example.pomodoro.SET_BREAK"
                putExtra("minutes", minutes)
            })
    }

    private lateinit var notifManager: NotificationManager
    private lateinit var settings: SettingsRepository
    private lateinit var db: AppDatabase
    private var timerJob: Job? = null
    private var lastTickTime = 0L
    private var sessionStartTime = 0L
    private var sessionStartRemaining = 0L

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
            ACTION_START  -> startTimer()
            ACTION_PAUSE  -> pauseTimer()
            ACTION_STOP   -> stopAll()
            ACTION_RESET  -> resetTimer()
            "com.example.pomodoro.SET_WORK"  -> {
                val m = intent.getIntExtra("minutes", 25)
                setWorkDuration(m)
            }
            "com.example.pomodoro.SET_BREAK" -> {
                val m = intent.getIntExtra("minutes", 5)
                setBreakDuration(m)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
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
                        val newRemaining = (s.remainingSeconds - elapsed).coerceAtLeast(0L)
                        s.copy(remainingSeconds = newRemaining)
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
        _uiState.update { s -> s.copy(remainingSeconds = s.totalSeconds, isRunning = false) }
        notifManager.notify(NOTIF_ID_TIMER, buildTimerNotification())
    }

    private fun setWorkDuration(minutes: Int) {
        timerJob?.cancel()
        val secs = minutes * 60L
        _uiState.update { s ->
            s.copy(
                preferredWorkDurationMinutes = minutes,
                totalSeconds = secs,
                remainingSeconds = secs,
                isRunning = false,
                isWorkMode = true
            )
        }
        notifManager.notify(NOTIF_ID_TIMER, buildTimerNotification())
    }

    private fun setBreakDuration(minutes: Int) {
        _uiState.update { s -> s.copy(preferredBreakDurationMinutes = minutes) }
    }

    private fun stopAll() {
        timerJob?.cancel()
        _uiState.value = TimerState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onTimerFinished() {
        val state = _uiState.value
        val wasWork = state.isWorkMode
        val actual = sessionStartRemaining - state.remainingSeconds

        // 作業ログを保存
        lifecycleScope.launch {
            db.workLogDao().insert(
                WorkLog(
                    sessionType    = if (wasWork) "WORK" else "BREAK",
                    plannedSeconds = state.totalSeconds,
                    actualSeconds  = actual,
                    completed      = true,
                    lapNumber      = state.currentLap
                )
            )
        }

        // 次のモードへ切り替え
        val newWork = !wasWork
        val nextDurMin = if (newWork) state.preferredWorkDurationMinutes else state.preferredBreakDurationMinutes
        val nextSecs = nextDurMin * 60L
        _uiState.update { s ->
            s.copy(
                isRunning              = false,
                completedLaps          = if (wasWork) s.completedLaps + 1 else s.completedLaps,
                totalWorkSecondsToday  = if (wasWork) s.totalWorkSecondsToday + s.totalSeconds else s.totalWorkSecondsToday,
                isWorkMode             = newWork,
                totalSeconds           = nextSecs,
                remainingSeconds       = nextSecs,
                currentLap             = if (newWork) s.currentLap + 1 else s.currentLap
            )
        }

        lifecycleScope.launch {
            val notifEnabled = settings.notificationEnabled.first()
            val soundEnabled = settings.soundEnabled.first()

            if (soundEnabled) playAlarmSound()

            if (notifEnabled) {
                val label = if (wasWork) "作業時間が終了しました" else "休憩時間が終了しました"
                val next  = if (wasWork) "休憩を開始しましょう" else "次の作業を始めましょう"
                notifManager.notify(
                    NOTIF_ID_ALERT,
                    NotificationCompat.Builder(this@TimerService, CHANNEL_ALERT)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(label)
                        .setContentText(next)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(openAppIntent())
                        .build()
                )
            }

            notifManager.notify(NOTIF_ID_TIMER, buildTimerNotification())
        }
    }

    // ────────────── Notification ──────────────

    private fun createNotificationChannels() {
        val timerChannel = NotificationChannel(
            CHANNEL_TIMER, "タイマー進行", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "タイマーの残り時間を表示します" }

        val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val alertChannel = NotificationChannel(
            CHANNEL_ALERT, "タイマー終了通知", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "作業・休憩の終了を通知します"
            setSound(alertUri, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            enableVibration(true)
        }

        notifManager.createNotificationChannel(timerChannel)
        notifManager.createNotificationChannel(alertChannel)
    }

    private fun buildTimerNotification(): Notification {
        val state = _uiState.value
        val mins  = state.remainingSeconds / 60
        val secs  = state.remainingSeconds % 60
        val timeStr  = "%02d:%02d".format(mins, secs)
        val modeStr  = if (state.isWorkMode) "🍅 作業中" else "☕ 休憩中"
        val progress = ((state.totalSeconds - state.remainingSeconds).toFloat() / state.totalSeconds * 100).toInt()
        val lapStr   = "ラップ ${state.currentLap}  ／  完了 ${state.completedLaps} ポモドーロ"

        val pauseOrStart = if (state.isRunning) {
            NotificationCompat.Action(android.R.drawable.ic_media_pause, "一時停止",
                actionIntent(ACTION_PAUSE))
        } else {
            NotificationCompat.Action(android.R.drawable.ic_media_play, "スタート",
                actionIntent(ACTION_START))
        }
        val stopAction = NotificationCompat.Action(android.R.drawable.ic_delete, "停止",
            actionIntent(ACTION_STOP))

        return NotificationCompat.Builder(this, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$modeStr  $timeStr")
            .setContentText(lapStr)
            .setProgress(100, progress, false)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$timeStr\n$lapStr")
                    .setBigContentTitle(modeStr)
            )
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
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun playAlarmSound() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(applicationContext, uri)
                isLooping = false
                prepare()
                start()
                setOnCompletionListener { release() }
            }
        } catch (e: Exception) {
            // フォールバック: システム通知音
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(applicationContext, uri)?.play()
            } catch (_: Exception) {}
        }
    }
}
