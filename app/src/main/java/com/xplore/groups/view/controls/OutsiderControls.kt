package com.xplore.groups.view.controls

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import com.xplore.General
import com.xplore.R
import kotlinx.android.synthetic.main.outsider_controls.*

/**
 * Created by Nika on 8/8/2017.
 *
 * აღწერა:
 * ეს ფრაგმენტი ჩნდება მხოლოდ მაშინ როდესაც მომხმარებელი უყურებს ჯგუფს, რომელშიც იგი არც
 * დაპატიჟებულია და არც გაწევრიანებული. აქედან მას შეუძლია გაწევრიანების რექუესტი გაუგზავნოს ჯგუფს.
 *
 * Description:
 * This fragment is only shown when the user is viewing a group that he's neither invited to nor
 * joined. From this panel, the user can send a join request to the group.
 *
 */
class OutsiderControls : Fragment() {

    //Firebase
    private val F_GROUPS_TAG = "groups"
    private val F_INVITED_MEMBER_IDS = "invited_member_ids"
    private val F_MEMBER_IDS = "member_ids"
    private val F_INVITED_GROUP_IDS = "invited_group_ids"
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")
    lateinit private var currentGroupRef: DatabaseReference
    //
    lateinit private var groupId: String
    private var awaitingRequest = false


    companion object {
        @JvmStatic
        fun newInstance(groupId: String, awaitingRequest: Boolean = false): OutsiderControls {
            val fragment = OutsiderControls()
            val args = Bundle()
            args.putString("groupId", groupId)
            args.putBoolean("awaitingRequest", awaitingRequest)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
            = inflater.inflate(R.layout.outsider_controls, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = arguments.getString("groupId")
        currentGroupRef = FirebaseDatabase.getInstance().getReference("$F_GROUPS_TAG/$groupId")

        awaitingRequest = arguments.getBoolean("awaitingRequest")

        if (awaitingRequest) {
            startListeningForJoinRequestResponse()
            //Remove button
            joinGroupButton.isEnabled = false
            joinGroupButton.visibility = View.GONE
            //Show request sent text
            requestSentTextView.visibility = View.VISIBLE
        } else {
            startListeningForInvites()
            joinGroupButton.setOnClickListener {
                sendJoinRequest(groupId)
            }
        }
    }

    private fun sendJoinRequest(groupId: String) {

        stopListeningForInvites()

        //Adding current member id to invited member ids WITH FALSE VALUE
        currentGroupRef.child(F_INVITED_MEMBER_IDS).child(General.currentUserId).setValue(false)

        //Adding group id to member's invited group ids WITH FALSE VALUE
        usersRef.child(General.currentUserId).child(F_INVITED_GROUP_IDS).child(groupId)
                .setValue(false)

        refreshControls()
    }

    private val onInviteListener = object : ChildEventListener {
        //Triggers when invite is received from this group
        override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
            if (dataSnapshot != null) {
                if (dataSnapshot.key == General.currentUserId) {
                    val value = dataSnapshot.getValue(Boolean::class.java)
                    if (value != null && value == true)
                    Toast.makeText(activity,
                            "You have been invited to this group",
                            Toast.LENGTH_SHORT).show()
                    refresh()
                }
            }
        }

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        override fun onChildRemoved(p0: DataSnapshot?) {}

        override fun onCancelled(p0: DatabaseError?) {}
    }

    private val onJoinRequestResponseListener = object: ChildEventListener {
        //Triggers when join request is accepted from this group
        override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
            if (dataSnapshot != null) {
                if (dataSnapshot.key == General.currentUserId) {
                    //TODO string resources
                    Toast.makeText(activity,
                            "Your join request has been accepted!",
                            Toast.LENGTH_SHORT).show()
                    refresh()
                }
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {}

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        override fun onChildRemoved(p0: DataSnapshot?) {}

        override fun onCancelled(p0: DatabaseError?) {}
    }

    //Starts listening for invite from current group
    private fun startListeningForInvites() {
        currentGroupRef.child(F_INVITED_MEMBER_IDS).addChildEventListener(onInviteListener)
    }

    private fun stopListeningForInvites() {
        currentGroupRef.child(F_INVITED_MEMBER_IDS).removeEventListener(onInviteListener)
    }

    //Starts listening for join request acceptance from current group
    private fun startListeningForJoinRequestResponse(){
        currentGroupRef.child(F_MEMBER_IDS).addChildEventListener(onJoinRequestResponseListener)
    }
    private fun stopListeningForJoinRequestResponse(){
        currentGroupRef.child(F_MEMBER_IDS).removeEventListener(onJoinRequestResponseListener)
    }

    //Refreshes the whole activity
    private fun refresh() {
        val intent = activity.intent
        activity.finish()
        startActivity(intent)
    }

    //Refreshes only the fragment containing the controls
    private fun refreshControls() {
        fragmentManager.beginTransaction().detach(this)
                .replace(R.id.controls_container, newInstance(groupId, true)).commit()
    }

    override fun onDetach() {
        super.onDetach()
        if (awaitingRequest) {
            stopListeningForJoinRequestResponse()
        } else {
            stopListeningForInvites()
        }
    }
}