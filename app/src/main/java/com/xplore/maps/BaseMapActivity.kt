package com.xplore.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
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
    open val REQ_CODE_REQUEST_PERMISSION = 2
    open val REQUEST_CHECK_SETTINGS = 0x1

    open lateinit var map: GoogleMap
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

    private val settingsClient: SettingsClient by lazy {
        LocationServices.getSettingsClient(this)
    }

    open val layoutId = R.layout.activity_maps
    open val mapFragmentId = R.id.mapFragment
    open val titleId = R.string.activity_maps_title

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup Layout
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(layoutId)
        setTitle(titleId)

        // Init map
        val mapFragment = supportFragmentManager.findFragmentById(mapFragmentId) as SupportMapFragment
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
        checkLocationEnabled()
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

    override fun onResume() {
        super.onResume()
        if (!permissionsGranted()) {
            requestPermissions()
        }
    }

    // Requests that the user turns on location services
    private fun checkLocationEnabled() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener {
                    Log.i(TAG, "all location settings are satisfied; starting location updates")

                    onStartLocationUpdates()
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

                    checkLocationEnabled()
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

    open fun onStartLocationUpdates() {
        Log.i(TAG, "onStartLocationUpdates()")
    }

    open fun stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates()")
    }

    override fun onDestroy() {
        super.onDestroy()
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