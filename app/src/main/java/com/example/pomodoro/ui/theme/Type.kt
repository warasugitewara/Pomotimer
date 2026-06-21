package com.example.pomodoro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.pomodoro.R

/**
 * 等幅フォント（JetBrains Mono）。
 * 「データはmono、散文はsans」の方針で、タイマーの桁・統計の数値・グラフ軸など
 * 数値表現に用いる。本文・ラベルは可読性重視でシステムsansのまま。
 */
val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium,  FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold,    FontWeight.Bold),
)

/**
 * カウントダウンの大型桁。等幅なので秒更新でも横揺れしない。
 */
val TimerDigitStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Bold,
    fontSize = 68.sp,
    letterSpacing = (-1).sp,
)

/**
 * 統計値・グラフ数値など、本文中で数字を強調する用途。
 */
val MonoNumberStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
)

/**
 * Material3 Typography。
 * 数値系の見出し（display）に mono を割り当て、本文・ラベルは既定sansを維持する。
 */
val AppTypography: Typography = Typography().run {
    copy(
        displayLarge  = displayLarge.copy(fontFamily = JetBrainsMono),
        displayMedium = displayMedium.copy(fontFamily = JetBrainsMono),
        displaySmall  = displaySmall.copy(fontFamily = JetBrainsMono),
    )
}
