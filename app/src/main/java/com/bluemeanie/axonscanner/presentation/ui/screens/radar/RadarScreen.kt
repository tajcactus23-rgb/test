package com.bluemeanie.axonscanner.presentation.ui.screens.radar

import android.Manifest
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bluemeanie.axonscanner.domain.model.*
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieTheme
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieColors
import com.bluemeanie.axonscanner.presentation.viewmodel.RadarViewModel
import com.bluemeanie.axonscanner.util.ThemeEngine
import kotlin.math.*

@Composable
fun RadarScreen(
    viewModel: RadarViewModel = hiltViewModel(),
    onNavigateToFeed: () -> Unit,
    onNavigateToHeatmap: () -> Unit,
    onNavigateToIntel: () -> Unit,
    onNavigateToGear: () -> Unit
) {
    val radarState by viewModel.radarState.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val callsign by viewModel.callsign.collectAsState()
    val showAxonAlert by viewModel.showAxonAlert.collectAsState()
    val colors = BlueMeanieTheme.colors

    var selectedDevice by remember { mutableStateOf<ScannedDevice?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Status HUD
            RadarHeader(
                callsign = callsign,
                isScanning = radarState.isScanning,
                settings = settings,
                colors = colors
            )

            // Radar Display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RadarDisplay(
                    devices = devices.filter { it.lastSeen > System.currentTimeMillis() - 30000 },
                    radarStyle = settings.radarStyle,
                    isScanning = radarState.isScanning,
                    colors = colors,
                    onDeviceClick = { selectedDevice = it }
                )

                // Status Indicators
                StatusIndicators(
                    isScanning = radarState.isScanning,
                    isBluetoothEnabled = viewModel.isBluetoothEnabled,
                    settings = settings,
                    colors = colors,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
            }

            // Stats Bar
            StatsBar(
                devicesFound = devices.size,
                axonHits = radarState.axonHits,
                scanDuration = radarState.scanDuration,
                peakRssi = radarState.peakRssi,
                settings = settings,
                colors = colors
            )

            // Scan Button
            ScanButton(
                isScanning = radarState.isScanning,
                buttonStyle = settings.scanButtonStyle,
                buttonColor = settings.scanButtonColor,
                buttonSize = settings.scanButtonSize,
                onClick = {
                    if (radarState.isScanning) viewModel.stopScan() else viewModel.startScan()
                },
                colors = colors
            )

            // Bottom Navigation
            BottomNavigationBar(
                currentRoute = "radar",
                onNavigate = { route ->
                    when (route) {
                        "feed" -> onNavigateToFeed()
                        "heatmap" -> onNavigateToHeatmap()
                        "intel" -> onNavigateToIntel()
                        "gear" -> onNavigateToGear()
                    }
                },
                colors = colors
            )
        }

        // Axon Alert Overlay
        showAxonAlert?.let { device ->
            AxonAlertOverlay(
                device = device,
                onDismiss = { viewModel.dismissAxonAlert() },
                colors = colors
            )
        }

        // Device Detail Sheet
        selectedDevice?.let { device ->
            DeviceDetailSheet(
                device = device,
                onDismiss = { selectedDevice = null },
                onMarkThreat = { viewModel.markAsThreat(device, it) },
                colors = colors
            )
        }
    }
}

