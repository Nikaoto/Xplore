package com.xplore.maps

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult

/**
 * Created by Nika on 12/2/2017.
 * TODO write description of this class - what it does and why.
 */

class LocationUpdateBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "LU-broadcast-receiver"
    private fun log(s: String) = Log.i(TAG, s)

    companion object {
        const val ACTION_PROCESS_UPDATES = "com.xplore.maps.action.PROCESS_UPDATES"

        @JvmStatic
        fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, LocationUpdateBroadcastReceiver::class.java)
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
                }
            }
        }
    }
}