package com.example.pomodoro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            // ── 通知・アラート ─────────────────────────────
            SectionHeader("通知・アラート")
            SettingsToggleItem(Icons.Default.Notifications, "プッシュ通知", "セッション終了を通知", notificationEnabled, onNotifToggle)
            SettingsToggleItem(Icons.AutoMirrored.Filled.VolumeUp, "サウンド", "アラーム音を有効化", soundEnabled, onSoundToggle)
            SettingsToggleItem(Icons.Default.Vibration, "バイブレーション", "終了時に振動", vibrationEnabled, onVibrationToggle)

            Spacer(Modifier.height(16.dp))

            // ── カラーテーマ ───────────────────────────────
            SectionHeader("ビジュアル")
            ThemeSelector(
                appThemeName = appThemeName,
                customBg = customBg,
                customText = customText,
                customAccent = customAccent,
                onThemeChange = onThemeChange,
                onCustomBgChange = onCustomBgChange,
                onCustomTextChange = onCustomTextChange,
                onCustomAccentChange = onCustomAccentChange
            )

            Spacer(Modifier.height(24.dp))

            // ── アプリ情報 ─────────────────────────────────
            SectionHeader("情報")
            InfoItem(Icons.Default.Info, "バージョン", "1.3.0")
            InfoItem(Icons.Default.Code, "開発情報", "Jetpack Compose + Material3")

            Spacer(Modifier.height(16.dp))

            // ── クレジット ─────────────────────────────────
            SectionHeader("クレジット")
            CreditItem(Icons.Default.Person, "作者", "github.com/warasugitewara", "https://github.com/warasugitewara")

            Spacer(Modifier.height(32.dp))

            Text(
                "Pomotimer for Creators",
                modifier = Modifier.fillMaxWidth().alpha(0.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp),
        letterSpacing = 1.sp
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(description, fontSize = 12.sp) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
private fun InfoItem(icon: ImageVector, title: String, value: String) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(value, fontSize = 12.sp) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    )
}

@Composable
private fun CreditItem(icon: ImageVector, title: String, displayUrl: String, url: String) {
    val uriHandler = LocalUriHandler.current
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Text(displayUrl, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier.clickable { uriHandler.openUri(url) }
    )
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

    Column {
        ListItem(
            headlineContent = { Text("テーマ選択", fontWeight = FontWeight.Medium) },
            supportingContent = { Text(currentTheme.displayName) },
            leadingContent = { Icon(Icons.Default.Palette, null) },
            trailingContent = {
                Box {
                    IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        AppTheme.entries.forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(theme.displayName) },
                                onClick = { onThemeChange(theme.name); expanded = false }
                            )
                        }
                    }
                }
            }
        )

        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeColorCircle(currentTheme, customBg, customText, customAccent)
        }

        if (currentTheme == AppTheme.CUSTOM) {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ColorCodeInput("背景色", customBg, onCustomBgChange)
                    ColorCodeInput("テキスト", customText, onCustomTextChange)
                    ColorCodeInput("アクセント", customAccent, onCustomAccentChange)
                }
            }
        }
    }
}

@Composable
private fun ThemeColorCircle(
    theme: AppTheme, customBg: String, customText: String, customAccent: String
) {
    val samples: List<Color> = when (theme) {
        AppTheme.LIGHT           -> listOf(Color(0xFFFAFAFA), Color(0xFF212121), Color(0xFFD32F2F))
        AppTheme.DARK            -> listOf(Color(0xFF121212), Color(0xFFEEEEEE), Color(0xFFEF9A9A))
        AppTheme.SOLARIZED_LIGHT -> listOf(Color(0xFFFDF6E3), Color(0xFF657B83), Color(0xFF268BD2))
        AppTheme.SOLARIZED_DARK  -> listOf(Color(0xFF002B36), Color(0xFF839496), Color(0xFF268BD2))
        AppTheme.MONOKAI         -> listOf(Color(0xFF272822), Color(0xFFF8F8F2), Color(0xFFA6E22E))
        AppTheme.NORD            -> listOf(Color(0xFF2E3440), Color(0xFFECEFF4), Color(0xFF88C0D0))
        AppTheme.CUSTOM          -> listOf(
            parseHexColor(customBg, Color.White),
            parseHexColor(customText, Color.Black),
            parseHexColor(customAccent, Color(0xFFD32F2F))
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        samples.forEach { color ->
            Surface(modifier = Modifier.size(24.dp), color = color, shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))) {}
        }
    }
}

@Composable
private fun ColorCodeInput(label: String, value: String, onApply: (String) -> Unit) {
    var draft by remember(value) { mutableStateOf(value) }
    val isValid = draft.matches(Regex("#[0-9A-Fa-f]{6}"))
    val previewColor = if (isValid) parseHexColor(draft, Color.Gray) else Color.Gray

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(modifier = Modifier.size(32.dp), color = previewColor, shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))) {}
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it; if (it.matches(Regex("#[0-9A-Fa-f]{6}"))) onApply(it) },
            label = { Text(label) },
            placeholder = { Text("#RRGGBB") },
            isError = draft.isNotEmpty() && !isValid,
            singleLine = true,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodySmall
        )
    }
}

