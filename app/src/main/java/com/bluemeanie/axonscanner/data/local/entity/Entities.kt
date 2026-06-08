package com.bluemeanie.axonscanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bluemeanie.axonscanner.data.local.database.Converters

@Entity(tableName = "devices")
@TypeConverters(Converters::class)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val macAddress: String,
    val name: String?,
    val oui: String?,
    val rssiHistory: List<Int>,
    val firstSeen: Long,
    val lastSeen: Long,
    val detectionCount: Int,
    val isAxon: Boolean,
    val confidence: Int,
    val isThreat: Boolean,
    val notes: String?
)

@Entity(tableName = "scan_sessions")
data class ScanSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val durationSec: Int,
    val devicesFound: Int,
    val axonHits: Int,
    val scanMode: String,
    val locationLat: Double?,
    val locationLng: Double?
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceId: Long,
    val timestamp: Long,
    val rssi: Int,
    val distanceEstimate: String?,
    val type: String
)