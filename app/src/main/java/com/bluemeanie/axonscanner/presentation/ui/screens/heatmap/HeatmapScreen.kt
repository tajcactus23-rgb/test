package com.bluemeanie.axonscanner.presentation.ui.screens.heatmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.bluemeanie.axonscanner.domain.model.ScannedDevice
import com.bluemeanie.axonscanner.presentation.ui.screens.radar.BottomNavigationBar
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieTheme
import com.bluemeanie.axonscanner.presentation.viewmodel.RadarViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Circle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HeatmapScreen(
    viewModel: RadarViewModel = hiltViewModel(),
    onNavigateToRadar: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToIntel: () -> Unit,
    onNavigateToGear: () -> Unit
) {
    val devices by viewModel.devices.collectAsState()
    val colors = BlueMeanieTheme.colors
    val context = LocalContext.current

    var showMyDetections by remember { mutableStateOf(true) }
    var showCommunity by remember { mutableStateOf(false) }
    var autoCenter by remember { mutableStateOf(true) }
    var userLocation by remember { mutableStateOf<Location?>(null) }

    // Initialize OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Request location permission
    val locationPermissionGranted = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

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
                text = "HEAT DENSITY",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        // Filter toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToggleChip(
                label = "MY DETECTIONS",
                isSelected = showMyDetections,
                onClick = { showMyDetections = !showMyDetections },
                colors = colors
            )
            ToggleChip(
                label = "COMMUNITY",
                isSelected = showCommunity,
                onClick = { showCommunity = !showCommunity },
                colors = colors
            )
            Spacer(modifier = Modifier.weight(1f))
            ToggleChip(
                label = "AUTO CENTER",
                isSelected = autoCenter,
                onClick = { autoCenter = !autoCenter },
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Map
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
        ) {
            if (locationPermissionGranted) {
                // OSMDroid MapView
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            // Default to a central location (NYC)
                            controller.setCenter(GeoPoint(40.7128, -74.0060))
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { mapView ->
                        mapView.overlays.clear()

                        // Add heat circles for devices
                        if (showMyDetections) {
                            devices.forEach { device ->
                                // In production, use actual device location
                                val devicePoint = GeoPoint(
                                    40.7128 + (Math.random() - 0.5) * 0.01,
                                    -74.0060 + (Math.random() - 0.5) * 0.01
                                )
                                
                                val circle = Circle().apply {
                                    center = devicePoint
                                    radius = if (device.isAxon) 50.0 else 30.0
                                    fillPaint.color = android.graphics.Color.argb(
                                        if (device.isAxon) 100 else 50,
                                        if (device.isAxon) 255 else 0,
                                        if (device.isAxon) 0 else 240,
                                        if (device.isAxon) 0 else 255
                                    )
                                    outlinePaint.color = android.graphics.Color.argb(
                                        200,
                                        if (device.isAxon) 255 else 0,
                                        if (device.isAxon) 0 else 240,
                                        if (device.isAxon) 0 else 255
                                    )
                                    outlinePaint.strokeWidth = 2f
                                }
                                mapView.overlays.add(circle)
                            }
                        }

                        // User location marker
                        if (autoCenter && userLocation != null) {
                            val userPoint = GeoPoint(userLocation!!.latitude, userLocation!!.longitude)
                            mapView.controller.setCenter(userPoint)
                        }

                        mapView.invalidate()
                    }
                )

                // Pulsing user location indicator overlay
                if (autoCenter) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    ) {
                        // Pulse animation would be here
                    }
                }
            } else {
                // No location permission
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "LOCATION UNAVAILABLE",
                        fontSize = 14.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = "Enable location for heatmap",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }

            // Map corner brackets
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(colors.primary.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, colors.primary, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(colors.primary.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, colors.primary, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = colors.primary, label = "BLE")
            LegendItem(color = colors.danger, label = "AXON")
            LegendItem(color = colors.warning, label = "DENSE")
            LegendItem(color = colors.textMuted, label = "COMMUNITY")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Navigation
        BottomNavigationBar(
            currentRoute = "heatmap",
            onNavigate = { route ->
                when (route) {
                    "radar" -> onNavigateToRadar()
                    "feed" -> onNavigateToFeed()
                    "intel" -> onNavigateToIntel()
                    "gear" -> onNavigateToGear()
                }
            },
            colors = colors
        )
    }
}

@Composable
fun ToggleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: BlueMeanieTheme.colors
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.surface)
            .border(
                1.dp,
                if (isSelected) colors.primary else colors.border,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isSelected) colors.primary else colors.textMuted
            )
        }
    }
}

@Composable
fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = BlueMeanieTheme.colors.textMuted
        )
    }
}