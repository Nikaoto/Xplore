package com.xplore.maps

import android.app.PendingIntent
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

/*
 * Created by Nika on 12/2/2017.
 *
 * Used to get location updates based on the passed locationRequest.
 * ALWAYS CHECK PERMISSIONS BEFORE CALLING start()
 *
 */

class LocationUpdater private constructor (private val context: Context,
                                           private var locationRequest: LocationRequest?) {

    private val TAG = "location-updater"
    private fun log(s: String) = Log.i(TAG, s)
    private inner class NoCallbackException(override var message: String): Exception()

    private var locationCallback: LocationCallback? = null
    private var onLocationUpdate: ((locationResult: LocationResult) -> Unit?)? = null
    private var pendingIntent: PendingIntent? = null

    // With LocationCallback
    constructor(context: Context, locationRequest: LocationRequest?,
                onLocationUpdateArg: (locationResult: LocationResult) -> Unit)
            : this(context, locationRequest){
        this.onLocationUpdate = onLocationUpdateArg
        this.locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult != null) {
                    super.onLocationResult(locationResult)

                    // Update last location
                    lastLocation = locationResult.lastLocation

                    onLocationUpdate?.invoke(locationResult)
                }
            }
        }
    }

    // With PendingIntent of BroadcastReceiver
    constructor(context: Context, locationRequest: LocationRequest?, pendingIntent: PendingIntent)
            : this(context, locationRequest) {
        this.pendingIntent = pendingIntent
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var lastLocation: Location? = null
    var updatingLocation = false

    init {
        // Get last known location
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                lastLocation = location
            }
        } catch (e: SecurityException) {
            log("init: SecurityException")
        }
    }

    fun start() {
        log("start()")

        updatingLocation = true
        try {
            when {
                locationCallback != null -> {
                    log("starting with locationCallback")

                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                            Looper.myLooper())
                }
                pendingIntent != null -> {
                    log("starting with pendingIntent")

                    fusedLocationClient.requestLocationUpdates(locationRequest, pendingIntent)
                }
                else -> throw NoCallbackException("start(): pendingIntent and locationCallback are null")
            }
        } catch (e: SecurityException) {
            log("start(): SecurityException")
        }
    }

    fun updateLocationRequest(locationRequest: LocationRequest) {
        this.locationRequest = locationRequest
        if (updatingLocation) {
            start()
        }
    }

    fun stop() {
        when {
            locationCallback != null -> fusedLocationClient.removeLocationUpdates(locationCallback)
                    .addOnSuccessListener {
                        log("stopped locationCallback updates")
                        updatingLocation = false
                    }
            pendingIntent != null -> fusedLocationClient.removeLocationUpdates(pendingIntent)
                    .addOnSuccessListener {
                        log("stopped pendingIntent updates")
                        updatingLocation = false
                    }
            else -> throw NoCallbackException("stop(): pendingIntent and locationCallback are null")
        }
    }
}
