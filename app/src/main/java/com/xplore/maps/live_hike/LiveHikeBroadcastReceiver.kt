package com.xplore.maps.live_hike

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.DatabaseReference
import com.xplore.util.FirebaseUtil

/**
 * Created by Nika on 12/2/2017.
 *
 * BroadcastReceiver class for background location updates during a Live Hike.
 * Uploads Location data to given firebase node.
 *
 */

class LiveHikeBroadcastReceiver(): BroadcastReceiver() {

    private val TAG = "live-hike-broadcast-rec"
    private fun log(s: String) = Log.i(TAG, s)

    companion object {
        const val ACTION_PROCESS_UPDATES = "com.xplore.maps.action.PROCESS_UPDATES"

        @JvmStatic
        fun newPendingIntent(context: Context, uploadRef: DatabaseReference): PendingIntent {
            this.uploadRef = uploadRef
            val intent = Intent(context, LiveHikeBroadcastReceiver::class.java)
            intent.action = ACTION_PROCESS_UPDATES
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // TODO remove these statics when Google updates fusedLocationProviderClient api
        @JvmField
        var uploadRef: DatabaseReference? = null

        @JvmStatic
        fun uploadLocation(location: Location) {
            uploadRef?.let {
                it.child(FirebaseUtil.F_LATITUDE).setValue(location.latitude)
                it.child(FirebaseUtil.F_LONGITUDE).setValue(location.longitude)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        log("onReceive")
        if (intent != null) {
            if (intent.action == ACTION_PROCESS_UPDATES) {
                val locationResult = LocationResult.extractResult(intent)
                if (locationResult != null) {
                    // TODO use result.locations for snail trail func in the future
                    // val locations = locationResult.locations

                    // Log
                    log("lat : ${locationResult.lastLocation.latitude}")
                    log("lng : ${locationResult.lastLocation.longitude}")

                    uploadLocation(locationResult.lastLocation)
                }
            }
        }
    }
}