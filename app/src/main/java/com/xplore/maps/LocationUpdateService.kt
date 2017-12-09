package com.xplore.maps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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
 *
 * A Service wrapper for the LocationUpdater. Allows use of LocationUpdater in the background with
 * PendingIntent and LocationUpdateBroadcastReceiver receiving callbacks
 *
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

    override fun onCreate() {
        log("onCreate")
        super.onCreate()

        val name = "Location Update"
        val channelId = "channelId-0"
        val description = "description"

        locationUpdater = LocationUpdater(this, null, LocationUpdateBroadcastReceiver.getPendingIntent(this))
        locationUpdater.start()
        startForeground(id, createNotifOld(channelId, name, description))
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

    override fun onDestroy() {
        log("onDestroy")
        super.onDestroy()

        locationUpdater.stop()
    }
}