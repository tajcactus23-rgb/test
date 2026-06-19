package com.bluemeanie.axonscanner.presentation.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.bluemeanie.axonscanner.domain.model.DeviceType
import com.bluemeanie.axonscanner.domain.model.ScannedDevice
import com.bluemeanie.axonscanner.presentation.ui.screens.radar.BottomNavigationBar
import com.bluemeanie.axonscanner.presentation.ui.screens.radar.DeviceDetailSheet
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieTheme
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieColors
import com.bluemeanie.axonscanner.presentation.viewmodel.RadarViewModel

enum class FeedFilter {
    ALL, AXON, THREATS, SESSION
}

enum class FeedSort {
    TIME, SIGNAL, DISTANCE
}

@Composable
fun FeedScreen(
    viewModel: RadarViewModel = hiltViewModel(),
    onNavigateToRadar: () -> Unit,
    onNavigateToHeatmap: () -> Unit,
    onNavigateToIntel: () -> Unit,
    onNavigateToGear: () -> Unit
) {
    val devices by viewModel.devices.collectAsState()
    val colors = BlueMeanieTheme.colors

    var selectedFilter by remember { mutableStateOf(FeedFilter.ALL) }
    var selectedSort by remember { mutableStateOf(FeedSort.TIME) }
    var selectedDevice by remember { mutableStateOf<ScannedDevice?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredDevices = devices
        .filter { device ->
            val matchesFilter = when (selectedFilter) {
                FeedFilter.ALL -> true
                FeedFilter.AXON -> device.isAxon
                FeedFilter.THREATS -> device.isThreat
                FeedFilter.SESSION -> device.lastSeen > System.currentTimeMillis() - (60 * 60 * 1000)
            }
            val matchesSearch = searchQuery.isEmpty() ||
                    device.name?.contains(searchQuery, ignoreCase = true) == true ||
                    device.macAddress.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
        .sortedWith(
            when (selectedSort) {
                FeedSort.TIME -> compareByDescending { it.lastSeen }
                FeedSort.SIGNAL -> compareByDescending { it.rssi }
                FeedSort.DISTANCE -> compareBy { it.rssi }
            }
        )

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
                text = "DETECTION FEED",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            // Hidden easter egg
            Text(
                text = "data-bm23=\"hidden\"",
                fontSize = 1.sp,
                color = colors.textMuted.copy(alpha = 0f)
            )
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search devices...", color = colors.textMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search devices", tint = colors.textMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.primary,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "ALL",
                isSelected = selectedFilter == FeedFilter.ALL,
                onClick = { selectedFilter = FeedFilter.ALL },
                colors = colors
            )
            FilterChip(
                label = "AXON",
                isSelected = selectedFilter == FeedFilter.AXON,
                onClick = { selectedFilter = FeedFilter.AXON },
                colors = colors,
                highlightColor = colors.danger
            )
            FilterChip(
                label = "THREATS",
                isSelected = selectedFilter == FeedFilter.THREATS,
                onClick = { selectedFilter = FeedFilter.THREATS },
                colors = colors,
                highlightColor = colors.danger
            )
            FilterChip(
                label = "SESSION",
                isSelected = selectedFilter == FeedFilter.SESSION,
                onClick = { selectedFilter = FeedFilter.SESSION },
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sort
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Sort: ",
                fontSize = 12.sp,
                color = colors.textMuted
            )
            SortOption(
                label = "TIME",
                isSelected = selectedSort == FeedSort.TIME,
                onClick = { selectedSort = FeedSort.TIME },
                colors = colors
            )
            Text(
                text = " | ",
                fontSize = 12.sp,
                color = colors.textMuted
            )
            SortOption(
                label = "SIGNAL",
                isSelected = selectedSort == FeedSort.SIGNAL,
                onClick = { selectedSort = FeedSort.SIGNAL },
                colors = colors
            )
            Text(
                text = " | ",
                fontSize = 12.sp,
                color = colors.textMuted
            )
            SortOption(
                label = "DISTANCE",
                isSelected = selectedSort == FeedSort.DISTANCE,
                onClick = { selectedSort = FeedSort.DISTANCE },
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device List
        if (filteredDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "NO CONTACTS MATCH FILTER",
                        fontSize = 14.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToRadar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.textPrimary
                        )
                    ) {
                        Text("START SCANNING")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredDevices, key = { it.macAddress }) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { selectedDevice = device },
                        onLongPress = { viewModel.markAsThreat(device, !device.isThreat) },
                        colors = BlueMeanieTheme.colors
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Bottom Navigation
        BottomNavigationBar(
            currentRoute = "feed",
            onNavigate = { route ->
                when (route) {
                    "radar" -> onNavigateToRadar()
                    "heatmap" -> onNavigateToHeatmap()
                    "intel" -> onNavigateToIntel()
                    "gear" -> onNavigateToGear()
                }
            },
            colors = colors
        )
    }

    selectedDevice?.let { device ->
        DeviceDetailSheet(
            device = device,
            onDismiss = { selectedDevice = null },
            onMarkThreat = { viewModel.markAsThreat(device, it) },
            colors = colors
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: BlueMeanieColors,
    highlightColor: androidx.compose.ui.graphics.Color? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) (highlightColor ?: colors.primary).copy(alpha = 0.2f) else colors.surface)
            .border(
                1.dp,
                if (isSelected) (highlightColor ?: colors.primary) else colors.border,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) (highlightColor ?: colors.primary) else colors.textMuted
        )
    }
}

@Composable
fun SortOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: BlueMeanieColors
) {
    Text(
        text = label,
        fontSize = 12.sp,
        color = if (isSelected) colors.primary else colors.textMuted,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun DeviceCard(
    device: ScannedDevice,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    colors: BlueMeanieColors
) {
    val deviceColor = when {
        device.isThreat -> colors.danger
        device.isAxon -> colors.danger
        device.deviceType == DeviceType.BODY_CAM -> colors.bodyCam
        device.deviceType == DeviceType.TASER -> colors.taser
        device.deviceType == DeviceType.FLEX -> colors.flex
        else -> colors.textMuted
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, deviceColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.name ?: "Unknown",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    if (device.isAxon) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.danger)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AXON",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                    }
                    if (device.isThreat) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Threat",
                            tint = colors.danger,
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
                Text(
                    text = "${device.rssi}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = deviceColor,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "dBm",
                    fontSize = 10.sp,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimeAgo(device.lastSeen),
                    fontSize = 10.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}
