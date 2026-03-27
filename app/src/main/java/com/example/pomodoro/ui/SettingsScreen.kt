package com.example.pomodoro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.ui.theme.AppTheme
import com.example.pomodoro.ui.theme.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    notificationEnabled: Boolean,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    appThemeName: String,
    customBg: String,
    customText: String,
    customAccent: String,
    onNotifToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onThemeChange: (String) -> Unit,
    onCustomBgChange: (String) -> Unit,
    onCustomTextChange: (String) -> Unit,
    onCustomAccentChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("設定", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp))

        // ── 通知・アラート ─────────────────────────────
        SectionHeader("通知・アラート")

        SettingsToggleItem(Icons.Default.Notifications, "プッシュ通知",
            "作業・休憩の終了時に通知する", notificationEnabled, onNotifToggle)
        SettingsToggleItem(Icons.AutoMirrored.Filled.VolumeUp, "通知音",
            "タイマー終了時にアラーム音を鳴らす", soundEnabled, onSoundToggle)
        SettingsToggleItem(Icons.Default.PhoneAndroid, "バイブレーション",
            "タイマー終了時に振動する", vibrationEnabled, onVibrationToggle)

        Spacer(Modifier.height(8.dp))

        // ── カラーテーマ ───────────────────────────────
        SectionHeader("カラーテーマ")

        ThemeSelector(
            appThemeName      = appThemeName,
            customBg          = customBg,
            customText        = customText,
            customAccent      = customAccent,
            onThemeChange     = onThemeChange,
            onCustomBgChange  = onCustomBgChange,
            onCustomTextChange= onCustomTextChange,
            onCustomAccentChange = onCustomAccentChange
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(4.dp))

        // ── アプリ情報 ─────────────────────────────────
        SectionHeader("アプリについて")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Pomotimer", fontWeight = FontWeight.Bold)
                Text("バージョン 1.2.0", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("ポモドーロ・テクニックに基づいた集中管理タイマーアプリです。",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
}

@Composable
private fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(description, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(
    appThemeName: String,
    customBg: String, customText: String, customAccent: String,
    onThemeChange: (String) -> Unit,
    onCustomBgChange: (String) -> Unit,
    onCustomTextChange: (String) -> Unit,
    onCustomAccentChange: (String) -> Unit
) {
    val currentTheme = AppTheme.entries.find { it.name == appThemeName } ?: AppTheme.LIGHT
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 16.dp))
                Text("テーマ", fontWeight = FontWeight.Medium)
            }

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = currentTheme.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("カラーテーマ") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    AppTheme.entries.forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme.displayName) },
                            onClick = { onThemeChange(theme.name); expanded = false }
                        )
                    }
                }
            }

            // テーマプレビュー（色のサンプル行）
            ThemePreviewBar(currentTheme, customBg, customText, customAccent)

            // カスタムテーマの色設定
            if (currentTheme == AppTheme.CUSTOM) {
                HorizontalDivider()
                Text("カスタムカラー (例: #FF5722)",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ColorCodeInput("背景色",      customBg,     onCustomBgChange)
                ColorCodeInput("テキスト色",  customText,   onCustomTextChange)
                ColorCodeInput("アクセント色", customAccent, onCustomAccentChange)
            }
        }
    }
}

@Composable
private fun ThemePreviewBar(
    theme: AppTheme, customBg: String, customText: String, customAccent: String
) {
    val samples: List<Color> = when (theme) {
        AppTheme.LIGHT           -> listOf(Color(0xFFFAFAFA), Color(0xFF212121), Color(0xFFD32F2F), Color(0xFF388E3C))
        AppTheme.DARK            -> listOf(Color(0xFF121212), Color(0xFFEEEEEE), Color(0xFFEF9A9A), Color(0xFF81C784))
        AppTheme.SOLARIZED_LIGHT -> listOf(Color(0xFFFDF6E3), Color(0xFF657B83), Color(0xFF268BD2), Color(0xFF2AA198))
        AppTheme.SOLARIZED_DARK  -> listOf(Color(0xFF002B36), Color(0xFF839496), Color(0xFF268BD2), Color(0xFF2AA198))
        AppTheme.MONOKAI         -> listOf(Color(0xFF272822), Color(0xFFF8F8F2), Color(0xFFA6E22E), Color(0xFFFD971F))
        AppTheme.NORD            -> listOf(Color(0xFF2E3440), Color(0xFFECEFF4), Color(0xFF88C0D0), Color(0xFFBF616A))
        AppTheme.CUSTOM          -> listOf(
            parseHexColor(customBg, Color.White),
            parseHexColor(customText, Color.Black),
            parseHexColor(customAccent, Color(0xFFD32F2F)),
            Color.Gray
        )
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        samples.forEach { color ->
            Box(Modifier.size(32.dp).background(color, CircleShape))
        }
        Spacer(Modifier.weight(1f))
        Text("背景・テキスト・アクセント", fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
private fun ColorCodeInput(label: String, value: String, onApply: (String) -> Unit) {
    var draft by remember(value) { mutableStateOf(value) }
    val isValid = draft.matches(Regex("#[0-9A-Fa-f]{6}"))
    val previewColor = if (isValid) parseHexColor(draft, Color.Gray) else Color.Gray

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.size(24.dp).background(previewColor, CircleShape))
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it; if (it.matches(Regex("#[0-9A-Fa-f]{6}"))) onApply(it) },
            label = { Text(label, fontSize = 11.sp) },
            placeholder = { Text("#RRGGBB") },
            isError = draft.isNotEmpty() && !isValid,
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
}
