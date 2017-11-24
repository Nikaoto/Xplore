package com.xplore.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.MenuItem
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.xplore.R
import com.xplore.base.BaseAppCompatActivity

/**
 * Created by Nika on 8/20/2017.
 * TODO write description of this class - what it does and why.
 */

open class BaseMapActivity : BaseAppCompatActivity(), OnMapReadyCallback {

    private val TAG = "base-map-act"

    open val ZOOM_AMOUNT = 15f
    open val UPDATE_INTERVAL = 5000L
    open val FASTEST_UPDATE_INTERVAL = 1000L
    open val LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY
    open val REQ_CODE_REQUEST_PERMISSION = 1
    open val REQUEST_CHECK_SETTINGS = 0x1


    open lateinit var map: GoogleMap
    // KML
    // private var kmlEnabled = false
    // private var kmlLayer: KmlLayer? = null

    // Location
    private val locationRequest: LocationRequest by lazy {
        LocationRequest().setInterval(UPDATE_INTERVAL).setFastestInterval(FASTEST_UPDATE_INTERVAL)
                .setPriority(LOCATION_PRIORITY)
    }

    private val locationSettingsRequest: LocationSettingsRequest by lazy {
        LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
    }

    private var updatingLocation = false


    // Clients
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private val settingsClient: SettingsClient by lazy {
        LocationServices.getSettingsClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initMap()
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        map = googleMap


        if (permissionsGranted()) {
            configureMap(map)
        } else {
            requestPermissions()
        }

        //configureKmlButton(googleMap)
    }

    private fun permissionsGranted(permission: String = Manifest.permission.ACCESS_FINE_LOCATION)
            = ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    // Does things to map; override this instead of onMapReady
    @SuppressLint("MissingPermission")
    open fun configureMap(googleMap: GoogleMap) {
        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener {
            if (!permissionsGranted()) {
                requestPermissions()
            }
            checkLocationEnabled()
            false
        }
        startLocationUpdates()
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (shouldProvideRationale) {
            //This only happens when user denies request and doesn't check "don't show this again"
            //TODO explain to the user why you need location
        } else {
            //Requests permission
            ActivityCompat.requestPermissions(this@BaseMapActivity,
                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQ_CODE_REQUEST_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_CODE_REQUEST_PERMISSION) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "user interaction cancelled")

                grantResults[0] == PackageManager.PERMISSION_GRANTED -> configureMap(map)

                else -> onBackPressed() //TODO explain here why you need to track their location
            }
        }
    }

    // Does whatever is inside when current location is changed
    open val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                super.onLocationResult(locationResult)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!permissionsGranted()) {
            requestPermissions()
        }
    }

    private fun startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()")

        if (!updatingLocation) {
            Log.i(TAG, "setting updatingLocation to true")
            updatingLocation = true

            checkLocationEnabled()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkLocationEnabled() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener {
                    Log.i(TAG, "all location settings are satisfied; starting location updates")

                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                            Looper.myLooper())
                }
                .addOnFailureListener { e ->
                    val statusCode = (e as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.i(TAG, "location settings not satisfied; attempting to upgrade")

                            try {
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(this@BaseMapActivity,
                                        REQUEST_CHECK_SETTINGS)
                            } catch (sie: IntentSender.SendIntentException) {
                                Log.i(TAG, "PendingIntent unable to execute request")
                            }
                        }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            Log.i(TAG, "location settings can't be fixed; fix in settings")
                        }
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == REQUEST_CHECK_SETTINGS) {
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "user agreed to enable location")

                    startLocationUpdates()
                } else {
                    Log.i(TAG, "user chose not to enable location")
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates()")
        if (updatingLocation) {
            Log.i(TAG, "stopping loc updates")

            fusedLocationClient.removeLocationUpdates(locationCallback)
                    .addOnCompleteListener {
                        updatingLocation = false
                        Log.i(TAG, "stopped loc updates")
                    }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        stopLocationUpdates()
        destroyMap()
    }

    private fun destroyMap() {
        val f = fragmentManager.findFragmentById(R.id.mapFragment)
        if (f != null) {
            fragmentManager.beginTransaction().remove(f).commit()
        }
    }

    // USE ONLY IN DEMO
/*    private fun configureKmlButton(map: GoogleMap) {
        KMLButton.setOnClickListener {
            if (kmlLayer == null) {
                // Add Layer
                kmlLayer = KmlLayer(map, R.raw.testeroni, this)
                kmlLayer?.addLayerToMap()
            } else {
                // Remove Layer
                kmlLayer?.removeLayerFromMap()
                kmlLayer = null
            }
        }
    }*/
}