@Composable
fun RadarHeader(
    callsign: String,
    isScanning: Boolean,
    settings: AppSettings,
    colors: BlueMeanieColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            // BLUEMEANIE Title with BLUE in blue, MEANIE in red
            Text(
                text = buildAnnotatedString {
                    append("BLUE")
                    addStyle(SpanStyle(color = Color(0xFF3B82F6)), 0, 4)
                    append("MEANIE")
                    addStyle(SpanStyle(color = Color(0xFFEF4444)), 4, 10)
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row {
                Text(
                    text = "OPS: ",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
                Text(
                    text = callsign,
                    fontSize = 12.sp,
                    color = colors.primary
                )
                // Hidden easter egg - BM23 in binary, nearly invisible
                Text(
                    text = "01000010 01001101 00110010 00110011",
                    fontSize = 8.sp,
                    color = colors.textMuted.copy(alpha = 0.1f)
                )
            }
        }

        // Scan mode indicator
        ScanModeChip(
            mode = settings.scanMode,
            isScanning = isScanning,
            colors = colors
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ScanModeChip(
    mode: ScanMode,
    isScanning: Boolean,
    colors: BlueMeanieColors
) {
    val modeName = when (mode) {
        ScanMode.TACTICAL -> "TACTICAL"
        ScanMode.STEALTH -> "STEALTH"
        ScanMode.SWEEP -> "SWEEP"
    }

    var showInfo by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { showInfo = !showInfo }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isScanning) colors.primary else colors.textMuted)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = modeName,
            fontSize = 12.sp,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = colors.textMuted,
            modifier = Modifier.size(14.dp)
        )
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("$modeName MODE", color = colors.primary) },
            text = {
                Text(
                    when (mode) {
                        ScanMode.TACTICAL -> "Low latency, high battery usage. Best for active field operations with real-time device detection."
                        ScanMode.STEALTH -> "Low power, periodic scanning. Ideal for extended surveillance with minimal battery consumption."
                        ScanMode.SWEEP -> "Interval burst scanning. Balanced performance for general use with moderate power draw."
                    },
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text("UNDERSTOOD", color = colors.primary)
                }
            }
        )
    }
}

