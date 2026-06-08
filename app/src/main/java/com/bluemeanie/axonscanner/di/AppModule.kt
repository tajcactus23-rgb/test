package com.bluemeanie.axonscanner.di

import android.content.Context
import androidx.room.Room
import com.bluemeanie.axonscanner.data.local.dao.AlertDao
import com.bluemeanie.axonscanner.data.local.dao.DeviceDao
import com.bluemeanie.axonscanner.data.local.dao.ScanSessionDao
import com.bluemeanie.axonscanner.data.local.database.BlueMeanieDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BlueMeanieDatabase {
        return Room.databaseBuilder(
            context,
            BlueMeanieDatabase::class.java,
            BlueMeanieDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideDeviceDao(database: BlueMeanieDatabase): DeviceDao {
        return database.deviceDao()
    }

    @Provides
    @Singleton
    fun provideScanSessionDao(database: BlueMeanieDatabase): ScanSessionDao {
        return database.scanSessionDao()
    }

    @Provides
    @Singleton
    fun provideAlertDao(database: BlueMeanieDatabase): AlertDao {
        return database.alertDao()
    }
}