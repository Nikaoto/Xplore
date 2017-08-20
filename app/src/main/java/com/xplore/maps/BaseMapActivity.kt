package com.xplore.maps

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.xplore.General
import com.xplore.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_maps.*


/**
 * Created by Nika on 8/20/2017.
 * TODO write description of this class - what it does and why.
 */

class BaseMapActivity : AppCompatActivity() {

    private val TAG = "basemaps"

    private val UPDATE_INTERVAL = 5000L
    private val FASTEST_UPDATE_INTERVAL = 1000L
    private val LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY
    private val REQUEST_PERMISSION_REQ_CODE = 1
    private val REQUEST_CHECK_SETTINGS = 0x1

    //Location
    private lateinit var currentLocation: Location
    private var updatingLocation = false
    private var listeningForGroupLocation = false
    private var sendingLocationToGroup = false
    private val locationRequest: LocationRequest by lazy { createLocationRequest() }
    private val locationSettingsRequest: LocationSettingsRequest by lazy {
        buildLocationSettingsRequest()
    }

    //Clients
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private val settingsClient: SettingsClient by lazy {
        LocationServices.getSettingsClient(this)
    }

    //Firebase
    private val F_GROUPS = "groups"
    private val F_LOCATIONS = "locations"
    private val F_LATITUDE = "latitude"
    private val F_LONGITUDE = "longitude"
    private lateinit var currentGroupRef: DatabaseReference
    private lateinit var groupLocationsRef: DatabaseReference
    private lateinit var currentUserLocationRef: DatabaseReference

    private lateinit var groupId: String

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String, destinationName: String,
                           destinationLng: Double, destinationLat: Double): Intent {
            return Intent(context, BaseMapActivity::class.java)
                    .putExtra("groupId", groupId)
                    .putExtra("destinationName", destinationName)
                    .putExtra("destinationLat", destinationLat)
                    .putExtra("destinationLng", destinationLng)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //buildDestinationMarker()
        //startListeningForGroupLocations()
        initMap()
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync{ googleMap ->
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

            if (checkPermissions()) {
                googleMap.isMyLocationEnabled = true
                googleMap.setOnMyLocationButtonClickListener {
                    if (!checkPermissions()) {
                        requestPermissions()
                    }
                    checkLocationEnabled()
                    false
                }
            } else {
                requestPermissions()
            }
        }
    }

    private fun initFirebaseReferences() {
        groupId = intent.getStringExtra("groupId")

        //Set firebase references
        currentGroupRef = FirebaseDatabase.getInstance().getReference("$F_GROUPS/$groupId")
        groupLocationsRef = currentGroupRef.child(F_LOCATIONS)
        currentUserLocationRef = groupLocationsRef.child(General.currentUserId)

    }

    private fun buildDestinationMarker(): MarkerOptions {
        val pos = LatLng(intent.getDoubleExtra("destinationLat", 0.0),
                intent.getDoubleExtra("destinationLng", 0.0))
        val markerOptions = MarkerOptions()
        markerOptions.position(pos)
        markerOptions.title(intent.getStringExtra("destinationName"))
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        return markerOptions
    }

    private fun createLocationRequest(): LocationRequest {
        val temp = LocationRequest()
        temp.interval = UPDATE_INTERVAL
        temp.fastestInterval = FASTEST_UPDATE_INTERVAL
        temp.priority = LOCATION_PRIORITY
        return temp
    }

    private fun buildLocationSettingsRequest(): LocationSettingsRequest {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        return builder.build()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                super.onLocationResult(locationResult)

                currentLocation = locationResult.lastLocation
                sendLocationData()
            }

        }
    }

    private fun sendLocationData() {

    }


    override fun onResume() {
        super.onResume()
        if (!checkPermissions()) {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
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
                    REQUEST_PERMISSION_REQ_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_REQ_CODE) {
            if (grantResults.isEmpty()) {
                //If interaction canelled
                Log.i(TAG, "user interaction cancelled")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Permission granted

                startLocationUpdates()
            } else {
                //Permission denied

                //TODO explain why you need to track their location
                //finish()
            }
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

            when (requestCode) {
                REQUEST_CHECK_SETTINGS -> {
                    if (resultCode == Activity.RESULT_OK) {
                        Log.i(TAG, "user agreed to enable location")

                        startLocationUpdates()
                    } else {
                        Log.i(TAG, "user chose not to enable location")
                    }
                }
                else -> Log.i(TAG, "This message should never show up lol wtf did you do to my code *cryinglaughingemoji* 1!!!11!exclamationmark!")
            }
        }

    }

    private fun stopLocationUpdates() {
        if (updatingLocation) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
                    .addOnCompleteListener { updatingLocation = false }
        }
    }

    private fun destroyMap() {
        val f = fragmentManager.findFragmentById(R.id.mapFragment)
        if (f != null) {
            fragmentManager.beginTransaction().remove(f).commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        stopLocationUpdates()
        destroyMap()
    }

}