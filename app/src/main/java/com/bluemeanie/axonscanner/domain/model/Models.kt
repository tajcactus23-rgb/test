package com.bluemeanie.axonscanner.domain.model

data class ScannedDevice(
    val id: Long = 0,
    val macAddress: String,
    val name: String?,
    val oui: String?,
    val rssi: Int,
    val rssiHistory: List<Int> = emptyList(),
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val detectionCount: Int = 1,
    val isAxon: Boolean = false,
    val confidence: Int = 0,
    val isThreat: Boolean = false,
    val notes: String? = null,
    val deviceType: DeviceType = DeviceType.UNKNOWN,
    val manufacturer: String? = null,
    val serviceUuids: List<String> = emptyList(),
    val txPower: Int? = null
)

enum class DeviceType {
    BODY_CAM,
    TASER,
    FLEX,
    UNKNOWN,
    BLE_GENERIC
}

data class ScanSession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val durationSec: Int = 0,
    val devicesFound: Int = 0,
    val axonHits: Int = 0,
    val scanMode: ScanMode = ScanMode.TACTICAL,
    val locationLat: Double? = null,
    val locationLng: Double? = null
)

enum class ScanMode {
    TACTICAL,  // Low latency, high battery
    STEALTH,   // Low power, periodic
    SWEEP      // Interval bursts
}

data class DetectionAlert(
    val id: Long = 0,
    val device: ScannedDevice,
    val timestamp: Long = System.currentTimeMillis(),
    val rssi: Int,
    val distanceEstimate: String? = null,
    val alertType: AlertType = AlertType.AXON_DETECTED
)

enum class AlertType {
    AXON_DETECTED,
    THREAT_DETECTED,
    NEW_DEVICE,
    DEVICE_LOST
}

data class RadarState(
    val isScanning: Boolean = false,
    val devices: List<ScannedDevice> = emptyList(),
    val currentSession: ScanSession? = null,
    val scanMode: ScanMode = ScanMode.TACTICAL,
    val scanDuration: Long = 0,
    val peakRssi: Int = -100,
    val axonHits: Int = 0
)

data class AppSettings(
    val theme: ThemeType = ThemeType.CLASSIC,
    val background: BackgroundType = BackgroundType.NEURAL,
    val backgroundOpacity: Float = 0.5f,
    val performanceMode: Boolean = false,
    val radarStyle: RadarStyle = RadarStyle.TACTICAL,
    val scanButtonStyle: ScanButtonStyle = ScanButtonStyle.PULSE,
    val scanButtonColor: String = "#00F0FF",
    val scanButtonSize: ScanButtonSize = ScanButtonSize.MEDIUM,
    val counterSize: CounterSize = CounterSize.MEDIUM,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val fullscreenAlertEnabled: Boolean = true,
    val telegramBotToken: String? = null,
    val telegramChatId: String? = null,
    val scanMode: ScanMode = ScanMode.TACTICAL,
    val fontRadarSize: Int = 32,
    val fontResultsSize: Int = 14,
    val fontMenuSize: Int = 12,
    val fontSettingsSize: Int = 14,
    val fontConsoleSize: Int = 10,
    val fontGaugeSize: Int = 36,
    val fontRadarFamily: String = "JetBrains Mono",
    val fontResultsFamily: String = "Fira Code",
    val fontMenuFamily: String = "JetBrains Mono",
    val fontSettingsFamily: String = "Source Code Pro",
    val fontConsoleFamily: String = "JetBrains Mono",
    val fontGaugeFamily: String = "Space Mono"
)

enum class ThemeType {
    CLASSIC,
    CARBON,
    TITANIUM,
    AURORA,
    MONOLITH,
    ARCTIC,
    MIDNIGHT,
    QUANTUM,
    NOVA,
    GLASS,
    INFERNO,
    SPECTRE,
    EMBER,
    PHANTOM,
    VENOM
}

enum class BackgroundType {
    NEURAL,
    PARTICLE,
    MATRIX,
    AURORA,
    GRID
}

enum class RadarStyle {
    TACTICAL,
    SONAR,
    PULSE,
    GRID,
    ORBITAL
}

enum class ScanButtonStyle {
    PULSE,
    GLOW,
    MILITARY,
    MINIMAL,
    HEX
}

enum class ScanButtonSize {
    SMALL,
    MEDIUM,
    LARGE
}

enum class CounterSize {
    SMALL,
    MEDIUM,
    LARGE
}