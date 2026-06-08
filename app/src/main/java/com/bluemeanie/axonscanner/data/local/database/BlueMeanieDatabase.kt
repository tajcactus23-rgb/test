package com.bluemeanie.axonscanner.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bluemeanie.axonscanner.data.local.dao.AlertDao
import com.bluemeanie.axonscanner.data.local.dao.DeviceDao
import com.bluemeanie.axonscanner.data.local.dao.ScanSessionDao
import com.bluemeanie.axonscanner.data.local.entity.AlertEntity
import com.bluemeanie.axonscanner.data.local.entity.DeviceEntity
import com.bluemeanie.axonscanner.data.local.entity.ScanSessionEntity

@Database(
    entities = [
        DeviceEntity::class,
        ScanSessionEntity::class,
        AlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BlueMeanieDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun scanSessionDao(): ScanSessionDao
    abstract fun alertDao(): AlertDao

    companion object {
        const val DATABASE_NAME = "bluemeanie_db"
    }
}