package com.capstone.eco_route.ui.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
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
import com.capstone.eco_route.ui.detailtracker.DetailTrackerActivity
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
import java.util.concurrent.TimeUnit

class TrackerActivity : AppCompatActivity(), OnMapReadyCallback {

    private val binding: ActivityTrackerBinding by lazy {
        ActivityTrackerBinding.inflate(layoutInflater)
    }

//    private val viewModel: TrackerViewModel by viewModels()

//    private var mMap: GoogleMap? =  null
/*

    private var isTracking = false
    private var trackPathPoints = mutableListOf<Polyline>()



*/

/*
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    */

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isTracking = false

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var allLatLng = ArrayList<LatLng>()

    private var boundBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        requestPermissionsLauncher()

        binding.buttonEndTrack.setOnClickListener {
            val intent = Intent(
                this@TrackerActivity,
                DetailTrackerActivity::class.java
            )
            startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /*binding.buttonStartTrack.setOnClickListener {
            startTracking()
        }*/

    }

    /*override fun onResume() {
        super.onResume()
    }

    private fun trackObserver() {

        TrackerService.isTracking.observe(this) {
            updateTrackingStatus(it)
        }

        TrackerService.trackPathPoints.observe(this) {
            trackPathPoints = it
            createLatestPolyline()
            moveCameraZoom()
        }
    }

    private fun startTracking() {
        if (isTracking) {
            sendCommandToService(PAUSE_ACTION_SERVICE)
        } else {
            sendCommandToService(START_OR_RESUME_ACTION_SERVICE)
        }
    }

    private fun updateTrackingStatus(status: Boolean) {
        this.isTracking = status
        if (!isTracking) {
            binding.buttonStartTrack.text = "Start"
            binding.buttonEndTrack.visibility = View.VISIBLE
        } else {
            binding.buttonStartTrack.text = "Stop"
            binding.buttonEndTrack.visibility = View.GONE
        }
    }

    private fun moveCameraZoom() {
        if (trackPathPoints.isNotEmpty() && trackPathPoints.last().isNotEmpty()) {
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    trackPathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun createAllPolyline() {
        for (polyline in trackPathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
            mMap?.addPolyline(polylineOptions)
        }
    }

    private fun createLatestPolyline() {
        if (trackPathPoints.isNotEmpty() && trackPathPoints.last().size > 1) {
            val preLastLatLng = trackPathPoints.last()[trackPathPoints.last().size - 2]
            val lastLatLng = trackPathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            mMap?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(command: String) =
        Intent(
            this@TrackerActivity,
            TrackerService::class.java
        ).also {
            it.action = command
            this.startService(it)
        }*/

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap!!.uiSettings.isZoomControlsEnabled = true

        /*trackObserver()
        createAllPolyline()*/

        getLatestLocation()
        createLocationRequest()
        createLocationCallback()

        binding.buttonStartTrack.setOnClickListener {
            if (!isTracking) {
                clearMaps()
                updateTrackingStatus(true)
                startTrackingUpdates()
            } else {
                updateTrackingStatus(false)
                stopTrackingUpdates()
            }
        }
    }

    /*private fun requestPermissionsLauncher() {
        if (TrackerUtility.locationPermissionsProvided(this)) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You should accept location permissions to use this app",
                PERMISSIONS_CODE_REQUEST_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You should accept location permissions to use this app",
                PERMISSIONS_CODE_REQUEST_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(
                this,
                perms
        )) {
            AppSettingsDialog.Builder(
                this
            ).build().show()
        } else {
            requestPermissionsLauncher()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions, grantResults, this)
    }*/



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
                            "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
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

        private fun clearMaps() {
            mMap.clear()
            allLatLng.clear()
            boundBuilder = LatLngBounds.Builder()
        }

}