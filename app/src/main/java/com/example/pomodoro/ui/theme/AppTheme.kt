package com.example.pomodoro.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String) {
    LIGHT("ライト"),
    DARK("ダーク"),
    SOLARIZED_LIGHT("Solarized ライト"),
    SOLARIZED_DARK("Solarized ダーク"),
    MONOKAI("Monokai"),
    NORD("Nord"),
    CUSTOM("カスタム")
}

// ── Standard Light ────────────────────────────────────
private val LightScheme = lightColorScheme(
    primary            = Color(0xFFD32F2F),
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFFFCDD2),
    onPrimaryContainer = Color(0xFF7F0000),
    secondary          = Color(0xFF388E3C),
    onSecondary        = Color.White,
    background         = Color(0xFFFAFAFA),
    onBackground       = Color(0xFF212121),
    surface            = Color(0xFFFFFFFF),
    onSurface          = Color(0xFF212121),
    surfaceVariant     = Color(0xFFF5F5F5),
    onSurfaceVariant   = Color(0xFF757575),
    error              = Color(0xFFB71C1C),
)

// ── Standard Dark ─────────────────────────────────────
private val DarkScheme = darkColorScheme(
    primary            = Color(0xFFEF9A9A),
    onPrimary          = Color(0xFF7F0000),
    primaryContainer   = Color(0xFF4E0000),
    onPrimaryContainer = Color(0xFFFFCDD2),
    secondary          = Color(0xFF81C784),
    onSecondary        = Color(0xFF1B5E20),
    background         = Color(0xFF121212),
    onBackground       = Color(0xFFEEEEEE),
    surface            = Color(0xFF1E1E1E),
    onSurface          = Color(0xFFEEEEEE),
    surfaceVariant     = Color(0xFF2C2C2C),
    onSurfaceVariant   = Color(0xFFBDBDBD),
    error              = Color(0xFFCF6679),
)

// ── Solarized Light ───────────────────────────────────
private val SolarizedLightScheme = lightColorScheme(
    primary            = Color(0xFF268BD2),
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFD4E8F7),
    onPrimaryContainer = Color(0xFF003B6A),
    secondary          = Color(0xFF2AA198),
    onSecondary        = Color.White,
    tertiary           = Color(0xFFCB4B16),
    background         = Color(0xFFFDF6E3),
    onBackground       = Color(0xFF657B83),
    surface            = Color(0xFFEEE8D5),
    onSurface          = Color(0xFF586E75),
    surfaceVariant     = Color(0xFFEEE8D5),
    onSurfaceVariant   = Color(0xFF93A1A1),
    error              = Color(0xFFDC322F),
    outline            = Color(0xFF93A1A1),
)

// ── Solarized Dark ────────────────────────────────────
private val SolarizedDarkScheme = darkColorScheme(
    primary            = Color(0xFF268BD2),
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFF003B6A),
    onPrimaryContainer = Color(0xFFD4E8F7),
    secondary          = Color(0xFF2AA198),
    onSecondary        = Color.White,
    tertiary           = Color(0xFFCB4B16),
    background         = Color(0xFF002B36),
    onBackground       = Color(0xFF839496),
    surface            = Color(0xFF073642),
    onSurface          = Color(0xFF93A1A1),
    surfaceVariant     = Color(0xFF073642),
    onSurfaceVariant   = Color(0xFF657B83),
    error              = Color(0xFFDC322F),
    outline            = Color(0xFF586E75),
)

