package com.bluemeanie.axonscanner.presentation.ui.screens.intel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bluemeanie.axonscanner.domain.model.ScannedDevice
import com.bluemeanie.axonscanner.presentation.ui.screens.radar.BottomNavigationBar
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieTheme
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieColors
import com.bluemeanie.axonscanner.presentation.viewmodel.RadarViewModel
import com.bluemeanie.axonscanner.presentation.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IntelScreen(
    viewModel: RadarViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToRadar: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToHeatmap: () -> Unit,
    onNavigateToGear: () -> Unit
) {
    val devices by viewModel.devices.collectAsState()
    val colors = BlueMeanieTheme.colors

    val axonDevices = devices.filter { it.isAxon }
    val threatDevices = devices.filter { it.isThreat }

    var exportContent by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "THREAT INTEL",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Overview
            item {
                StatsOverviewCard(
                    totalDevices = devices.size,
                    axonDevices = axonDevices.size,
                    threatDevices = threatDevices.size,
                    colors = colors
                )
            }

            // Top Threats
            item {
                Text(
                    text = "TOP THREATS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.danger
                )
            }

            if (threatDevices.isEmpty() && axonDevices.isEmpty()) {
                item {
                    EmptyThreatsCard(colors = colors)
                }
            } else {
                items((threatDevices + axonDevices).sortedByDescending { it.confidence }.take(10)) { device ->
                    ThreatCard(device = device, colors = colors)
                }
            }

            // Detection Patterns
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DETECTION PATTERNS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }

            item {
                DetectionPatternsCard(
                    devices = devices,
                    colors = colors
                )
            }

            // Export Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        exportContent = settingsViewModel.exportAllData()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXPORT REPORT (.TXT)")
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Bottom Navigation
        BottomNavigationBar(
            currentRoute = "intel",
            onNavigate = { route ->
                when (route) {
                    "radar" -> onNavigateToRadar()
                    "feed" -> onNavigateToFeed()
                    "heatmap" -> onNavigateToHeatmap()
                    "gear" -> onNavigateToGear()
                }
            },
            colors = colors
        )
    }

    // Export Dialog
    exportContent?.let { content ->
        AlertDialog(
            onDismissRequest = { exportContent = null },
            title = { Text("REPORT EXPORTED", color = colors.primary) },
            text = {
                Column {
                    Text(
                        text = "Report has been copied to clipboard.",
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = content.take(500) + "...",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { exportContent = null }) {
                    Text("CLOSE", color = colors.primary)
                }
            }
        )
    }
}

@Composable
fun StatsOverviewCard(
    totalDevices: Int,
    axonDevices: Int,
    threatDevices: Int,
    colors: BlueMeanieColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatColumn(
            value = totalDevices.toString(),
            label = "TOTAL",
            color = colors.primary,
            colors = colors
        )
        StatColumn(
            value = axonDevices.toString(),
            label = "AXON",
            color = colors.danger,
            colors = colors
        )
        StatColumn(
            value = threatDevices.toString(),
            label = "THREATS",
            color = colors.warning,
            colors = colors
        )
    }
}

@Composable
fun StatColumn(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    colors: BlueMeanieColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.textMuted
        )
    }
}

@Composable
fun ThreatCard(
    device: ScannedDevice,
    colors: BlueMeanieColors
) {
    val riskLevel = when {
        device.confidence >= 80 -> "HIGH"
        device.confidence >= 50 -> "MEDIUM"
        else -> "LOW"
    }
    val riskColor = when (riskLevel) {
        "HIGH" -> colors.danger
        "MEDIUM" -> colors.warning
        else -> colors.success
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, riskColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = device.name ?: "Unknown Device",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = device.macAddress,
                    fontSize = 11.sp,
                    color = colors.textMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(riskColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$riskLevel RISK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${device.confidence}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = riskColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun EmptyThreatsCard(colors: BlueMeanieColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ShieldMoon,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NO AXON THREATS LOGGED",
                fontSize = 14.sp,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Default.Radar,
                contentDescription = null,
                tint = colors.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun DetectionPatternsCard(
    devices: List<ScannedDevice>,
    colors: BlueMeanieColors
) {
    // Analyze detection patterns
    val hourDistribution = remember(devices) {
        devices.groupBy {
            Calendar.getInstance().apply { timeInMillis = it.lastSeen }.get(Calendar.HOUR_OF_DAY)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "DETECTIONS BY HOUR",
                fontSize = 12.sp,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Simple bar chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                for (hour in 0..23) {
                    val count = hourDistribution[hour]?.size ?: 0
                    val maxCount = hourDistribution.values.maxOfOrNull { it.size } ?: 1
                    val height = (count.toFloat() / maxCount * 60).coerceAtLeast(4f)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(height.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (count > 0) colors.primary else colors.border)
                        )
                        if (hour % 6 == 0) {
                            Text(
                                text = "${hour}h",
                                fontSize = 8.sp,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Peak: ${hourDistribution.maxByOrNull { it.value.size }?.key ?: 0}h",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = "Total: ${devices.size} detections",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}