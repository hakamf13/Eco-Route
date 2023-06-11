package com.capstone.eco_route.datasource.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Track::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TrackerDatabase: RoomDatabase() {

    abstract fun getTrackDao(): TrackDao

}
