package com.xplore.notifications

import android.view.View
import android.widget.TextView
import com.google.firebase.database.*
import com.xplore.General
import com.xplore.util.FirebaseUtil.getInvitedGroupIdsRef

/**
 * Created by Nik on 7/22/2017.
 *
 * Description:
 * This class updates notifications and displays their number on the drawerBadge and textViews
 * given in constructor
 * //TODO add actual push notifications and background checking
 * //TODO add message sent notifications
 *
 */

class NotificationUtil(private val drawerBadge: BadgeDrawerArrowDrawable,
                           private val myGroupsBadge: TextView) {

    private var invitedGroupIdsRef = getInvitedGroupIdsRef(General.currentUserId)

    //Listener for tracking changes in invites
    private val inviteListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
            if (dataSnapshot != null) {
                val value = dataSnapshot.getValue(Boolean::class.java)
                if (value != null) {
                    if (value) {
                        updateCount(1)
                    }
                }
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
            if (dataSnapshot != null) {
                val value = dataSnapshot.getValue(Boolean::class.java)
                if (value != null) {
                    if (value) {
                        updateCount(-1)
                    }
                }
            }
        }

        override fun onCancelled(p0: DatabaseError?) {}

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}
    }
    //
    //TODO join listener and notif seen

    private var notificationCount: Int = 0;

    init {
        init()
    }

    fun init(){
        updateBadges()
        startListening()
    }

    //Listens for changes in invites and updates notifs accordingly
    fun startListening() {
        invitedGroupIdsRef.addChildEventListener(inviteListener)
    }

    //Disabled badges if no notifs, otherwise udpates their visibility and text
    fun updateBadges() {
        if (notificationCount == 0) {
            //Drawer
            drawerBadge.setEnabled(false)
            //My Groups
            myGroupsBadge.visibility = View.INVISIBLE
        } else {
            //Drawer
            drawerBadge.setEnabled(true)
            drawerBadge.text = notificationCount.toString()
            //My Groups
            myGroupsBadge.post {
                myGroupsBadge.visibility = View.VISIBLE
                myGroupsBadge.text = notificationCount.toString()
            }
        }
    }

    fun updateCount(amount: Int) {
        notificationCount += amount
        updateBadges()
    }

    fun reset() {
        invitedGroupIdsRef.removeEventListener(inviteListener)
        invitedGroupIdsRef = getInvitedGroupIdsRef(General.currentUserId)
        init()
    }

    fun disable() {
        invitedGroupIdsRef.removeEventListener(inviteListener)
        notificationCount = 0
        updateBadges()
    }
}
