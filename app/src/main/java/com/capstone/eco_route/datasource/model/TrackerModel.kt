package com.capstone.eco_route.datasource.model

import android.graphics.Bitmap
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TrackerModel(

    var image: Bitmap? = null,
    val idUser: String?= null,
    val carType: String? = null,
    val carYear: String? = null,
    val carFuel: String? = null,
    val mileageInMeters: Float = 0.0F,
    val date: String? = null,
    val carbonEmissions: Float = 0.0F

) {

    @PrimaryKey(autoGenerate = true)
    val idTrip: Int? = null
}