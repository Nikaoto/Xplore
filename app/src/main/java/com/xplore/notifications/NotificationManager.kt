package com.xplore.notifications

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xplore.General


/**
 * Created by Nik on 7/22/2017.
 *
 * Description:
 * This class updates notifications and displays their number on the drawerBadge.
 * //TODO add actual push notifications and background checking
 * //TODO add message sent notifications
 *
 */
class NotificationManager(val drawerBadge: BadgeDrawerArrowDrawable) {

    var notificationCount: Int = 0;

    init {
        drawerBadge.setEnabled(false)
        drawerBadge.text = "0"
    }

    fun update() {
        FirebaseDatabase.getInstance().reference.child("users").child(General.currentUserId)
                .child("invited_group_ids")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot != null) {
                            notificationCount = dataSnapshot.childrenCount.toInt()
                        } else {
                            notificationCount = 0
                        }
                        updateBadges()
                    }

                    override fun onCancelled(p0: DatabaseError?) {}
                }
        )
    }

    fun updateBadges() {
        if (notificationCount == 0) {
            drawerBadge.setEnabled(false)
        } else {
            drawerBadge.setEnabled(true)
            drawerBadge.text = notificationCount.toString()
        }
    }
}
