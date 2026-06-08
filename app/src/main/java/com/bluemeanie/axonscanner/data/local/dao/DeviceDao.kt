package com.bluemeanie.axonscanner.data.local.dao

import androidx.room.*
import com.bluemeanie.axonscanner.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY lastSeen DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE isAxon = 1 ORDER BY lastSeen DESC")
    fun getAxonDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE isThreat = 1 ORDER BY lastSeen DESC")
    fun getThreatDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE macAddress = :mac LIMIT 1")
    suspend fun getDeviceByMac(macAddress: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getDeviceById(id: Long): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity): Long

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Delete
    suspend fun deleteDevice(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE lastSeen < :timestamp")
    suspend fun deleteOldDevices(timestamp: Long)

    @Query("SELECT COUNT(*) FROM devices")
    fun getDeviceCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM devices WHERE isAxon = 1")
    fun getAxonCount(): Flow<Int>
}