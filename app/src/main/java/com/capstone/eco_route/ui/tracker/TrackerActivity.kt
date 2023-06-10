package com.capstone.eco_route.ui.tracker

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.capstone.eco_route.R
import com.capstone.eco_route.databinding.ActivityTrackerBinding
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

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private var isTracking = false
    private lateinit var locationCallback: LocationCallback

    private var allLatLng = ArrayList<LatLng>()

    private var boundBuilder = LatLngBounds.Builder()

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

    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK ->
                    Log.i("TAG", "onActivityResult: All location setting are satisfied.")
                RESULT_CANCELED ->
                    Toast.makeText(
                        this,
                        "Set your GPS First",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        mapsView()

    }

/*    @Suppress("DEPRECATION")
    private fun mapsView() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }



    }*/

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        getLocation()
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

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun checkPermit(permit: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permit
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLatestLocation() {
        if (checkPermit(Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermit(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    showStartMarker(location)
                } else {
                    //
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
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
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