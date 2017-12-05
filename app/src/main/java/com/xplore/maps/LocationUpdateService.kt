package com.xplore.maps

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
/**
 * Created by Nika on 12/2/2017.
 * TODO write description of this class - what it does and why.
 */

class LocationUpdateService : Service() {

    private val TAG = "location-update-serv"
    private fun log(s: String) = Log.i(TAG, s)

    private val id = 12

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

    private fun getBroadcastReceiverPendingIntent(): PendingIntent {
        val intent = Intent(this, LocationUpdateBroadcastReceiver::class.java)
        intent.action = LocationUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getNotificationManager(): NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotifNew(channelId: String) {
        val name = "Location Update"
        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
        channel.lightColor = Color.GREEN
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

    }

    private fun createNotifOld(channelId: String, name: String, description: String): Notification {
        return NotificationCompat.Builder(this, channelId)
                .setContentTitle(name)
                .setContentText(description)
                .build()
    }

    override fun onCreate() {
        log("onCreate")
        super.onCreate()

        val name = "Location Update"
        val channelId = "channelId-0"
        val description = "description"

/*
        locationUpdater = LocationUpdater(applicationContext, null, {locationResult ->
            val lastLoc = locationResult.lastLocation
            log("lat: " + lastLoc.latitude.toString())
            log("lng: " + lastLoc.longitude.toString())
        })
*/

        locationUpdater = LocationUpdater(this, null, getBroadcastReceiverPendingIntent())
        locationUpdater.start()
        startForeground(id, createNotifOld(channelId, name, description))
    }

    override fun onDestroy() {
        log("onDestroy")
        super.onDestroy()

        locationUpdater.stop()
    }
}