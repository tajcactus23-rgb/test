package com.bluemeanie.axonscanner.presentation.ui.screens.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bluemeanie.axonscanner.presentation.ui.theme.BlueMeanieTheme
import com.bluemeanie.axonscanner.presentation.viewmodel.SettingsViewModel

sealed class OnboardingStep {
    object Welcome : OnboardingStep()
    object CallSign : OnboardingStep()
    object Bluetooth : OnboardingStep()
    object Location : OnboardingStep()
    object Notifications : OnboardingStep()
    object Battery : OnboardingStep()
    object Ready : OnboardingStep()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onComplete: () -> Unit
) {
    val colors = BlueMeanieTheme.colors
    val context = LocalContext.current

    var currentStep by remember { mutableStateOf<OnboardingStep>(OnboardingStep.Welcome) }
    var callsign by remember { mutableStateOf("") }
    var bluetoothGranted by remember { mutableStateOf(false) }
    var locationGranted by remember { mutableStateOf(false) }
    var notificationsGranted by remember { mutableStateOf(false) }
    var logoTapCount by remember { mutableIntStateOf(0) }
    var showMorseToast by remember { mutableStateOf(false) }

    // Permission launchers
    val bluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        bluetoothGranted = permissions.values.all { it }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationGranted = granted
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        val progressValue = when (currentStep) {
            is OnboardingStep.Welcome -> 0.1f
            is OnboardingStep.CallSign -> 0.25f
            is OnboardingStep.Bluetooth -> 0.4f
            is OnboardingStep.Location -> 0.55f
            is OnboardingStep.Notifications -> 0.7f
            is OnboardingStep.Battery -> 0.85f
            is OnboardingStep.Ready -> 1f
        }
        LinearProgressIndicator(
            progress = progressValue,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = colors.primary,
            trackColor = colors.border,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Content based on step
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (currentStep) {
                is OnboardingStep.Welcome -> WelcomeStep(
                    onLogoTap = {
                        logoTapCount++
                        if (logoTapCount >= 5) {
                            showMorseToast = true
                            logoTapCount = 0
                        }
                    },
                    showMorseToast = showMorseToast,
                    colors = colors
                )

                is OnboardingStep.CallSign -> CallSignStep(
                    callsign = callsign,
                    onCallsignChange = { callsign = it.uppercase() },
                    colors = colors
                )

                is OnboardingStep.Bluetooth -> PermissionStep(
                    title = "BLUETOOTH ACCESS",
                    icon = Icons.Default.Bluetooth,
                    description = "Required for BLE scanning of nearby devices. All scanning is performed locally on your device only. No data is transmitted to the internet.",
                    bullets = listOf(
                        "Local device scanning only",
                        "No internet transmission",
                        "No data collection"
                    ),
                    isGranted = bluetoothGranted,
                    onRequest = {
                        bluetoothLauncher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                        )
                    },
                    colors = colors
                )

                is OnboardingStep.Location -> PermissionStep(
                    title = "LOCATION ACCESS",
                    icon = Icons.Default.LocationOn,
                    description = "Android requires location permission for BLE scanning due to platform restrictions. Location data is stored locally only.",
                    bullets = listOf(
                        "Required for BLE scanning",
                        "Optional for heatmap",
                        "No background tracking",
                        "No cloud upload"
                    ),
                    isGranted = locationGranted,
                    onRequest = {
                        locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    colors = colors
                )

                is OnboardingStep.Notifications -> PermissionStep(
                    title = "NOTIFICATION ACCESS",
                    icon = Icons.Default.Notifications,
                    description = "Used for detection alerts, persistent scanner notification, and background scan awareness.",
                    bullets = listOf(
                        "Detection alerts",
                        "Background scan status",
                        "Can be disabled in settings"
                    ),
                    isGranted = notificationsGranted,
                    onRequest = {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    colors = colors
                )

                is OnboardingStep.Battery -> BatteryOptimizationStep(colors = colors)

                is OnboardingStep.Ready -> ReadyStep(
                    bluetoothGranted = bluetoothGranted,
                    locationGranted = locationGranted,
                    notificationsGranted = notificationsGranted,
                    colors = colors
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            if (currentStep !is OnboardingStep.Welcome) {
                TextButton(
                    onClick = {
                        currentStep = when (currentStep) {
                            is OnboardingStep.CallSign -> OnboardingStep.Welcome
                            is OnboardingStep.Bluetooth -> OnboardingStep.CallSign
                            is OnboardingStep.Location -> OnboardingStep.Bluetooth
                            is OnboardingStep.Notifications -> OnboardingStep.Location
                            is OnboardingStep.Battery -> OnboardingStep.Notifications
                            is OnboardingStep.Ready -> OnboardingStep.Battery
                            else -> currentStep
                        }
                    }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textMuted)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BACK", color = colors.textMuted)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Next/Complete button
            Button(
                onClick = {
                    when (currentStep) {
                        is OnboardingStep.Welcome -> currentStep = OnboardingStep.CallSign
                        is OnboardingStep.CallSign -> {
                            if (callsign.isNotBlank()) {
                                viewModel.updateCallsign(callsign)
                                currentStep = OnboardingStep.Bluetooth
                            }
                        }
                        is OnboardingStep.Bluetooth -> currentStep = OnboardingStep.Location
                        is OnboardingStep.Location -> currentStep = OnboardingStep.Notifications
                        is OnboardingStep.Notifications -> currentStep = OnboardingStep.Battery
                        is OnboardingStep.Battery -> currentStep = OnboardingStep.Ready
                        is OnboardingStep.Ready -> {
                            onComplete()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.textPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = when (currentStep) {
                    is OnboardingStep.CallSign -> callsign.isNotBlank()
                    else -> true
                }
            ) {
                Text(
                    if (currentStep is OnboardingStep.Ready) "ENTER THE FIELD" else "NEXT"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    if (currentStep is OnboardingStep.Ready) Icons.Default.ArrowForward else Icons.Default.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun WelcomeStep(
    onLogoTap: () -> Unit,
    showMorseToast: Boolean,
    colors: BlueMeanieTheme.colors
) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Animated Logo
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(colors.surface)
                .border(2.dp, Brush.linearGradient(listOf(colors.primary, colors.secondary)), CircleShape)
                .clickable(onClick = onLogoTap),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Radar,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title - BLUE in blue, MEANIE in red
        Text(
            text = buildAnnotatedString {
                append("BLUE")
                withStyle(SpanStyle(color = Color(0xFF3B82F6))) { }
                append("MEANIE")
                withStyle(SpanStyle(color = Color(0xFFEF4444))) { }
            },
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "APEX EDITION",
            fontSize = 14.sp,
            color = colors.primary,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "The ultimate BLE Axon device scanner",
            fontSize = 16.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Morse code decoration (hidden)
        Text(
            text = "-... .-.. ..- . -- .- -. .. . -.-. ...",
            fontSize = 1.sp,
            color = colors.primary.copy(alpha = 0.1f),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun CallSignStep(
    callsign: String,
    onCallsignChange: (String) -> Unit,
    colors: BlueMeanieTheme.colors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CALLSIGN SETUP",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter your operational callsign",
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = callsign,
            onValueChange = onCallsignChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Callsign", color = colors.textMuted) },
            placeholder = { Text("OPERATOR", color = colors.textMuted.copy(alpha = 0.5f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.primary
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
        )
    }
}

@Composable
fun PermissionStep(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    bullets: List<String>,
    isGranted: Boolean,
    onRequest: () -> Unit,
    colors: BlueMeanieTheme.colors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGranted) colors.success else colors.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bullets
        bullets.forEach { bullet ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = bullet,
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Status
        if (isGranted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.success.copy(alpha = 0.2f))
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.success
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PERMISSION GRANTED",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.success
                )
            }
        } else {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.textPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("REQUEST PERMISSION")
            }
        }
    }
}

@Composable
fun BatteryOptimizationStep(colors: BlueMeanieTheme.colors) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.BatteryChargingFull,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "BATTERY OPTIMIZATION",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Required for long-running scans and foreground persistence. This ensures scan reliability.",
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .clickable {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Disable Battery Optimization",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tap to open system settings",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = colors.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This step is optional but recommended for field operations.",
            fontSize = 12.sp,
            color = colors.textMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ReadyStep(
    bluetoothGranted: Boolean,
    locationGranted: Boolean,
    notificationsGranted: Boolean,
    colors: BlueMeanieTheme.colors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = colors.success,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ARMED & READY",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.success
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Checklist
        ChecklistItem(
            label = "Bluetooth Ready",
            isComplete = bluetoothGranted,
            colors = colors
        )
        ChecklistItem(
            label = "Scanner Permissions",
            isComplete = bluetoothGranted,
            colors = colors
        )
        ChecklistItem(
            label = "Notification Engine",
            isComplete = notificationsGranted,
            colors = colors
        )
        ChecklistItem(
            label = "Map Engine",
            isComplete = true,
            colors = colors,
            note = if (!locationGranted) "Limited" else null
        )
        ChecklistItem(
            label = "Background Services",
            isComplete = true,
            colors = colors
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "All systems operational. You're ready to enter the field.",
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChecklistItem(
    label: String,
    isComplete: Boolean,
    colors: BlueMeanieTheme.colors,
    note: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isComplete) colors.success else colors.textMuted,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isComplete) colors.textPrimary else colors.textMuted
        )
        if (note != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "($note)",
                fontSize = 12.sp,
                color = colors.warning
            )
        }
    }
}