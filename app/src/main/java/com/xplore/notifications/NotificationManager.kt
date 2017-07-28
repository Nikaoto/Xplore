package com.xplore.notifications

import android.view.View
import android.widget.TextView
import com.google.firebase.database.*
import com.xplore.General


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
class NotificationManager(
        private val drawerBadge: BadgeDrawerArrowDrawable,
        private val myGroupsBadge: TextView) {

    //Firebase
    private val FB_TAG_USERS = "users"
    private val FB_TAG_INVITED_GROUP_IDS = "invited_group_ids"
    private fun getInvitedGroupIdsFirebaseReference(userId: String): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
                .child(FB_TAG_USERS)
                .child(userId)
                .child(FB_TAG_INVITED_GROUP_IDS)
    }
    private var firebaseReference = getInvitedGroupIdsFirebaseReference(General.currentUserId)

    //Listener for tracking changes in invites
    private val inviteListener = object : ChildEventListener {
        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
            updateCount(1)
        }

        override fun onChildRemoved(p0: DataSnapshot?) {
            updateCount(-1)
        }

        override fun onCancelled(p0: DatabaseError?) {}

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}
    }
    //

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
        firebaseReference.addChildEventListener(inviteListener)
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
        firebaseReference.removeEventListener(inviteListener)
        firebaseReference = getInvitedGroupIdsFirebaseReference(General.currentUserId)
        init()
    }

    fun disable() {
        firebaseReference.removeEventListener(inviteListener)
        notificationCount = 0
        updateBadges()
    }
}
