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
import com.google.android.gms.location.LocationRequest
import com.xplore.maps.live_hike.LiveHikeBroadcastReceiver
import com.xplore.util.FirebaseUtil

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

    companion object {
        const val ARG_LOCATION_REQUEST = "locationRequest"
        const val ARG_UPLOAD_LOCATION = "uploadLocation"

        @JvmStatic
        fun newIntent(context: Context, locationRequest: LocationRequest): Intent {
            return Intent(context, LocationUpdateService::class.java)
                    .putExtra(ARG_LOCATION_REQUEST, locationRequest)
        }

        @JvmStatic
        fun newIntent(context: Context, locationRequest: LocationRequest, uploadLocaiton: String): Intent {
            return Intent(context, LocationUpdateService::class.java)
                    .putExtra(ARG_LOCATION_REQUEST, locationRequest)
                    .putExtra(ARG_UPLOAD_LOCATION, uploadLocaiton)
        }
    }

    private lateinit var locationUpdater: LocationUpdater
    private lateinit var locationRequest: LocationRequest

    override fun onBind(intent: Intent?): IBinder? {
        log("onBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand")
        super.onStartCommand(intent, flags, startId)

        locationRequest = intent?.getParcelableExtra(ARG_LOCATION_REQUEST) as LocationRequest
        val pendingIntent = LiveHikeBroadcastReceiver.newPendingIntent(this,
                FirebaseUtil.getRef(intent.getStringExtra(ARG_UPLOAD_LOCATION)))

        locationUpdater = LocationUpdater(this, locationRequest, pendingIntent)
        locationUpdater.start()

        return START_STICKY
    }

    override fun onCreate() {
        log("onCreate")
        super.onCreate()

        val name = "Location Update"
        val channelId = "channelId-0"
        val description = "description"

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