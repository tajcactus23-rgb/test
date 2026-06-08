package com.bluemeanie.axonscanner.data.local.dao

import androidx.room.*
import com.bluemeanie.axonscanner.data.local.entity.ScanSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanSessionDao {
    @Query("SELECT * FROM scan_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ScanSessionEntity>>

    @Query("SELECT * FROM scan_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): ScanSessionEntity?

    @Query("SELECT * FROM scan_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): ScanSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScanSessionEntity): Long

    @Update
    suspend fun updateSession(session: ScanSessionEntity)

    @Delete
    suspend fun deleteSession(session: ScanSessionEntity)

    @Query("DELETE FROM scan_sessions WHERE startTime < :timestamp")
    suspend fun deleteOldSessions(timestamp: Long)

    @Query("SELECT SUM(durationSec) FROM scan_sessions")
    fun getTotalScanTime(): Flow<Int?>

    @Query("SELECT SUM(axonHits) FROM scan_sessions")
    fun getTotalAxonHits(): Flow<Int?>
}