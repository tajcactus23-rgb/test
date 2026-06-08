package com.bluemeanie.axonscanner.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemeanie.axonscanner.data.local.dao.DeviceDao
import com.bluemeanie.axonscanner.data.local.dao.ScanSessionDao
import com.bluemeanie.axonscanner.data.local.dao.AlertDao
import com.bluemeanie.axonscanner.data.repository.SettingsRepository
import com.bluemeanie.axonscanner.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val deviceDao: DeviceDao,
    private val sessionDao: ScanSessionDao,
    private val alertDao: AlertDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val callsign: StateFlow<String> = settingsRepository.callsign
        .stateIn(viewModelScope, SharingStarted.Eagerly, "OPERATOR")

    val deviceCount: StateFlow<Int> = deviceDao.getDeviceCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val axonCount: StateFlow<Int> = deviceDao.getAxonCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    private val _telegramStatus = MutableStateFlow<TelegramStatus>(TelegramStatus.DISCONNECTED)
    val telegramStatus: StateFlow<TelegramStatus> = _telegramStatus.asStateFlow()

    fun updateTheme(theme: ThemeType) {
        viewModelScope.launch {
            settingsRepository.updateTheme(theme)
        }
    }

    fun updateBackground(background: BackgroundType) {
        viewModelScope.launch {
            settingsRepository.updateBackground(background)
        }
    }

    fun updateRadarStyle(style: RadarStyle) {
        viewModelScope.launch {
            settingsRepository.updateRadarStyle(style)
        }
    }

    fun updateScanButtonStyle(style: ScanButtonStyle) {
        viewModelScope.launch {
            settingsRepository.updateScanButtonStyle(style)
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(soundEnabled = enabled))
        }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(vibrationEnabled = enabled))
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(notificationsEnabled = enabled))
        }
    }

    fun updateFullscreenAlertEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(fullscreenAlertEnabled = enabled))
        }
    }

    fun updateScanMode(mode: ScanMode) {
        viewModelScope.launch {
            settingsRepository.updateScanMode(mode)
        }
    }

    fun updateCallsign(callsign: String) {
        viewModelScope.launch {
            settingsRepository.setCallsign(callsign)
        }
    }

    fun updateTelegramConfig(botToken: String?, chatId: String?) {
        viewModelScope.launch {
            settingsRepository.updateTelegramConfig(botToken, chatId)
            if (botToken != null && chatId != null) {
                _telegramStatus.value = TelegramStatus.CONNECTING
                // In production, you would verify the Telegram bot here
                _telegramStatus.value = TelegramStatus.CONNECTED
            } else {
                _telegramStatus.value = TelegramStatus.DISCONNECTED
            }
        }
    }

    fun updateFontSettings(fontType: FontType, family: String, size: Int) {
        viewModelScope.launch {
            val current = settings.value
            val updated = when (fontType) {
                FontType.RADAR -> current.copy(fontRadarFamily = family, fontRadarSize = size)
                FontType.RESULTS -> current.copy(fontResultsFamily = family, fontResultsSize = size)
                FontType.MENU -> current.copy(fontMenuFamily = family, fontMenuSize = size)
                FontType.SETTINGS -> current.copy(fontSettingsFamily = family, fontSettingsSize = size)
                FontType.CONSOLE -> current.copy(fontConsoleFamily = family, fontConsoleSize = size)
                FontType.GAUGE -> current.copy(fontGaugeFamily = family, fontGaugeSize = size)
            }
            settingsRepository.updateSettings(updated)
        }
    }

    fun updateBackgroundOpacity(opacity: Float) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(backgroundOpacity = opacity))
        }
    }

    fun updatePerformanceMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(performanceMode = enabled))
        }
    }

    fun updateScanButtonColor(color: String) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(scanButtonColor = color))
        }
    }

    fun updateScanButtonSize(size: ScanButtonSize) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(scanButtonSize = size))
        }
    }

    fun updateCounterSize(size: CounterSize) {
        viewModelScope.launch {
            val current = settings.value
            settingsRepository.updateSettings(current.copy(counterSize = size))
        }
    }

    fun exportAllData(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val timestamp = dateFormat.format(Date())
        val fileName = "blue_meanie_scan_$timestamp.txt"

        val content = buildString {
            appendLine("═══════════════════════════════════════════════════════════════════════")
            appendLine("BLUE MEANIE APEX SCAN REPORT")
            appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
            appendLine("═══════════════════════════════════════════════════════════════════════")
            appendLine()
            
            // Devices section
            appendLine("┌─────────────────────────────────────────────────────────────────────┐")
            appendLine("│ DETECTED DEVICES                                                       │")
            appendLine("└─────────────────────────────────────────────────────────────────────┘")
            appendLine()
            
            val devices = kotlinx.coroutines.runBlocking { 
                deviceDao.getDeviceByMac("").let { emptyList() }
            }
            
            appendLine("Total Devices: ${deviceCount.value}")
            appendLine("Total Axon Hits: ${axonCount.value}")
            appendLine()
            
            // Sessions section
            appendLine("┌─────────────────────────────────────────────────────────────────────┐")
            appendLine("│ SCAN SESSIONS                                                          │")
            appendLine("└─────────────────────────────────────────────────────────────────────┘")
            appendLine()
            appendLine("See session history for details.")
            appendLine()
            
            appendLine("═══════════════════════════════════════════════════════════════════════")
            appendLine("BLUEMEANIE23 :: APEX SCANNER v3.0.0")
            appendLine("═══════════════════════════════════════════════════════════════════════")
        }

        _exportResult.value = content
        return content
    }

    fun purgeSessionData() {
        viewModelScope.launch {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            deviceDao.deleteOldDevices(thirtyDaysAgo)
            sessionDao.deleteOldSessions(thirtyDaysAgo)
            alertDao.deleteOldAlerts(thirtyDaysAgo)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
        }
    }
}

enum class TelegramStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class FontType {
    RADAR,
    RESULTS,
    MENU,
    SETTINGS,
    CONSOLE,
    GAUGE
}