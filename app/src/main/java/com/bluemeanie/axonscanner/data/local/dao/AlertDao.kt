package com.bluemeanie.axonscanner.data.local.dao

import androidx.room.*
import com.bluemeanie.axonscanner.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun getAlertsForDevice(deviceId: Long): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE timestamp > :since ORDER BY timestamp DESC")
    fun getAlertsSince(since: Long): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Delete
    suspend fun deleteAlert(alert: AlertEntity)

    @Query("DELETE FROM alerts WHERE timestamp < :timestamp")
    suspend fun deleteOldAlerts(timestamp: Long)

    @Query("SELECT COUNT(*) FROM alerts")
    fun getAlertCount(): Flow<Int>
}