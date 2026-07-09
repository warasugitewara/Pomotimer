package com.example.pomodoro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.ui.theme.AppTheme
import com.example.pomodoro.ui.theme.parseHexColor
import com.example.pomodoro.util.ConnectionTestResult
import com.example.pomodoro.util.DiscordRpcReporter
import kotlinx.coroutines.launch

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
    discordRpcEnabled: Boolean,
    discordBridgeHost: String,
    discordBridgePort: String,
    discordBridgeHttps: Boolean,
    discordBridgeToken: String,
    connectionTestResult: ConnectionTestResult?,
    autoStartBreak: Boolean,
    autoStartWork: Boolean,
    onNotifToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onThemeChange: (String) -> Unit,
    onCustomBgChange: (String) -> Unit,
    onCustomTextChange: (String) -> Unit,
    onCustomAccentChange: (String) -> Unit,
    onDiscordRpcToggle: (Boolean) -> Unit,
    onDiscordBridgeHostChange: (String) -> Unit,
    onDiscordBridgePortChange: (String) -> Unit,
    onDiscordBridgeHttpsToggle: (Boolean) -> Unit,
    onDiscordBridgeTokenChange: (String) -> Unit,
    onTestDiscordConnection: (String, String, Boolean, String) -> Unit,
    onAutoStartBreakToggle: (Boolean) -> Unit,
    onAutoStartWorkToggle: (Boolean) -> Unit
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

            // ── サイクル ───────────────────────────────────
            SectionHeader("サイクル")
            SettingsToggleItem(
                Icons.Default.PlayCircle,
                "休憩を自動開始",
                "作業終了後、操作なしで休憩を開始",
                autoStartBreak,
                onAutoStartBreakToggle
            )
            SettingsToggleItem(
                Icons.Default.PlayCircle,
                "作業を自動開始",
                "休憩終了後、操作なしで作業を開始",
                autoStartWork,
                onAutoStartWorkToggle
            )

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

            // ── Discord RPC連携 ──────────────────────────────
            SectionHeader("Discord RPC連携")
            SettingsToggleItem(
                Icons.Default.SmartToy,
                "Waras-discordRPCと連携",
                "タイマー状態をDiscordのステータスに表示",
                discordRpcEnabled,
                onDiscordRpcToggle
            )
            if (discordRpcEnabled) {
                DiscordRpcConfig(
                    host = discordBridgeHost,
                    port = discordBridgePort,
                    useHttps = discordBridgeHttps,
                    token = discordBridgeToken,
                    connectionTestResult = connectionTestResult,
                    onHostChange = onDiscordBridgeHostChange,
                    onPortChange = onDiscordBridgePortChange,
                    onHttpsToggle = onDiscordBridgeHttpsToggle,
                    onTokenChange = onDiscordBridgeTokenChange,
                    onTestConnection = onTestDiscordConnection
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── アプリ情報 ─────────────────────────────────
            SectionHeader("情報")
            InfoItem(Icons.Default.Info, "バージョン", com.example.pomodoro.BuildConfig.VERSION_NAME)
            InfoItem(Icons.Default.Code, "開発情報", "Jetpack Compose + Material3")

            Spacer(Modifier.height(16.dp))

            // ── クレジット（情報ハブ） ─────────────────────
            SectionHeader("クレジット")
            CreditItem(Icons.Default.Code, "Repository", "GitHub - Pomotimer", "https://github.com/warasugitewara/Pomotimer")
            CreditItem(Icons.Default.Person, "Developer", ".warasugi", "https://github.com/warasugitewara")
            CreditItem(Icons.Default.Gavel, "License", "MIT License", "https://github.com/warasugitewara/Pomotimer/blob/main/LICENSE")

            Spacer(Modifier.height(16.dp))

            SectionHeader("OSS Credits")
            InfoItem(Icons.Default.Storage, "Room", "Apache License 2.0")
            InfoItem(Icons.Default.Save, "DataStore", "Apache License 2.0")
            InfoItem(Icons.Default.SwapHoriz, "Navigation Compose", "Apache License 2.0")
            InfoItem(Icons.Default.BarChart, "Vico", "Apache License 2.0")
            InfoItem(Icons.Default.TextFields, "JetBrains Mono", "SIL Open Font License 1.1")

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
        AppTheme.DISCORD         -> listOf(Color(0xFF313338), Color(0xFFDBDEE1), Color(0xFF5865F2))
        AppTheme.BTOP            -> listOf(Color(0xFF0D0D0D), Color(0xFFCCCCCC), Color(0xFF00FF41))
        AppTheme.CATPPUCCIN      -> listOf(Color(0xFF1E1E2E), Color(0xFFCDD6F4), Color(0xFFCBA6F7))
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
private fun DiscordRpcConfig(
    host: String,
    port: String,
    useHttps: Boolean,
    token: String,
    connectionTestResult: ConnectionTestResult?,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onHttpsToggle: (Boolean) -> Unit,
    onTokenChange: (String) -> Unit,
    onTestConnection: (String, String, Boolean, String) -> Unit
) {
    var hostDraft by remember(host) { mutableStateOf(host) }
    var portDraft by remember(port) { mutableStateOf(port) }
    var tokenDraft by remember(token) { mutableStateOf(token) }
    var tokenVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = hostDraft,
                onValueChange = { hostDraft = it; onHostChange(it) },
                label = { Text("IPアドレス / ホスト名") },
                placeholder = { Text("例: 192.168.1.10") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.weight(1.6f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = portDraft,
                onValueChange = { v -> if (v.all { it.isDigit() } && v.length <= 5) { portDraft = v; onPortChange(v) } },
                label = { Text("ポート") },
                placeholder = { Text("8765") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("HTTPS", style = MaterialTheme.typography.bodySmall)
            Switch(checked = useHttps, onCheckedChange = onHttpsToggle)
            Text(
                text = "自宅LAN/Twingateのみで使うなら通常OFF",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val previewUrl = remember(hostDraft, portDraft, useHttps) {
            DiscordRpcReporter.buildBridgeUrl(hostDraft, portDraft, useHttps)
        }
        Text(
            text = "接続先: ${previewUrl ?: "IPアドレスを入力してください"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = tokenDraft,
            onValueChange = { tokenDraft = it; onTokenChange(it) },
            label = { Text("Token") },
            singleLine = true,
            visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { tokenVisible = !tokenVisible }) {
                    Icon(
                        if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (tokenVisible) "非表示" else "表示"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { onTestConnection(hostDraft, portDraft, useHttps, tokenDraft) },
                enabled = hostDraft.isNotBlank() && tokenDraft.isNotBlank()
            ) { Text("接続テスト") }

            when (connectionTestResult) {
                ConnectionTestResult.SUCCESS ->
                    Text("✅ 接続成功", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                ConnectionTestResult.AUTH_FAILED ->
                    Text("❌ 認証失敗（Tokenを確認）", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                ConnectionTestResult.UNREACHABLE ->
                    Text("❌ 到達できません（VPN/IP/ポート/PC側ファイアウォールを確認）", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                null -> {}
            }
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

