package com.bluemeanie.axonscanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BlueMeanieApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Scanner Channel - Foreground service
            val scannerChannel = NotificationChannel(
                CHANNEL_SCANNER,
                getString(R.string.notif_channel_scanner),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent scanner notification"
                setShowBadge(false)
            }

            // Alerts Channel - Detection alerts
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                getString(R.string.notif_channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Detection alerts and threat notifications"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(scannerChannel, alertsChannel))
        }
    }

    companion object {
        const val CHANNEL_SCANNER = "scanner_channel"
        const val CHANNEL_ALERTS = "alerts_channel"
    }
}