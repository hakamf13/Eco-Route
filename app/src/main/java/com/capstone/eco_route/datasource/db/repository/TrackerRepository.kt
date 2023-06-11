package com.capstone.eco_route.datasource.db.repository

import com.capstone.eco_route.datasource.db.Track
import com.capstone.eco_route.datasource.db.TrackDao
import javax.inject.Inject

class TrackerRepository @Inject constructor(
    val trackDao: TrackDao
) {

    suspend fun insertTrack(track: Track) = trackDao.insertTrack(track)

    suspend fun deleteTrack(track: Track) = trackDao.deleteTrack(track)

    fun getAllTrackerSortedByDate() = trackDao.getAllTrackerSortedByDate()

    fun getAllTrackerSortedByTimeInMills() = trackDao.getAllTrackerSortedByTimeInMills()

    fun getAllTrackerSortedByMileageInMeters() = trackDao.getAllTrackerSortedByMileageInMeters()

    fun getAllTrackerSortedByEmissionsReduced() = trackDao.getAllTrackerSortedByEmissionsReduced()

    fun getAllTrackerSortedByAverageSpeed() = trackDao.getAllTrackerSortedByAverageSpeed()

    fun getTotalAverageSpeed() = trackDao.getTotalAverageSpeed()

    fun getTotalMileage() = trackDao.getTotalMileageInMeters()

    fun getTotalEmissionsReduced() = trackDao.getTotalEmissionsReduced()

    fun getTotalTimeInMills() = trackDao.getTotalTimeInMills()

}