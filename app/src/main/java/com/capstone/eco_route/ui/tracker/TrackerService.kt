package com.capstone.eco_route.ui.tracker

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.capstone.eco_route.R
import com.capstone.eco_route.datasource.db.other.ContantsToken.CHANNEL_NOTIFICATION_ID
import com.capstone.eco_route.datasource.db.other.ContantsToken.CHANNEL_NOTIFICATION_NAME
import com.capstone.eco_route.datasource.db.other.ContantsToken.FASTEST_LOCATION_UPDATE_INTERVAL
import com.capstone.eco_route.datasource.db.other.ContantsToken.NORMAL_LOCATION_UPDATE_INTERVAL
import com.capstone.eco_route.datasource.db.other.ContantsToken.NOTIFICATION_ID
import com.capstone.eco_route.datasource.db.other.ContantsToken.PAUSE_ACTION_SERVICE
import com.capstone.eco_route.datasource.db.other.ContantsToken.START_OR_RESUME_ACTION_SERVICE
import com.capstone.eco_route.datasource.db.other.ContantsToken.STOP_ACTION_SERVICE
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@Suppress("DEPRECATION")
class TrackerService: LifecycleService() {

    private var isFirstTrack = true

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        trackPostInitialValues()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(
            this@TrackerService
        ) {
            updateLocationTrackingStatus(it)
        }
    }

    @SuppressLint("LogNotTimber")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {

                START_OR_RESUME_ACTION_SERVICE -> {
                    if (isFirstTrack) {
                        startForegroundService()
                        isFirstTrack = false
                    } else {
                        Log.d("RESUME_SERVICE","Service has been Resumed")
                        startForegroundService()
                    }
                }

                PAUSE_ACTION_SERVICE -> {
                    Log.d("PAUSED_SERVICE","Service has been Paused")
                    pauseActionService()
                }

                STOP_ACTION_SERVICE -> {
                    Log.d("STOPPED_SERVICE","Service has been Stopped")
                }

                else -> {

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseActionService() {
        isTracking.postValue(false)
    }

    private fun startForegroundService() {

        createEmptyPolyline()
        isTracking.postValue(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            createNotificationAppsChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(
            this,
            CHANNEL_NOTIFICATION_ID
        )
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.cloud)
            .setContentTitle("Eco-Route")
            .setContentText("00:00:00")
/*            .setContentIntent(getTrackerPendingIntent())*/

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    /*private fun getTrackerPendingIntent() = PendingIntent.getActivity(
        this@TrackerService,
        0,
        Intent(
            this@TrackerService,
            TrackerActivity::class.java
        ).also {
            it.action = SHOW_ACTION_TRACKER_ACTIVITY
        },
        FLAG_UPDATE_CURRENT or FLAG_MUTABLE
    )*/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationAppsChannel(notificationManager: NotificationManager) {
        val notificationAppsChannel = NotificationChannel(
            CHANNEL_NOTIFICATION_ID,
            CHANNEL_NOTIFICATION_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(notificationAppsChannel)
    }

    private fun trackPostInitialValues() {
        isTracking.postValue(false)
        trackPathPoints.postValue(mutableListOf())
    }

    private fun createEmptyPolyline() = trackPathPoints.value?.apply {
        add(mutableListOf())
        trackPathPoints.postValue(this)
    } ?: trackPathPoints.postValue(mutableListOf(mutableListOf()))



    @SuppressLint("MissingPermission")
    private fun updateLocationTrackingStatus(isTracking: Boolean) {
        if (isTracking) {
            if (TrackerUtility.locationPermissionsProvided(this)) {
                val locationRequest = LocationRequest().apply {
                    interval = NORMAL_LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        @SuppressLint("LogNotTimber")
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (isTracking.value!!) {
                locationResult.locations.let { locations ->
                    for (location in locationResult.locations) {
                        createTrackPathPoints(location)
                        /*Timber.d("Update Location: ${location.latitude}, ${location.longitude}")*/
                        Log.d("Callback", "Update Location: " + location.latitude + ", " + location.longitude)
                    }
                }
            }
        }
    }

    private fun createTrackPathPoints(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            trackPathPoints.value?.apply {
                last().add(position)
                trackPathPoints.postValue(this)
            }
        }
    }

    companion object {

        val isTracking = MutableLiveData<Boolean>()
        val trackPathPoints = MutableLiveData<Polylines>()

    }

}