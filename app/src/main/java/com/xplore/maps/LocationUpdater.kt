package com.xplore.maps

import android.content.Context
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

class LocationUpdater(private val context: Context,
                      private var locationRequest: LocationRequest?,
                      private val onLocationUpdate: (locationResult: LocationResult) -> Unit) {

    private val TAG = "location-updater"
    private fun log(s: String) = Log.i(TAG, s)

    companion object {
        const val DEFAULT_UPDATE_INTERVAL = 5000L
        const val DEFAULT_FASTEST_UPDATE_INTERVAL = 1000L
        const val DEFAULT_LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    init {
        // Set default location request if none given
        if (locationRequest == null) {
            locationRequest = LocationRequest().setInterval(DEFAULT_UPDATE_INTERVAL)
                    .setFastestInterval(DEFAULT_FASTEST_UPDATE_INTERVAL)
                    .setPriority(DEFAULT_LOCATION_PRIORITY)
        }
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var updatingLocation = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult != null) {
                super.onLocationResult(locationResult)
                log("""onLocationUpdate:
                |latitude: ${locationResult.lastLocation.latitude}
                |longitude: ${locationResult.lastLocation.longitude}""".trimMargin())

                onLocationUpdate(locationResult)
            }
        }
    }

    fun start() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            updatingLocation = true
        } catch (e: SecurityException) {
            log("start() - SecurityException")
        }
    }

    fun stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnSuccessListener {
                    log("removeLocationUpdates - onSuccess")
                    updatingLocation = false
                }
    }
}
