package com.xplore.util

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.xplore.MainActivity
import com.xplore.R
import com.xplore.groups.view.GroupInfoActivity
import com.xplore.maps.live_hike.LiveHikeMapActivity

/**
 * Created by Nika on 12/19/2017.
 *
 * Used for push and live notifications
 *
 */

class NotificationUtil private constructor(val context: Context){

    companion object : SingletonHolder<NotificationUtil, Context>(::NotificationUtil)

    private fun getNotificationManager(): NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    /* Live Hike */

    val LIVE_HIKE_NOTIFICATION_CHANNEL_ID = "xplore-live-hike-01"

    val LIVE_HIKE_NOTIFICATION_NAME: String by lazy {
        context.getString(R.string.live_hike_notification_name)
    }

    val LIVE_HIKE_NOTIFICATION_DESCRIPTION: String by lazy {
        context.getString(R.string.live_hike_notification_description)
    }

    val LIVE_HIKE_NOTIFICATION_COLOR_ID = R.color.colorPrimary

    @SuppressLint("NewApi")
    fun createLiveHikeNotification(groupId: String, destName: String, destLatLng: LatLng): Notification {
        // Create channel if v26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    LIVE_HIKE_NOTIFICATION_CHANNEL_ID,
                    LIVE_HIKE_NOTIFICATION_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            )
            channel.lightColor = LIVE_HIKE_NOTIFICATION_COLOR_ID
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            getNotificationManager().createNotificationChannel(channel)
        }

        // Build backstack
        val stackBuilder = TaskStackBuilder.create(context)
                .addParentStack(MainActivity::class.java)
                .addNextIntent(Intent(context, MainActivity::class.java))
                .addNextIntent(GroupInfoActivity.newIntent(context, groupId))
                .addNextIntent(LiveHikeMapActivity.newIntent(context, groupId, destName,
                        destLatLng.latitude, destLatLng.longitude))
        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        //TODO add distance calculation

        return NotificationCompat.Builder(context, LIVE_HIKE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(LIVE_HIKE_NOTIFICATION_NAME)
                .setContentText(LIVE_HIKE_NOTIFICATION_DESCRIPTION)
                .setContentIntent(pendingIntent)
                //.setDeleteIntent()
                .setSmallIcon(R.drawable.ic_xplore_tiny)
                .setColor(ContextCompat.getColor(context, LIVE_HIKE_NOTIFICATION_COLOR_ID))
                .setColorized(true)
                .build()
    }
}