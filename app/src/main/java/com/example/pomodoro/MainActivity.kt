package com.example.pomodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.pomodoro.ui.PomotimerApp
import com.example.pomodoro.viewmodel.TimerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            PomotimerApp(
                uiState = uiState,
                onStart = { viewModel.startTimer() },
                onPause = { viewModel.pauseTimer() },
                onReset = { viewModel.resetTimer() },
                onSetWorkDuration = { viewModel.setWorkDuration(it) },
                onSetBreakDuration = { viewModel.setBreakDuration(it) }
            )
        }
    }
}