// ── Monokai ───────────────────────────────────────────
private val MonokaiScheme = darkColorScheme(
    primary            = Color(0xFFA6E22E),
    onPrimary          = Color(0xFF272822),
    primaryContainer   = Color(0xFF3E3D32),
    onPrimaryContainer = Color(0xFFA6E22E),
    secondary          = Color(0xFFFD971F),
    onSecondary        = Color(0xFF272822),
    tertiary           = Color(0xFF66D9E8),
    background         = Color(0xFF272822),
    onBackground       = Color(0xFFF8F8F2),
    surface            = Color(0xFF3E3D32),
    onSurface          = Color(0xFFF8F8F2),
    surfaceVariant     = Color(0xFF49483E),
    onSurfaceVariant   = Color(0xFF75715E),
    error              = Color(0xFFF92672),
    outline            = Color(0xFF75715E),
)

// ── Nord ──────────────────────────────────────────────
private val NordScheme = darkColorScheme(
    primary            = Color(0xFF88C0D0),
    onPrimary          = Color(0xFF2E3440),
    primaryContainer   = Color(0xFF4C566A),
    onPrimaryContainer = Color(0xFF88C0D0),
    secondary          = Color(0xFF81A1C1),
    onSecondary        = Color(0xFF2E3440),
    tertiary           = Color(0xFFBF616A),
    background         = Color(0xFF2E3440),
    onBackground       = Color(0xFFECEFF4),
    surface            = Color(0xFF3B4252),
    onSurface          = Color(0xFFE5E9F0),
    surfaceVariant     = Color(0xFF434C5E),
    onSurfaceVariant   = Color(0xFFD8DEE9),
    error              = Color(0xFFBF616A),
    outline            = Color(0xFF4C566A),
)

// ── Custom ────────────────────────────────────────────
fun parseHexColor(hex: String, default: Color = Color.Gray): Color = try {
    val clean = hex.trim().let { if (it.startsWith("#")) it else "#$it" }
    Color(android.graphics.Color.parseColor(clean))
} catch (e: Exception) { default }

private fun buildCustomScheme(bg: String, text: String, accent: String): ColorScheme {
    val bgColor     = parseHexColor(bg,     Color.White)
    val textColor   = parseHexColor(text,   Color.Black)
    val accentColor = parseHexColor(accent, Color(0xFFD32F2F))
    val bgInt = try {
        android.graphics.Color.parseColor(bg.trim().let { if (it.startsWith("#")) it else "#$it" })
    } catch (e: Exception) { android.graphics.Color.WHITE }
    val luminance = (0.299 * android.graphics.Color.red(bgInt) +
                     0.587 * android.graphics.Color.green(bgInt) +
                     0.114 * android.graphics.Color.blue(bgInt)) / 255.0

    return if (luminance > 0.5) lightColorScheme(
        primary          = accentColor, onPrimary    = Color.White,
        secondary        = accentColor, onSecondary  = Color.White,
        background       = bgColor,     onBackground = textColor,
        surface          = bgColor,     onSurface    = textColor,
        surfaceVariant   = bgColor,     onSurfaceVariant = textColor.copy(alpha = 0.6f),
    ) else darkColorScheme(
        primary          = accentColor, onPrimary    = bgColor,
        secondary        = accentColor, onSecondary  = bgColor,
        background       = bgColor,     onBackground = textColor,
        surface          = bgColor,     onSurface    = textColor,
        surfaceVariant   = bgColor,     onSurfaceVariant = textColor.copy(alpha = 0.6f),
    )
}

@Composable
fun PomotimerTheme(
    theme: AppTheme = AppTheme.LIGHT,
    customBg: String     = "#FAFAFA",
    customText: String   = "#212121",
    customAccent: String = "#E53935",
    content: @Composable () -> Unit
) {
    val scheme = when (theme) {
        AppTheme.LIGHT           -> LightScheme
        AppTheme.DARK            -> DarkScheme
        AppTheme.SOLARIZED_LIGHT -> SolarizedLightScheme
        AppTheme.SOLARIZED_DARK  -> SolarizedDarkScheme
        AppTheme.MONOKAI         -> MonokaiScheme
        AppTheme.NORD            -> NordScheme
        AppTheme.CUSTOM          -> buildCustomScheme(customBg, customText, customAccent)
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
