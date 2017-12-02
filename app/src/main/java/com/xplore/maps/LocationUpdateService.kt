package com.xplore.maps

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
/**
 * Created by Nika on 12/2/2017.
 * TODO write description of this class - what it does and why.
 */

class LocationUpdateService : Service() {

    private val TAG = "location-update-serv"
    private fun log(s: String) = Log.i(TAG, s)

    private lateinit var locationUpdater: LocationUpdater

    override fun onBind(intent: Intent?): IBinder? {
        log("onBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand")
        super.onStartCommand(intent, flags, startId)

        return START_STICKY
    }

    override fun onCreate() {
        log("onCreate")
        super.onCreate()

        val intent = Intent(this, LocationUpdateIntentService::class.java)
        intent.action = LocationUpdateIntentService.ACTION_PROCESS_UPDATES
        locationUpdater = LocationUpdater(this, null,
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))

/*
        locationUpdater = LocationUpdater(applicationContext, null, {locationResult ->
            val lastLoc = locationResult.lastLocation
            log("lat: " + lastLoc.latitude.toString())
            log("lng: " + lastLoc.longitude.toString())
        })
*/

        locationUpdater.start()
    }

    override fun onDestroy() {
        log("onDestroy")
        super.onDestroy()

        locationUpdater.stop()
    }
}