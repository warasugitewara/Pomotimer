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
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

private val LabelListKey = ExtraStore.Key<List<String>>()

/**
 * 日別ポモドーロ数の縦棒グラフ。色は compose-m3 テーマ（= 現在のMaterial3カラー）に追従する。
 * 横軸ラベルは "M/d"。
 */
@Composable
fun PomodoroBarChart(stats: List<DailyStat>, modifier: Modifier = Modifier) {
    if (stats.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(stats) {
        val labels = stats.map { shortDate(it.date) }
        modelProducer.runTransaction {
            columnSeries { series(stats.map { it.pomodoros }) }
            // 横軸ラベルは日別/曜日別・7日/30日でデータ点数が変わるうえログ削除で
            // 疎な日付集計の行数も変わる。remoteな Compose state で渡すとモデル更新
            // （非同期・新旧並行処理されうる）とラベルが食い違いクラッシュしていたため、
            // 同一トランザクションの extras に載せてモデルと必ず同期させる。
            extras { it[LabelListKey] = labels }
        }
    }

    val bottomFormatter = remember {
        CartesianValueFormatter { context, value, _ ->
            context.model.extraStore.getOrNull(LabelListKey)?.getOrNull(value.toInt()).orEmpty()
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
            // 7↔30日・日別↔曜日別を切り替えると棒の本数（データ点数）が変わる。
            // 既定の差分アニメーションは旧モデルと新モデルを補間するため、点数や
            // x ドメインが変わるとその補間中にクラッシュしていた。animationSpec=null で
            // 補間を無効化し、更新時は常に最終モデルへ直接切り替える（初回表示と同じ経路）。
            animationSpec = null,
            modifier = modifier.fillMaxWidth().height(200.dp),
        )
    }
}

private fun shortDate(dateKey: String): String = try {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)!!
    SimpleDateFormat("M/d", Locale.getDefault()).format(parsed)
} catch (e: Exception) { dateKey }
