package com.bluemeanie.axonscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemeanie.axonscanner.data.local.dao.AlertDao
import com.bluemeanie.axonscanner.data.local.dao.DeviceDao
import com.bluemeanie.axonscanner.data.local.dao.ScanSessionDao
import com.bluemeanie.axonscanner.data.local.entity.AlertEntity
import com.bluemeanie.axonscanner.data.local.entity.DeviceEntity
import com.bluemeanie.axonscanner.data.local.entity.ScanSessionEntity
import com.bluemeanie.axonscanner.data.repository.BleRepository
import com.bluemeanie.axonscanner.data.repository.SettingsRepository
import com.bluemeanie.axonscanner.domain.model.*
import com.bluemeanie.axonscanner.util.AudioEngine
import com.bluemeanie.axonscanner.util.VibrationPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RadarViewModel @Inject constructor(
    private val bleRepository: BleRepository,
    private val settingsRepository: SettingsRepository,
    private val deviceDao: DeviceDao,
    private val sessionDao: ScanSessionDao,
    private val alertDao: AlertDao,
    private val audioEngine: AudioEngine
) : ViewModel() {

    private val _radarState = MutableStateFlow(RadarState())
    val radarState: StateFlow<RadarState> = _radarState.asStateFlow()

    private val _devices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val devices: StateFlow<List<ScannedDevice>> = _devices.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _callsign = MutableStateFlow("OPERATOR")
    val callsign: StateFlow<String> = _callsign.asStateFlow()

    private val _showAxonAlert = MutableStateFlow<ScannedDevice?>(null)
    val showAxonAlert: StateFlow<ScannedDevice?> = _showAxonAlert.asStateFlow()

    private var scanJob: Job? = null
    private var scanTimerJob: Job? = null
    private var currentSessionId: Long? = null
    private var scanStartTime: Long = 0

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _settings.value = settings
            }
        }
        viewModelScope.launch {
            settingsRepository.callsign.collect { callsign ->
                _callsign.value = callsign
            }
        }
        viewModelScope.launch {
            deviceDao.getAllDevices().collect { entities ->
                _devices.value = entities.map { it.toScannedDevice() }
            }
        }
    }

    val isBluetoothEnabled: Boolean
        get() = bleRepository.isBluetoothEnabled

    val isBluetoothSupported: Boolean
        get() = bleRepository.isBluetoothSupported

    fun startScan() {
        if (_radarState.value.isScanning) return

        viewModelScope.launch {
            _radarState.update { it.copy(isScanning = true, axonHits = 0, peakRssi = -100) }
            scanStartTime = System.currentTimeMillis()
            
            // Create new session
            val session = ScanSessionEntity(
                startTime = scanStartTime,
                scanMode = _settings.value.scanMode.name,
                devicesFound = 0,
                axonHits = 0,
                durationSec = 0
            )
            currentSessionId = sessionDao.insertSession(session)

            // Play start sounds
            if (_settings.value.soundEnabled) {
                audioEngine.playScanStart()
            }
            if (_settings.value.vibrationEnabled) {
                audioEngine.vibrate(VibrationPattern.START)
            }

            // Start scan timer
            scanTimerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000)
                    val duration = ((System.currentTimeMillis() - scanStartTime) / 1000).toInt()
                    _radarState.update { it.copy(scanDuration = duration * 1000L) }
                }
            }

            // Start BLE scanning
            scanJob = viewModelScope.launch {
                try {
                    bleRepository.startScan(_settings.value.scanMode).collect { device ->
                        handleDeviceDetected(device)
                    }
                } catch (e: Exception) {
                    _radarState.update { it.copy(isScanning = false) }
                }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanTimerJob?.cancel()
        
        viewModelScope.launch {
            // Play stop sounds
            if (_settings.value.soundEnabled) {
                audioEngine.playScanStop()
            }

            // Update session
            currentSessionId?.let { sessionId ->
                sessionDao.getSessionById(sessionId)?.let { session ->
                    val updatedSession = session.copy(
                        endTime = System.currentTimeMillis(),
                        durationSec = ((System.currentTimeMillis() - scanStartTime) / 1000).toInt(),
                        devicesFound = _radarState.value.devices.size,
                        axonHits = _radarState.value.axonHits
                    )
                    sessionDao.updateSession(updatedSession)
                }
            }

            _radarState.update { it.copy(isScanning = false, currentSession = null) }
        }
    }

    private suspend fun handleDeviceDetected(device: ScannedDevice) {
        // Update peak RSSI
        if (device.rssi > _radarState.value.peakRssi) {
            _radarState.update { it.copy(peakRssi = device.rssi) }
        }

        // Check if Axon
        if (device.isAxon) {
            _radarState.update { it.copy(axonHits = it.axonHits + 1) }
            
            // Play axon alert
            if (_settings.value.soundEnabled) {
                audioEngine.playAxonAlert()
            }
            if (_settings.value.vibrationEnabled) {
                audioEngine.vibrate(VibrationPattern.AXON)
            }
            
            // Show fullscreen alert
            if (_settings.value.fullscreenAlertEnabled) {
                _showAxonAlert.value = device
                delay(5000)
                _showAxonAlert.value = null
            }

            // Save alert
            val alert = AlertEntity(
                deviceId = device.id,
                timestamp = System.currentTimeMillis(),
                rssi = device.rssi,
                type = AlertType.AXON_DETECTED.name
            )
            alertDao.insertAlert(alert)
        }

        // Save/update device in database
        val existingDevice = deviceDao.getDeviceByMac(device.macAddress)
        if (existingDevice != null) {
            val updatedDevice = existingDevice.copy(
                rssiHistory = existingDevice.rssiHistory + device.rssi,
                lastSeen = System.currentTimeMillis(),
                detectionCount = existingDevice.detectionCount + 1,
                isAxon = existingDevice.isAxon || device.isAxon,
                confidence = maxOf(existingDevice.confidence, device.confidence)
            )
            deviceDao.updateDevice(updatedDevice)
        } else {
            val entity = device.toEntity()
            deviceDao.insertDevice(entity)
        }
    }

    fun setScanMode(mode: ScanMode) {
        viewModelScope.launch {
            settingsRepository.updateScanMode(mode)
        }
    }

    fun markAsThreat(device: ScannedDevice, isThreat: Boolean) {
        viewModelScope.launch {
            val entity = deviceDao.getDeviceByMac(device.macAddress)
            entity?.let {
                deviceDao.updateDevice(it.copy(isThreat = isThreat))
            }
        }
    }

    fun dismissAxonAlert() {
        _showAxonAlert.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
        audioEngine.release()
    }

    private fun DeviceEntity.toScannedDevice() = ScannedDevice(
        id = id,
        macAddress = macAddress,
        name = name,
        oui = oui,
        rssiHistory = rssiHistory,
        firstSeen = firstSeen,
        lastSeen = lastSeen,
        detectionCount = detectionCount,
        isAxon = isAxon,
        confidence = confidence,
        isThreat = isThreat,
        notes = notes,
        rssi = rssiHistory.lastOrNull() ?: -100
    )

    private fun ScannedDevice.toEntity() = DeviceEntity(
        macAddress = macAddress,
        name = name,
        oui = oui,
        rssiHistory = rssiHistory,
        firstSeen = firstSeen,
        lastSeen = lastSeen,
        detectionCount = detectionCount,
        isAxon = isAxon,
        confidence = confidence,
        isThreat = isThreat,
        notes = notes
    )
}