package com.bluemeanie.axonscanner.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bluemeanie.axonscanner.BlueMeanieApp
import com.bluemeanie.axonscanner.R
import com.bluemeanie.axonscanner.data.repository.BleRepository
import com.bluemeanie.axonscanner.domain.model.ScanMode
import com.bluemeanie.axonscanner.domain.model.ScannedDevice
import com.bluemeanie.axonscanner.presentation.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@AndroidEntryPoint
class BleScannerService : Service() {

    @Inject
    lateinit var bleRepository: BleRepository

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var scanJob: Job? = null
    
    private val _deviceFlow = MutableSharedFlow<ScannedDevice>(extraBufferCapacity = 100)
    val deviceFlow: SharedFlow<ScannedDevice> = _deviceFlow
    
    private val _scanState = MutableStateFlow(false)
    val scanState: SharedFlow<Boolean> = _scanState
    
    private var currentScanMode: ScanMode = ScanMode.TACTICAL

    inner class LocalBinder : Binder() {
        fun getService(): BleScannerService = this@BleScannerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SCAN -> {
                val mode = intent.getStringExtra(EXTRA_SCAN_MODE)?.let {
                    ScanMode.valueOf(it)
                } ?: ScanMode.TACTICAL
                startScanning(mode)
            }
            ACTION_STOP_SCAN -> stopScanning()
        }
        return START_STICKY
    }

    private fun startScanning(mode: ScanMode) {
        currentScanMode = mode
        
        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        scanJob?.cancel()
        scanJob = serviceScope.launch {
            _scanState.emit(true)
            try {
                bleRepository.startScan(mode).collect { device ->
                    _deviceFlow.emit(device)
                }
            } catch (e: Exception) {
                _scanState.emit(false)
            }
        }
    }

    private fun stopScanning() {
        scanJob?.cancel()
        serviceScope.launch {
            bleRepository.stopScan()
            _scanState.emit(false)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, BleScannerService::class.java).apply {
                action = ACTION_STOP_SCAN
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, BlueMeanieApp.CHANNEL_SCANNER)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notif_scanning))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "STOP", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanJob?.cancel()
        serviceScope.cancel()
        bleRepository.stopScan()
    }

    companion object {
        const val ACTION_START_SCAN = "com.bluemeanie.axonscanner.START_SCAN"
        const val ACTION_STOP_SCAN = "com.bluemeanie.axonscanner.STOP_SCAN"
        const val EXTRA_SCAN_MODE = "scan_mode"
        const val NOTIFICATION_ID = 1001
    }
}