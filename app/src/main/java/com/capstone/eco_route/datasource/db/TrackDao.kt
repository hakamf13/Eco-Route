package com.capstone.eco_route.datasource.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Delete
    suspend fun deleteTrack(track: Track)

    @Query("""
        SELECT * FROM track_table
        ORDER BY
        CASE WHEN :column = 'timeStamp' THEN timeStamp END DESC,
        CASE WHEN :column = 'timeInMills' THEN timeInMills END DESC,
        CASE WHEN :column = 'emissionReduced' THEN emissionsReduced END DESC,
        CASE WHEN :column = 'averageSpeed' THEN averageSpeed END DESC,
        CASE WHEN :column = 'mileageInMeters' THEN mileageInMeters END DESC
    """)
    suspend fun filterBy(column: String): LiveData<List<Track>>

    @Query("SELECT SUM(timeInMills) FROM track_table")
    fun getTotalTimeInMills(): LiveData<Long>

    @Query("SELECT SUM(emissionsReduced) FROM track_table")
    fun getTotalEmissionsReduced(): LiveData<Long>

    @Query("SELECT SUM(mileageInMeters) FROM track_table")
    fun getTotalMileageInMeres(): LiveData<Long>

    @Query("SELECT AVG(averageSpeed) FROM track_table")
    fun getTotalAverageSpeed(): LiveData<Float>

}