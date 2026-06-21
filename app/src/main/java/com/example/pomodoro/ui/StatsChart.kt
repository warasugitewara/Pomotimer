package com.example.pomodoro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import com.example.pomodoro.data.DailyStat
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 日別ポモドーロ数の縦棒グラフ。色は compose-m3 テーマ（= 現在のMaterial3カラー）に追従する。
 * 横軸ラベルは "M/d"。
 */
@Composable
fun PomodoroBarChart(stats: List<DailyStat>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val labels = remember(stats) { stats.map { shortDate(it.date) } }

    LaunchedEffect(stats) {
        if (stats.isEmpty()) return@LaunchedEffect
        modelProducer.runTransaction {
            columnSeries { series(stats.map { it.pomodoros }) }
        }
    }

    val bottomFormatter = remember(labels) {
        CartesianValueFormatter { _, value, _ ->
            labels.getOrNull(value.toInt()).orEmpty()
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomFormatter),
            ),
            modelProducer = modelProducer,
            modifier = modifier.fillMaxWidth().height(200.dp),
        )
    }
}

private fun shortDate(dateKey: String): String = try {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)!!
    SimpleDateFormat("M/d", Locale.getDefault()).format(parsed)
} catch (e: Exception) { dateKey }
