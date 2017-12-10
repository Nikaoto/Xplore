package com.xplore.maps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.LocationRequest
import com.xplore.R
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

    private val id = 1

    companion object {
        const val ARG_LOCATION_REQUEST = "locationRequest"
        const val ARG_UPLOAD_LOCATION = "uploadLocation"
        const val CHANNEL_ID = "xplore-live-hike-01"

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

        val name = "Xplore Location Update"
        val description = "description"
        val notifColorId = R.color.colorPrimary

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(id, createNotifNew(CHANNEL_ID, name, description, notifColorId))
        } else {
            startForeground(id, createNotifOld(CHANNEL_ID, name, description, notifColorId))
        }
    }

    private fun getNotificationManager(): NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotifNew(channelId: String, name: String, description: String, colorId: Int)
            : Notification {
        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
        channel.lightColor = colorId
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        getNotificationManager().createNotificationChannel(channel)

        return createNotifOld(channelId, name, description, colorId)
    }

    private fun createNotifOld(channelId: String, name: String, description: String, colorId: Int)
            : Notification {
        return NotificationCompat.Builder(this, channelId)
                .setContentTitle(name)
                .setContentText(description)
                //.setContentIntent()
                //.setDeleteIntent()
                .setSmallIcon(R.drawable.ic_xplore_tiny)
                .setColor(ContextCompat.getColor(this, colorId))
                .setColorized(true)
                .build()
    }

    override fun onDestroy() {
        log("onDestroy")
        super.onDestroy()

        locationUpdater.stop()
    }
}