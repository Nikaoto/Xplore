package com.xplore.maps

import android.app.IntentService
import android.content.Intent
import android.util.Log

/**
 * Created by Nika on 12/2/2017.
 * TODO write description of this class - what it does and why.
 */

class LocationUpdateIntentService() : IntentService("") {

    private val TAG = "LU-intent-service"
    private fun log(s: String) = Log.i(TAG, s)

    companion object {
        const val ACTION_PROCESS_UPDATES = "apu"
    }

    override fun onHandleIntent(intent: Intent?) {
        log("onHandleIntent")

/*        if (intent != null) {
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
        }*/
    }
}