package com.bluemeanie.axonscanner.presentation.ui.screens.gear

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bluemeanie.axonscanner.domain.model.*
import com.bluemeanie.axonscanner.presentation.ui.screens.radar.BottomNavigationBar
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieTheme
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieColors
import com.bluemeanie.axonscanner.presentation.viewmodel.FontType
import com.bluemeanie.axonscanner.presentation.viewmodel.SettingsViewModel
import com.bluemeanie.axonscanner.presentation.viewmodel.TelegramStatus
import com.bluemeanie.axonscanner.util.AudioEngine
import com.bluemeanie.axonscanner.util.VibrationPattern
import com.bluemeanie.axonscanner.util.ThemeEngine

@Composable
fun GearScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToRadar: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToHeatmap: () -> Unit,
    onNavigateToIntel: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val callsign by viewModel.callsign.collectAsState()
    val deviceCount by viewModel.deviceCount.collectAsState()
    val axonCount by viewModel.axonCount.collectAsState()
    val telegramStatus by viewModel.telegramStatus.collectAsState()
    val colors = BlueMeanieTheme.colors

    var expandedSection by remember { mutableStateOf<String?>(null) }
    var telegramToken by remember { mutableStateOf(settings.telegramBotToken ?: "") }
    var telegramChatId by remember { mutableStateOf(settings.telegramChatId ?: "") }
    var ghostProtocolVisible by remember { mutableStateOf(false) }
    var ghostTapCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    ghostTapCount++
                    if (ghostTapCount >= 5) {
                        ghostProtocolVisible = true
                        ghostTapCount = 0
                    }
                }
        ) {
            Text(
                text = "SYSTEM GEAR",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            // Hidden easter egg - BM23 binary
            Text(
                text = "01000010 01001101 00110010 00110011",
                fontSize = 1.sp,
                color = colors.textMuted.copy(alpha = 0.05f),
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        // Ghost Protocol Easter Egg Dialog
        if (ghostProtocolVisible) {
            AlertDialog(
                onDismissRequest = { ghostProtocolVisible = false },
                title = {
                    Text("👻 GHOST PROTOCOL", color = colors.primary)
                },
                text = {
                    Column {
                        Text("Secret activated!", color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "bluemeanie23 has been spotted...",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { ghostProtocolVisible = false }) {
                        Text("DISMISS", color = colors.primary)
                    }
                }
            )
        }

        // CallSign Edit
        SettingsSection(
            title = "CALLSIGN",
            isExpanded = expandedSection == "callsign",
            onToggle = { expandedSection = if (expandedSection == "callsign") null else "callsign" },
            colors = colors
        ) {
            var newCallsign by remember { mutableStateOf(callsign) }
            OutlinedTextField(
                value = newCallsign,
                onValueChange = { newCallsign = it.uppercase() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Callsign", color = colors.textMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.primary
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.updateCallsign(newCallsign) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.textPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("UPDATE CALLSIGN")
            }
        }

        // Scan Mode
        SettingsSection(
            title = "SCAN MODE",
            isExpanded = expandedSection == "scanmode",
            onToggle = { expandedSection = if (expandedSection == "scanmode") null else "scanmode" },
            colors = colors
        ) {
            ScanMode.entries.forEach { mode ->
                RadioOption(
                    label = mode.name,
                    description = when (mode) {
                        ScanMode.TACTICAL -> "Low latency, high battery"
                        ScanMode.STEALTH -> "Low power, periodic scanning"
                        ScanMode.SWEEP -> "Interval burst, balanced"
                    },
                    isSelected = settings.scanMode == mode,
                    onClick = { viewModel.updateScanMode(mode) },
                    colors = colors
                )
            }
        }

        // Theme Engine
        SettingsSection(
            title = "THEME ENGINE",
            isExpanded = expandedSection == "theme",
            onToggle = { expandedSection = if (expandedSection == "theme") null else "theme" },
            colors = colors
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ThemeType.entries) { theme ->
                    ThemeOption(
                        theme = theme,
                        isSelected = settings.theme == theme,
                        onClick = { viewModel.updateTheme(theme) },
                        colors = colors
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${ThemeEngine.themes[settings.theme.name]?.name ?: "Classic"} THEME ACTIVE",
                fontSize = 12.sp,
                color = colors.primary
            )
        }

        // Background Engine
        SettingsSection(
            title = "BACKGROUND ENGINE",
            isExpanded = expandedSection == "background",
            onToggle = { expandedSection = if (expandedSection == "background") null else "background" },
            colors = colors
        ) {
            BackgroundType.entries.forEach { bg ->
                RadioOption(
                    label = bg.name.replace("_", " "),
                    description = when (bg) {
                        BackgroundType.NEURAL -> "Connected nodes with collision physics"
                        BackgroundType.PARTICLE -> "Drifting particles with parallax"
                        BackgroundType.MATRIX -> "Falling character streams"
                        BackgroundType.AURORA -> "Flowing gradient waves"
                        BackgroundType.GRID -> "Minimal geometric grid"
                    },
                    isSelected = settings.background == bg,
                    onClick = { viewModel.updateBackground(bg) },
                    colors = colors
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("OPACITY: ${(settings.backgroundOpacity * 100).toInt()}%", color = colors.textMuted)
            Slider(
                value = settings.backgroundOpacity,
                onValueChange = { viewModel.updateBackgroundOpacity(it) },
                valueRange = 0.2f..0.8f,
                colors = SliderDefaults.colors(
                    thumbColor = colors.primary,
                    activeTrackColor = colors.primary
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PERFORMANCE MODE", fontSize = 12.sp, color = colors.textSecondary)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.performanceMode,
                    onCheckedChange = { viewModel.updatePerformanceMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.primary,
                        checkedTrackColor = colors.primary.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // Font Engine
        SettingsSection(
            title = "FONT ENGINE",
            isExpanded = expandedSection == "font",
            onToggle = { expandedSection = if (expandedSection == "font") null else "font" },
            colors = colors
        ) {
            FontType.entries.forEach { fontType ->
                FontSection(
                    fontType = fontType,
                    currentFamily = when (fontType) {
                        FontType.RADAR -> settings.fontRadarFamily
                        FontType.RESULTS -> settings.fontResultsFamily
                        FontType.MENU -> settings.fontMenuFamily
                        FontType.SETTINGS -> settings.fontSettingsFamily
                        FontType.CONSOLE -> settings.fontConsoleFamily
                        FontType.GAUGE -> settings.fontGaugeFamily
                    },
                    currentSize = when (fontType) {
                        FontType.RADAR -> settings.fontRadarSize
                        FontType.RESULTS -> settings.fontResultsSize
                        FontType.MENU -> settings.fontMenuSize
                        FontType.SETTINGS -> settings.fontSettingsSize
                        FontType.CONSOLE -> settings.fontConsoleSize
                        FontType.GAUGE -> settings.fontGaugeSize
                    },
                    onUpdate = { family, size -> viewModel.updateFontSettings(fontType, family, size) },
                    colors = colors
                )
                Divider(color = colors.border, modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        // Radar Engine
        SettingsSection(
            title = "RADAR ENGINE",
            isExpanded = expandedSection == "radar",
            onToggle = { expandedSection = if (expandedSection == "radar") null else "radar" },
            colors = colors
        ) {
            RadarStyle.entries.forEach { style ->
                RadioOption(
                    label = style.name.replace("_", " "),
                    description = when (style) {
                        RadarStyle.TACTICAL -> "Rotating sweep arm with trail"
                        RadarStyle.SONAR -> "Expanding concentric ripple rings"
                        RadarStyle.PULSE -> "Single thick ring with taper"
                        RadarStyle.GRID -> "Crosshair lines scanning outward"
                        RadarStyle.ORBITAL -> "Dots orbiting in spiral paths"
                    },
                    isSelected = settings.radarStyle == style,
                    onClick = { viewModel.updateRadarStyle(style) },
                    colors = colors
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Scan Button Style
            Text("SCAN BUTTON STYLE", fontSize = 12.sp, color = colors.textMuted)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ScanButtonStyle.entries) { style ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (settings.scanButtonStyle == style) colors.primary.copy(alpha = 0.2f) else colors.surface)
                            .border(1.dp, if (settings.scanButtonStyle == style) colors.primary else colors.border, RoundedCornerShape(8.dp))
                            .clickable { viewModel.updateScanButtonStyle(style) }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = style.name,
                            fontSize = 12.sp,
                            color = if (settings.scanButtonStyle == style) colors.primary else colors.textMuted
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("BUTTON COLOR", fontSize = 12.sp, color = colors.textMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("#00F0FF", "#FF006E", "#34D399", "#F6D365", "#F87171").forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(color)))
                            .border(
                                2.dp,
                                if (settings.scanButtonColor == color) colors.textPrimary else Color.Transparent,
                                CircleShape
                            )
                            .clickable { viewModel.updateScanButtonColor(color) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("COUNTER SIZE", fontSize = 12.sp, color = colors.textMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CounterSize.entries.forEach { size ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (settings.counterSize == size) colors.primary.copy(alpha = 0.2f) else colors.surface)
                            .border(1.dp, if (settings.counterSize == size) colors.primary else colors.border, RoundedCornerShape(8.dp))
                            .clickable { viewModel.updateCounterSize(size) }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = size.name,
                            fontSize = 12.sp,
                            color = if (settings.counterSize == size) colors.primary else colors.textMuted
                        )
                    }
                }
            }
        }

        // Alert System
        SettingsSection(
            title = "ALERT SYSTEM",
            isExpanded = expandedSection == "alerts",
            onToggle = { expandedSection = if (expandedSection == "alerts") null else "alerts" },
            colors = colors
        ) {
            ToggleRow(
                label = "SOUND EFFECTS",
                isEnabled = settings.soundEnabled,
                onToggle = { viewModel.updateSoundEnabled(it) },
                colors = colors
            )
            if (settings.soundEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Play BLE PING */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.surface,
                            contentColor = colors.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("BLE PING", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { /* Play AXON ALERT */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.danger.copy(alpha = 0.2f),
                            contentColor = colors.danger
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("AXON ALERT", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { /* Play SCAN START */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.surface,
                            contentColor = colors.success
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SCAN START", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            ToggleRow(
                label = "HAPTIC VIBRATION",
                isEnabled = settings.vibrationEnabled,
                onToggle = { viewModel.updateVibrationEnabled(it) },
                colors = colors
            )
            if (settings.vibrationEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Test AXON pattern */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.danger.copy(alpha = 0.2f),
                            contentColor = colors.danger
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("AXON PATTERN", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { /* Test THREAT pattern */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.warning.copy(alpha = 0.2f),
                            contentColor = colors.warning
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("THREAT PATTERN", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { /* Test START pattern */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.surface,
                            contentColor = colors.success
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("START PATTERN", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            ToggleRow(
                label = "PUSH NOTIFICATIONS",
                isEnabled = settings.notificationsEnabled,
                onToggle = { viewModel.updateNotificationsEnabled(it) },
                colors = colors
            )

            Spacer(modifier = Modifier.height(8.dp))
            ToggleRow(
                label = "FULLSCREEN ALERT OVERLAY",
                isEnabled = settings.fullscreenAlertEnabled,
                onToggle = { viewModel.updateFullscreenAlertEnabled(it) },
                colors = colors
            )
        }

        // Telegram Output
        SettingsSection(
            title = "TELEGRAM OUTPUT",
            isExpanded = expandedSection == "telegram",
            onToggle = { expandedSection = if (expandedSection == "telegram") null else "telegram" },
            colors = colors
        ) {
            OutlinedTextField(
                value = telegramToken,
                onValueChange = { telegramToken = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bot Token", color = colors.textMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.primary
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = telegramChatId,
                onValueChange = { telegramChatId = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Chat ID", color = colors.textMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.primary
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (telegramStatus) {
                                    TelegramStatus.CONNECTED -> colors.success
                                    TelegramStatus.CONNECTING -> colors.warning
                                    else -> colors.danger
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = telegramStatus.name,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
                Button(
                    onClick = {
                        viewModel.updateTelegramConfig(
                            telegramToken.takeIf { it.isNotBlank() },
                            telegramChatId.takeIf { it.isNotBlank() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("LINK")
                }
            }
        }

        // Root Features
        SettingsSection(
            title = "ROOTED FEATURES",
            isExpanded = expandedSection == "root",
            onToggle = { expandedSection = if (expandedSection == "root") null else "root" },
            colors = colors
        ) {
            val rootFeatures = listOf(
                "RAW PACKET CAPTURE" to "Capture raw HCI packets",
                "MAC SPOOF" to "Spoof device MAC address",
                "TX POWER OVERRIDE" to "Override TX power level",
                "ALWAYS-ON SCAN" to "Continuous scanning mode",
                "DEEP PACKET DUMP" to "Full packet hex dump",
                "CHANNEL LOCK" to "Lock to specific BLE channel",
                "STEALTH RF" to "Reduced RF signature mode",
                "KERNEL MONITOR" to "Kernel-level HCI monitor",
                "HCI LOG EXPORT" to "Export HCI snoop logs",
                "AUTO GIT BACKUP" to "Auto commit scan data to git"
            )
            
            rootFeatures.forEach { (feature, description) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = description,
                            fontSize = 10.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }
            // Hidden easter egg - binary
            Text(
                text = "01000010 01001101 00110010 00110011",
                fontSize = 1.sp,
                color = colors.textMuted.copy(alpha = 0.03f)
            )
        }

        // Shizuku Module
        SettingsSection(
            title = "SHIZUKU MODULE",
            isExpanded = expandedSection == "shizuku",
            onToggle = { expandedSection = if (expandedSection == "shizuku") null else "shizuku" },
            colors = colors
        ) {
            val shizukuFeatures = listOf(
                "Wake Lock" to "Prevent device sleep during scans",
                "Background Scan" to "Continue scanning in background",
                "Privileged Permissions" to "Bypass permission prompts",
                "Screen-Off Scan" to "Scan while screen is off"
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Shizuku Status", color = colors.textPrimary)
                    Text("Not Connected", fontSize = 12.sp, color = colors.textMuted)
                }
                Switch(
                    checked = false,
                    onCheckedChange = { },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.primary,
                        checkedTrackColor = colors.primary.copy(alpha = 0.3f)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            shizukuFeatures.forEach { (feature, description) ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = feature, fontSize = 12.sp, color = colors.textSecondary)
                        Text(text = description, fontSize = 10.sp, color = colors.textMuted)
                    }
                }
            }
        }

        // Data Management
        SettingsSection(
            title = "DATA MANAGEMENT",
            isExpanded = expandedSection == "data",
            onToggle = { expandedSection = if (expandedSection == "data") null else "data" },
            colors = colors
        ) {
            Button(
                onClick = { viewModel.exportAllData() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surface,
                    contentColor = colors.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("EXPORT ALL DATA (.TXT)")
            }
            Text(
                text = "$deviceCount devices",
                fontSize = 10.sp,
                color = colors.textMuted,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.purgeSessionData() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.danger.copy(alpha = 0.2f),
                    contentColor = colors.danger
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PURGE SESSION DATA")
            }
        }

        // About
        SettingsSection(
            title = "ABOUT",
            isExpanded = expandedSection == "about",
            onToggle = { expandedSection = if (expandedSection == "about") null else "about" },
            colors = colors
        ) {
            AboutRow(label = "APP", value = "BlueMeanie", colors = colors)
            AboutRow(label = "VERSION", value = "3.0.0", colors = colors)
            AboutRow(label = "BUILD", value = "APEX", colors = colors)
            AboutRow(label = "ENGINE", value = "BLUETOOTHLESCANNER", colors = colors)
            Text(
                text = "NATIVE BLE",
                fontSize = 10.sp,
                color = colors.textMuted,
                modifier = Modifier.padding(start = 100.dp)
            )
            AboutRow(label = "DETECTION", value = "OUI + NAME + UUID MULTI-FACTOR", colors = colors)
            Spacer(modifier = Modifier.height(16.dp))
            // Hidden easter eggs
            Text(
                text = "01100010 01101100 01110101 01100101 01101101 01100101 01100001 01101110 01101001 01100101 00100011 00100011 00110011",
                fontSize = 1.sp,
                color = colors.textMuted.copy(alpha = 0.05f)
            )
            Text(
                text = "𓂀𓅓𓆓𓆣𓁹𓀀𓃀𓅱𓆑𓇋𓏏",
                fontSize = 1.sp,
                color = colors.textMuted.copy(alpha = 0.03f)
            )
            Text(
                text = "⠃⠇⠥⠑⠍⠑⠁⠝⠊⠑⠃⠆⠒",
                fontSize = 1.sp,
                color = colors.textMuted.copy(alpha = 0.03f)
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    colors: BlueMeanieColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .clickable(onClick = onToggle)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = colors.textMuted
            )
        }
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun RadioOption(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: BlueMeanieColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.primary,
                unselectedColor = colors.textMuted
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (isSelected) colors.primary else colors.textPrimary
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }
    }
}

@Composable
fun ThemeOption(
    theme: ThemeType,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: BlueMeanieColors
) {
    val themeColors = ThemeEngine.themes[theme.name]
    val primary = themeColors?.primary ?: colors.primary
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(primary)
                .border(
                    2.dp,
                    if (isSelected) colors.textPrimary else Color.Transparent,
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = theme.name,
            fontSize = 10.sp,
            color = if (isSelected) colors.primary else colors.textMuted
        )
    }
}

@Composable
fun FontSection(
    fontType: FontType,
    currentFamily: String,
    currentSize: Int,
    onUpdate: (String, Int) -> Unit,
    colors: BlueMeanieColors
) {
    val fontFamilies = listOf(
        "JetBrains Mono",
        "Fira Code",
        "Source Code Pro",
        "Space Mono",
        "Roboto Mono",
        "IBM Plex Mono",
        "Ubuntu Mono",
        "Cascadia Code"
    )

    Column {
        Text(
            text = "${fontType.name} FONTS",
            fontSize = 12.sp,
            color = colors.textMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Family selector
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(fontFamilies) { family ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (currentFamily == family) colors.primary.copy(alpha = 0.2f) else colors.surface)
                        .border(1.dp, if (currentFamily == family) colors.primary else colors.border, RoundedCornerShape(4.dp))
                        .clickable { onUpdate(family, currentSize) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = family.take(8),
                        fontSize = 10.sp,
                        color = if (currentFamily == family) colors.primary else colors.textMuted
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Size slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Size:", fontSize = 10.sp, color = colors.textMuted)
            Slider(
                value = currentSize.toFloat(),
                onValueChange = { onUpdate(currentFamily, it.toInt()) },
                valueRange = 8f..48f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = colors.primary,
                    activeTrackColor = colors.primary
                )
            )
            Text("${currentSize}sp", fontSize = 10.sp, color = colors.textPrimary)
        }
        
        // Live preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surface)
                .padding(8.dp)
        ) {
            Text(
                text = "Preview Text ABCabc123",
                fontSize = currentSize.sp,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
fun ToggleRow(
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    colors: BlueMeanieColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = colors.textPrimary
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                checkedTrackColor = colors.primary.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun AboutRow(
    label: String,
    value: String,
    colors: BlueMeanieColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.textMuted,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}
