package com.xplore.maps.live_hike

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.FirebaseDatabase
import com.xplore.util.FirebaseUtil

/**
 * Created by Nika on 12/2/2017.
 *
 * BroadcastReceiver class for background location updates during a Live Hike.
 * Uploads Location data to given firebase node.
 *
 */

class LiveHikeBroadcastReceiver(): BroadcastReceiver() {

    private val TAG = "LU-broadcast-receiver"
    private fun log(s: String) = Log.i(TAG, s)

    companion object {
        const val ACTION_PROCESS_UPDATES = "com.xplore.maps.action.PROCESS_UPDATES"

        @JvmStatic
        fun newPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, LiveHikeBroadcastReceiver::class.java)
            intent.action = ACTION_PROCESS_UPDATES
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }



    override fun onReceive(context: Context?, intent: Intent?) {
        log("onReceive")
        // TODO use result.locations for snail trail func in the future

        if (intent != null) {
            if (intent.action == ACTION_PROCESS_UPDATES) {
                val result = LocationResult.extractResult(intent)
                if (result != null) {
                    val locations = result.locations


                    locations.forEach { loc ->
                        log("lat: " + loc.latitude)
                        log("lng: " + loc.longitude)
                    }

                   /* val userId = intent.getStringExtra("userId")
                    val groupId = intent.getStringExtra("groupId")
                    val ref = "/groups/$groupId/locations/$userId"

                    uploadLocation(ref, result.lastLocation)*/
                }
            }
        }
    }

    private fun uploadLocation(locationRef: String, location: Location) {
        FirebaseDatabase.getInstance().getReference(locationRef).child("latitude").setValue(location.latitude)
        FirebaseDatabase.getInstance().getReference(locationRef).child("longitude").setValue(location.longitude)
    }
}