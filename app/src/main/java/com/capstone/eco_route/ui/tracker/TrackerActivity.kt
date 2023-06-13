package com.capstone.eco_route.ui.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.capstone.eco_route.R
import com.capstone.eco_route.databinding.ActivityTrackerBinding
import com.capstone.eco_route.datasource.adapter.TrackAdapter
import com.capstone.eco_route.ui.detailtracker.DetailTrackerActivity
import com.capstone.eco_route.utils.TimerService
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class TrackerActivity : AppCompatActivity(), OnMapReadyCallback {

    private val binding: ActivityTrackerBinding by lazy {
        ActivityTrackerBinding.inflate(layoutInflater)
    }

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isTracking = false

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var allLatLng = ArrayList<LatLng>()

    private var boundBuilder = LatLngBounds.Builder()

    private var isTimeStarted = false
    private var startTime = 0.0
    private lateinit var serviceIntent: Intent

    private lateinit var database: FirebaseDatabase

    private lateinit var trackAdapter: TrackAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupView()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.buttonEndTrack.setOnClickListener {

            val intent = Intent(
                this@TrackerActivity,
                DetailTrackerActivity::class.java
            )
            startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        database = Firebase.database

        database.reference.child(CHILD_MESSAGE)
    }

    private fun setupView() = binding.trackerLayout.apply {
        trackAdapter = TrackAdapter()
        setHasTransientState(true)
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            startTime = intent.getDoubleExtra(TimerService.TIME_EXTRA, startTime)
            binding.tripTimeValue.text = getTimeStringFromDouble(startTime)
        }

    }

    private fun getTimeStringFromDouble(time: Double): String {

        val timeResult = time.roundToInt()
        val timeInHours = timeResult % 86400 / 3600
        val timeInMinutes = timeResult % 86400 % 3600 / 60
        val timeInSeconds = timeResult % 86400 % 3600 % 60

        return convertTimeToString(timeInHours, timeInMinutes, timeInSeconds)
    }

    private fun convertTimeToString(
        hours: Int,
        mins: Int,
        secs: Int
    ): String = String.format("%02d:%02d:%02d", hours, mins, secs)

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        getLatestLocation()
        createLocationRequest()
        createLocationCallback()

        binding.buttonStartTrack.setOnClickListener {
            if (!isTracking) {
                toggleStopwatch()
                updateTrackingStatus(true)
                startTrackingUpdates()
            } else {
                resetStopwatch()
                updateTrackingStatus(false)
                stopTrackingUpdates()
            }
        }
    }

    private fun toggleStopwatch() {
        if (!isTimeStarted) {
            startStopwatch()
        } else {
            endStopwatch()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun endStopwatch() {

        stopService(serviceIntent)
        binding.buttonStartTrack.text = "Start"
        isTimeStarted = false

    }

    @SuppressLint("SetTextI18n")
    private fun startStopwatch() {

        serviceIntent.putExtra(TimerService.TIME_EXTRA, startTime)
        startService(serviceIntent)
        binding.buttonStartTrack.text = "Stop"
        isTimeStarted = true

    }

    private fun resetStopwatch() {
        endStopwatch()
        startTime = 0.0
        binding.tripTimeValue.text = getTimeStringFromDouble(startTime)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permit ->
            when {
                permit[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getLatestLocation()
                }
                permit[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getLatestLocation()
                }
                else -> {
                    // Do Nothing
                }
            }
        }


    private fun checkPermit(permit: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permit
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLatestLocation() {

        if (checkPermit(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    showStartMarker(location)
                } else {
                    Toast.makeText(
                        this@TrackerActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    private fun showStartMarker(location: Location) {
        val startLocation = LatLng(location.latitude, location.longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(startLocation)
                .title(getString(R.string.start_point))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 17f))
    }

    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK ->
                    Log.i("TAG", "onActivityResult: All location settings are satisfied.")
                RESULT_CANCELED ->
                    Toast.makeText(
                        this@TrackerActivity,
                        "You should accept the location permissions to use this app",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    @Suppress("DEPRECATION")
    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getLatestLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(this@TrackerActivity, sendEx.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun updateTrackingStatus(currentStatus: Boolean) {
        isTracking = currentStatus
        if (isTracking) {
            binding.buttonStartTrack.text = getString(R.string.stop_running)
        } else {
            binding.buttonStartTrack.text = getString(R.string.start_running)
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d("TAG", "onLocationResult: " + location.latitude + ", " + location.longitude)
                    val lastLatLng = LatLng(location.latitude, location.longitude)

                    // polyline
                    allLatLng.add(lastLatLng)
                    mMap.addPolyline(
                        PolylineOptions()
                            .color(Color.CYAN)
                            .width(10f)
                            .addAll(allLatLng)
                    )

                    // boundaries
                    boundBuilder.include(lastLatLng)
                    val bounds: LatLngBounds = boundBuilder.build()
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64))
                }
            }
        }
    }

    private fun startTrackingUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            Log.e("TAG", "Error: " + exception.message)
        }
    }

    private fun stopTrackingUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (isTracking) {
            startTrackingUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopTrackingUpdates()
    }

    companion object {

        private const val CHILD_MESSAGE = "message"

    }

}