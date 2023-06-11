package com.capstone.eco_route.datasource.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_table")
data class Track(
    var image: Bitmap? = null,
    var timeStamp: Long = 0L,
    var averageSpeed: Float = 0f,
    var mileageInMeters: Int = 0,
    var timeInMills: Long = 0L,
    var emissionsReduced: Int = 0
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

}
