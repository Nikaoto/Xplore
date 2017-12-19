package com.xplore.maps

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.xplore.General
import com.xplore.MainActivity
import com.xplore.R
import com.xplore.base.BaseService
import com.xplore.groups.view.GroupInfoActivity
import com.xplore.maps.live_hike.LiveHikeBroadcastReceiver
import com.xplore.maps.live_hike.LiveHikeMapActivity
import com.xplore.util.FirebaseUtil
import com.xplore.util.NotificationUtil

/**
 * Created by Nika on 12/2/2017.
 *
 * A Service wrapper for the LocationUpdater. Allows use of LocationUpdater in the background with
 * PendingIntent and a BroadcastReceiver receiving callbacks
 *
 */

class LocationUpdateService : BaseService() {

    private val TAG = "location-update-serv"
    private fun log(s: String) = Log.i(TAG, s)

    companion object {
        private const val ARG_LOCATION_REQUEST = "locationRequest"
        private const val ARG_GROUP_ID = "groupId"
        private const val ARG_USER_ID = "userId"
        private const val ARG_DEST_NAME = "destName"
        private const val ARG_DEST_LAT_LNG = "destLatLng"
        private const val FOREGROUND_ID = 1

        @JvmStatic
        fun newIntent(context: Context, locationRequest: LocationRequest, groupId: String,
                      destName: String, destLatLng: LatLng): Intent {

            return Intent(context, LocationUpdateService::class.java)
                    .putExtra(ARG_LOCATION_REQUEST, locationRequest)
                    .putExtra(ARG_GROUP_ID, groupId)
                    .putExtra(ARG_USER_ID, General.currentUserId)
                    .putExtra(ARG_DEST_NAME, destName)
                    .putExtra(ARG_DEST_LAT_LNG, destLatLng)
        }
    }

    private lateinit var locationUpdater: LocationUpdater
    private lateinit var locationRequest: LocationRequest

    private lateinit var groupId: String
    private lateinit var userId: String
    private lateinit var destName: String
    private lateinit var destLatLng: LatLng

    override fun onBind(intent: Intent?): IBinder? {
        log("onBind")
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        log("onStartCommand")
        super.onStartCommand(intent, flags, startId)

        // Get passed data
        userId = intent.getStringExtra(ARG_USER_ID)
        groupId = intent.getStringExtra(ARG_GROUP_ID)
        destName = intent.getStringExtra(ARG_DEST_NAME)
        destLatLng = intent.getParcelableExtra(ARG_DEST_LAT_LNG) as LatLng
        locationRequest = intent.getParcelableExtra(ARG_LOCATION_REQUEST) as LocationRequest

        startForeground(FOREGROUND_ID,
                NotificationUtil.getInstance(this).createLiveHikeNotification(groupId,
                        destName, destLatLng))

        // Create Pending Intent for Broadcast Receiver
        val pendingIntent = LiveHikeBroadcastReceiver.newPendingIntent(
                this,
                FirebaseUtil.getRef(FirebaseUtil.getUserLocationRefString(groupId, userId)))

        // Create and start LocationUpdater
        locationUpdater = LocationUpdater(this, locationRequest, pendingIntent)
        locationUpdater.start()

        return START_STICKY
    }

    override fun onCreate() {
        log("onCreate")
        super.onCreate()
    }

    override fun onDestroy() {
        log("onDestroy")
        super.onDestroy()

        locationUpdater.stop()
    }
}