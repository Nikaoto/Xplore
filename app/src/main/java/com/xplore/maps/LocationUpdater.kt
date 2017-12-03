package com.xplore.maps

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
                      private var pendingIntent: PendingIntent?
                      /*private val onLocationUpdate: (locationResult: LocationResult) -> Unit*/) {

    private val TAG = "location-updater"
    private fun log(s: String) = Log.i(TAG, s)

    companion object {
        const val DEFAULT_UPDATE_INTERVAL = 5000L
        const val DEFAULT_FASTEST_UPDATE_INTERVAL = 1000L
        const val DEFAULT_LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    init {
        // Set defaults

        if (locationRequest == null) {
            locationRequest = LocationRequest().setInterval(DEFAULT_UPDATE_INTERVAL)
                    .setFastestInterval(DEFAULT_FASTEST_UPDATE_INTERVAL)
                    .setPriority(DEFAULT_LOCATION_PRIORITY)
        }

        if (pendingIntent == null) {
            pendingIntent = getBroadcastReceiverPendingIntent()
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

                //onLocationUpdate(locationResult)
            }
        }
    }

    private fun getBroadcastReceiverPendingIntent(): PendingIntent {
        val intent = Intent(context, LocationUpdateBroadcastReceiver::class.java)
        intent.action = LocationUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun start() {
        try {
            log("start()")
            //fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            fusedLocationClient.requestLocationUpdates(locationRequest, pendingIntent)
            updatingLocation = true
        } catch (e: SecurityException) {
            log("start() - SecurityException")
        }
    }

    fun stop() {
        fusedLocationClient.removeLocationUpdates(pendingIntent)
                .addOnSuccessListener {
                    log("removeLocationUpdates - onSuccess")
                    updatingLocation = false
                }
    }
}
