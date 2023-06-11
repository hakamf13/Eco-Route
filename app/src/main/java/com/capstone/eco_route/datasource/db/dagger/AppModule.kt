package com.capstone.eco_route.datasource.db.dagger

import android.content.Context
import androidx.room.Room
import com.capstone.eco_route.datasource.db.TrackerDatabase
import com.capstone.eco_route.datasource.db.other.ContantsToken.TRACKER_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTrackerDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        TrackerDatabase::class.java,
        TRACKER_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideTrackDao(db: TrackerDatabase) = db.getTrackDao()

}