@Composable
fun RadarDisplay(
    devices: List<ScannedDevice>,
    radarStyle: RadarStyle,
    isScanning: Boolean,
    colors: BlueMeanieColors,
    onDeviceClick: (ScannedDevice) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    val radarSize = 280.dp
    val centerOffset = radarSize.value / 2

    Box(
        modifier = Modifier
            .size(radarSize)
            .clip(CircleShape)
            .background(ThemeEngine.RadarBackground)
            .border(2.dp, colors.border, CircleShape)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Draw range rings
            for (i in 1..3) {
                val ringRadius = radius * (i / 3f)
                drawCircle(
                    color = ThemeEngine.RadarRing,
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = 1f)
                )
            }

            // Draw crosshairs
            drawLine(
                color = ThemeEngine.RadarRing.copy(alpha = 0.5f),
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = 1f
            )
            drawLine(
                color = ThemeEngine.RadarRing.copy(alpha = 0.5f),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 1f
            )

            // Draw sweep based on style
            when (radarStyle) {
                RadarStyle.TACTICAL -> {
                    // Rotating sweep arm with trail
                    rotate(sweepAngle, pivot = center) {
                        val sweepPath = Path().apply {
                            moveTo(center.x, center.y)
                            lineTo(center.x, 0f)
                        }
                        drawPath(
                            path = sweepPath,
                            color = colors.primary,
                            style = Stroke(width = 2f)
                        )
                        // Trail glow
                        for (i in 0..5) {
                            val trailAngle = sweepAngle - i * 5
                            rotate(trailAngle, pivot = center) {
                                drawLine(
                                    color = colors.primary.copy(alpha = 0.2f - i * 0.03f),
                                    start = center,
                                    end = Offset(center.x, 0f),
                                    strokeWidth = 8f - i
                                )
                            }
                        }
                    }
                }
                RadarStyle.SONAR -> {
                    // Expanding rings
                    if (isScanning) {
                        val pulseTime = (System.currentTimeMillis() % 2500) / 2500f
                        for (i in 0..2) {
                            val progress = (pulseTime + i * 0.33f) % 1f
                            drawCircle(
                                color = colors.primary.copy(alpha = (1f - progress) * 0.6f),
                                radius = radius * progress,
                                center = center,
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }
                RadarStyle.PULSE -> {
                    // Single thick wave ring
                    if (isScanning) {
                        val pulseTime = (System.currentTimeMillis() % 3000) / 3000f
                        val pulseRadius = radius * pulseTime
                        val pulseWidth = 20f * (1f - pulseTime)
                        drawCircle(
                            color = colors.primary,
                            radius = pulseRadius,
                            center = center,
                            style = Stroke(width = pulseWidth)
                        )
                    }
                }
                RadarStyle.GRID -> {
                    // Crosshair scan lines
                    if (isScanning) {
                        rotate(sweepAngle * 2, pivot = center) {
                            for (i in 0..7) {
                                val angle = i * 45f
                                val innerRadius = radius * 0.1f
                                val outerRadius = radius * 0.9f
                                val radians = Math.toRadians(angle.toDouble())
                                drawLine(
                                    color = colors.primary.copy(alpha = 0.3f),
                                    start = Offset(
                                        center.x + (innerRadius * cos(radians)).toFloat(),
                                        center.y + (innerRadius * sin(radians)).toFloat()
                                    ),
                                    end = Offset(
                                        center.x + (outerRadius * cos(radians)).toFloat(),
                                        center.y + (outerRadius * sin(radians)).toFloat()
                                    ),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }
                }
                RadarStyle.ORBITAL -> {
                    // Orbiting device dots
                    if (isScanning) {
                        devices.take(5).forEachIndexed { index, device ->
                            val orbitAngle = sweepAngle + index * 72f
                            val orbitRadius = radius * 0.6f
                            val radians = Math.toRadians(orbitAngle.toDouble())
                            val x = center.x + (orbitRadius * cos(radians)).toFloat()
                            val y = center.y + (orbitRadius * sin(radians)).toFloat()
                            val dotColor = if (device.isAxon) colors.danger else colors.primary
                            drawCircle(
                                color = dotColor,
                                radius = 6f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            // Draw device dots
            devices.forEachIndexed { index, device ->
                val angle = (index * 137.5f) % 360f  // Golden angle for distribution
                val distance = 0.3f + (device.rssi + 100) / 100f * 0.5f
                val deviceRadius = radius * distance.coerceIn(0.1f, 0.9f)
                val radians = Math.toRadians(angle.toDouble())
                val x = center.x + (deviceRadius * cos(radians)).toFloat()
                val y = center.y + (deviceRadius * sin(radians)).toFloat()

                val dotColor = when {
                    device.isThreat -> colors.danger
                    device.isAxon -> colors.danger
                    device.deviceType == DeviceType.BODY_CAM -> colors.bodyCam
                    device.deviceType == DeviceType.TASER -> colors.taser
                    device.deviceType == DeviceType.FLEX -> colors.flex
                    else -> colors.textMuted
                }

                // Glow effect
                drawCircle(
                    color = dotColor.copy(alpha = 0.3f),
                    radius = 12f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = dotColor,
                    radius = 6f,
                    center = Offset(x, y)
                )
            }

            // Center dot
            drawCircle(
                color = colors.primary,
                radius = 4f,
                center = center
            )

            // Center glow
            drawCircle(
                color = colors.primary.copy(alpha = 0.3f),
                radius = 12f,
                center = center
            )
        }

        // Range labels
        Text(
            text = "10m",
            fontSize = 10.sp,
            color = colors.textMuted,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        )
        Text(
            text = "20m",
            fontSize = 10.sp,
            color = colors.textMuted,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
        )

        // Empty state
        if (devices.isEmpty()) {
            Text(
                text = if (isScanning) "TAP SCAN TO START" else "NO DEVICES IN RANGE",
                fontSize = 12.sp,
                color = colors.textMuted,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun StatusIndicators(
    isScanning: Boolean,
    isBluetoothEnabled: Boolean,
    settings: AppSettings,
    colors: BlueMeanieColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface.copy(alpha = 0.8f))
            .padding(8.dp)
    ) {
        StatusIndicator(
            label = "BLE",
            isActive = isBluetoothEnabled,
            isWarning = !isBluetoothEnabled,
            colors = colors
        )
        StatusIndicator(
            label = "SCAN",
            isActive = isScanning,
            isWarning = false,
            colors = colors
        )
        StatusIndicator(
            label = "NTFY",
            isActive = settings.notificationsEnabled,
            isWarning = !settings.notificationsEnabled,
            colors = colors
        )
        StatusIndicator(
            label = "DB",
            isActive = true,
            isWarning = false,
            colors = colors
        )
    }
}

@Composable
fun StatusIndicator(
    label: String,
    isActive: Boolean,
    isWarning: Boolean,
    colors: BlueMeanieColors
) {
    val infiniteTransition = rememberInfiniteTransition(label = "indicator")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isWarning -> colors.danger
                        isActive -> colors.success
                        else -> colors.textMuted
                    }
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 8.sp,
            color = if (isActive || isWarning) colors.textPrimary else colors.textMuted
        )
    }
}

@Composable
fun StatsBar(
    devicesFound: Int,
    axonHits: Int,
    scanDuration: Long,
    peakRssi: Int,
    settings: AppSettings,
    colors: BlueMeanieColors
) {
    val counterFontSize = when (settings.counterSize) {
        CounterSize.SMALL -> 24.sp
        CounterSize.MEDIUM -> 32.sp
        CounterSize.LARGE -> 40.sp
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            label = "DEVICES",
            value = devicesFound.toString(),
            color = colors.primary,
            fontSize = counterFontSize,
            colors = colors
        )
        StatItem(
            label = "AXON HITS",
            value = axonHits.toString(),
            color = colors.danger,
            fontSize = counterFontSize,
            colors = colors
        )
        StatItem(
            label = "DURATION",
            value = formatDuration(scanDuration),
            color = colors.warning,
            fontSize = counterFontSize,
            colors = colors
        )
        StatItem(
            label = "SIGNAL PEAK",
            value = "${peakRssi}dBm",
            color = colors.success,
            fontSize = counterFontSize,
            colors = colors
        )
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    colors: BlueMeanieColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            fontSize = 8.sp,
            color = colors.textMuted
        )
    }
}

@Composable
fun ScanButton(
    isScanning: Boolean,
    buttonStyle: ScanButtonStyle,
    buttonColor: String,
    buttonSize: ScanButtonSize,
    onClick: () -> Unit,
    colors: BlueMeanieColors
) {
    val buttonSizePx = when (buttonSize) {
        ScanButtonSize.SMALL -> 48.dp
        ScanButtonSize.MEDIUM -> 64.dp
        ScanButtonSize.LARGE -> 80.dp
    }

    val infiniteTransition = rememberInfiniteTransition(label = "button")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isScanning) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val buttonColorParsed = try {
        Color(android.graphics.Color.parseColor(buttonColor))
    } catch (e: Exception) {
        colors.primary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(buttonSizePx)
                .scale(if (isScanning) scale else 1f)
                .clip(if (buttonStyle == ScanButtonStyle.HEX) {
                    androidx.compose.foundation.shape.GenericShape { size, _ ->
                        val path = Path()
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val radius = minOf(size.width, size.height) / 2
                        for (i in 0..5) {
                            val angle = Math.toRadians((i * 60 - 30).toDouble())
                            val x = centerX + (radius * cos(angle)).toFloat()
                            val y = centerY + (radius * sin(angle)).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                    }
                } else CircleShape)
                .background(
                    if (buttonStyle == ScanButtonStyle.GLOW || buttonStyle == ScanButtonStyle.MILITARY) {
                        Brush.radialGradient(
                            colors = listOf(
                                buttonColorParsed.copy(alpha = 0.8f),
                                buttonColorParsed.copy(alpha = 0.4f),
                                buttonColorParsed.copy(alpha = 0.1f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                buttonColorParsed,
                                buttonColorParsed.copy(alpha = 0.7f)
                            )
                        )
                    }
                )
                .border(
                    width = 2.dp,
                    color = buttonColorParsed,
                    shape = if (buttonStyle == ScanButtonStyle.HEX) {
                        androidx.compose.foundation.shape.GenericShape { size, _ ->
                            val path = Path()
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            val radius = minOf(size.width, size.height) / 2
                            for (i in 0..5) {
                                val angle = Math.toRadians((i * 60 - 30).toDouble())
                                val x = centerX + (radius * cos(angle)).toFloat()
                                val y = centerY + (radius * sin(angle)).toFloat()
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            path.close()
                        }
                    } else CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isScanning) "Stop" else "Start",
                tint = colors.textPrimary,
                modifier = Modifier.size(buttonSizePx / 2)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    colors: BlueMeanieColors
) {
    val items = listOf(
        Triple("RADAR", Icons.Default.Radar, "radar"),
        Triple("FEED", Icons.Default.List, "feed"),
        Triple("HEATMAP", Icons.Default.Map, "heatmap"),
        Triple("INTEL", Icons.Default.Shield, "intel"),
        Triple("GEAR", Icons.Default.Settings, "gear")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { (label, icon, route) ->
            val isActive = currentRoute == route
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigate(route) }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) colors.primary else colors.textMuted,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = if (isActive) colors.primary else colors.textMuted
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(16.dp, 2.dp)
                            .background(colors.primary, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun AxonAlertOverlay(
    device: ScannedDevice,
    onDismiss: () -> Unit,
    colors: BlueMeanieColors
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alert")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(3.dp, colors.danger, RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alert",
                    tint = colors.danger,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "⚠️ AXON DETECTED ⚠️",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.danger
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = device.name ?: "Unknown Device",
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = device.macAddress,
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Confidence: ${device.confidence}%",
                    fontSize = 14.sp,
                    color = colors.warning
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap to dismiss",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
fun DeviceDetailSheet(
    device: ScannedDevice,
    onDismiss: () -> Unit,
    onMarkThreat: (Boolean) -> Unit,
    colors: BlueMeanieColors
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = device.name ?: "Unknown Device",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Row {
                        Text(
                            text = device.deviceType.name,
                            fontSize = 12.sp,
                            color = when {
                                device.isAxon -> colors.danger
                                device.isThreat -> colors.danger
                                else -> colors.textMuted
                            }
                        )
                        if (device.isAxon) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "● AXON",
                                fontSize = 12.sp,
                                color = colors.danger
                            )
                        }
                    }
                }
                // Hidden easter egg - braille
                Text(
                    text = "⠃⠍⠆⠒",
                    fontSize = 16.sp,
                    color = colors.textMuted.copy(alpha = 0.1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MAC Address
            DetailRow(label = "MAC ADDRESS", value = device.macAddress, colors = colors)

            // OUI
            device.oui?.let {
                DetailRow(label = "OUI", value = it, colors = colors)
            }

            // RSSI
            DetailRow(label = "SIGNAL STRENGTH", value = "${device.rssi} dBm", colors = colors)

            // Confidence
            if (device.isAxon) {
                DetailRow(label = "CONFIDENCE", value = "${device.confidence}%", colors = colors)
            }

            // First Seen
            DetailRow(
                label = "FIRST SEEN",
                value = formatTimestamp(device.firstSeen),
                colors = colors
            )

            // Last Seen
            DetailRow(
                label = "LAST SEEN",
                value = formatTimestamp(device.lastSeen),
                colors = colors
            )

            // Detection Count
            DetailRow(label = "DETECTIONS", value = device.detectionCount.toString(), colors = colors)

            Spacer(modifier = Modifier.height(24.dp))

            // Mark as Threat Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mark as Threat",
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Switch(
                    checked = device.isThreat,
                    onCheckedChange = onMarkThreat,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.danger,
                        checkedTrackColor = colors.danger.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    colors: BlueMeanieColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.textMuted
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = millis / (1000 * 60 * 60)
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
    return sdf.format(java.util.Date(timestamp))
}