package com.bluemeanie.axonscanner.data.repository

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import com.bluemeanie.axonscanner.domain.model.DeviceType
import com.bluemeanie.axonscanner.domain.model.ScanMode
import com.bluemeanie.axonscanner.domain.model.ScannedDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null

    // Axon OUI prefixes
    private val axonOuiPrefixes = listOf(
        "00:25:DF",  // Primary Axon
        "FC:A9:E8",  // Secondary Axon
        "A4:34:D9",  // Axon Flex
        "00:1A:7D",  // Axon Body
        "F4:5E:AB"   // Axon Camera
    )

    // Axon device name patterns
    private val axonNamePatterns = listOf(
        Pattern.compile("(?i)AXON", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)AXON_BODY", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)AXON_CAMERA", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)TASER", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)AXON_FLEX", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)EVIDENCE", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)BODYCAM", Pattern.CASE_INSENSITIVE)
    )

    // Known Axon Service UUIDs
    private val axonServiceUuids = listOf(
        "0000FE00-0000-1000-8000-00805F9B34FB",  // Axon GATT
        "6E400001-B5A3-F393-E0A9-E50E24DCCA9E",  // Nordic UART
        "00001800-0000-1000-8000-00805F9B34FB",  // Generic Access
        "00001801-0000-1000-8000-00805F9B34FB"   // Generic Attribute
    )

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    val isBluetoothSupported: Boolean
        get() = bluetoothAdapter != null

    @SuppressLint("MissingPermission")
    fun getBondedDevices(): Set<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices ?: emptySet()
    }

    @SuppressLint("MissingPermission")
    fun startScan(scanMode: ScanMode): Flow<ScannedDevice> = callbackFlow {
        scanner = bluetoothAdapter?.bluetoothLeScanner

        val settings = when (scanMode) {
            ScanMode.TACTICAL -> ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()
            ScanMode.STEALTH -> ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(5000)
                .build()
            ScanMode.SWEEP -> ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setReportDelay(1000)
                .build()
        }

        val filters = listOf(
            ScanFilter.Builder()
                .setDeviceAddress("00:25:DF:*")  // Axon primary
                .build(),
            ScanFilter.Builder()
                .setDeviceAddress("FC:A9:E8:*")  // Axon secondary
                .build()
        )

        scanCallback = object : ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = parseScanResult(result)
                trySend(device)
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                results.forEach { result ->
                    val device = parseScanResult(result)
                    trySend(device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                close(ScanException("BLE scan failed with error code: $errorCode"))
            }
        }

        try {
            scanner?.startScan(null, settings, scanCallback)
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanCallback?.let { callback ->
            try {
                scanner?.stopScan(callback)
            } catch (e: Exception) {
                // Ignore
            }
        }
        scanCallback = null
        scanner = null
    }

    @SuppressLint("MissingPermission")
    private fun parseScanResult(result: ScanResult): ScannedDevice {
        val device = result.device
        val macAddress = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            device.address
        } else {
            // Android 11+ randomizes MAC, but we still get the actual address for paired devices
            device.address
        }
        
        val name = device.name ?: result.scanRecord?.deviceName
        val rssi = result.rssi
        val txPower = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result.scanRecord?.txPowerLevel
        } else null

        val scanRecord = result.scanRecord
        val serviceUuids = scanRecord?.serviceUuids?.map { it.uuid.toString() } ?: emptyList()
        val manufacturerData = parseManufacturerData(scanRecord)
        
        // Extract OUI from MAC address
        val oui = extractOui(macAddress)
        
        // Detect if this is an Axon device
        val (isAxon, confidence, deviceType) = detectAxonDevice(
            macAddress = macAddress,
            name = name,
            oui = oui,
            serviceUuids = serviceUuids,
            manufacturerData = manufacturerData
        )

        return ScannedDevice(
            macAddress = macAddress,
            name = name,
            oui = oui,
            rssi = rssi,
            rssiHistory = listOf(rssi),
            isAxon = isAxon,
            confidence = confidence,
            deviceType = deviceType,
            txPower = txPower,
            serviceUuids = serviceUuids
        )
    }

    private fun parseManufacturerData(scanRecord: ScanRecord?): Map<Int, ByteArray> {
        val manufacturerData = mutableMapOf<Int, ByteArray>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && scanRecord != null) {
            scanRecord.manufacturerSpecificData?.let { data ->
                for (i in 0 until data.size()) {
                    val manufacturerId = data.keyAt(i)
                    val companyData = data.valueAt(i)
                    if (companyData != null) {
                        manufacturerData[manufacturerId] = companyData
                    }
                }
            }
        }
        return manufacturerData
    }

    private fun extractOui(macAddress: String): String? {
        return try {
            val parts = macAddress.split(":")
            if (parts.size >= 3) {
                "${parts[0]}:${parts[1]}:${parts[2]}".uppercase()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun detectAxonDevice(
        macAddress: String,
        name: String?,
        oui: String?,
        serviceUuids: List<String>,
        manufacturerData: Map<Int, ByteArray>
    ): Triple<Boolean, Int, DeviceType> {
        var totalConfidence = 0
        var deviceType = DeviceType.UNKNOWN

        // Check OUI
        oui?.let { ouiValue ->
            if (axonOuiPrefixes.any { ouiValue.startsWith(it) }) {
                totalConfidence += 40
            }
        }

        // Check device name
        name?.let { deviceName ->
            if (axonNamePatterns.any { it.matcher(deviceName).find() }) {
                totalConfidence += 35
            }
            
            // Determine device type
            when {
                deviceName.contains("BODY", ignoreCase = true) || 
                deviceName.contains("BODYCAM", ignoreCase = true) -> {
                    deviceType = DeviceType.BODY_CAM
                }
                deviceName.contains("TASER", ignoreCase = true) -> {
                    deviceType = DeviceType.TASER
                }
                deviceName.contains("FLEX", ignoreCase = true) -> {
                    deviceType = DeviceType.FLEX
                }
            }
        }

        // Check service UUIDs
        val matchingUuids = serviceUuids.count { uuid ->
            axonServiceUuids.any { axUuid -> uuid.equals(axUuid, ignoreCase = true) }
        }
        if (matchingUuids > 0) {
            totalConfidence += 25
        }

        // Check manufacturer data (Axon uses specific company IDs)
        val axonCompanyIds = listOf(0x00, 0x25, 0xDF, 0xFC, 0xA9, 0xE8)
        if (manufacturerData.keys.any { it in axonCompanyIds }) {
            totalConfidence += 20
        }

        val isAxon = totalConfidence >= 40
        return Triple(isAxon, totalConfidence.coerceIn(0, 100), deviceType)
    }
}

class ScanException(message: String) : Exception(message)
