package com.xplore.groups.view.controls

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.xplore.General
import com.xplore.R
import kotlinx.android.synthetic.main.invited_controls.*

/**
 * Created by Nika on 8/4/2017.
 *
 * აღწერა:
 * ეს ფრაგმენტი ჩნდება მხოლოდ მაშინ როდესაც მომხმარებელი უყურებს ჯგუფს, რომელშიც იგი დაპატიჟეს და
 * აძლევს არჩევანს (მიიღოს ან უარყოს მოწვევა).
 *
 * Description:
 * This fragment is only shown when the user is viewing a group that he's invited in. The user
 * can choose to accept or decline the invite from this fragment.
 *
 */

class InvitedControls : Fragment() {

    //Firebase
    private val F_MEMBER_IDS = "member_ids"
    private val F_INVITED_MEMBER_IDS = "invited_member_ids"
    private val F_INVITED_GROUP_IDS = "invited_group_ids"
    private val currentUserRef = FirebaseDatabase.getInstance().reference
            .child("users").child(General.currentUserId)
    private val invitedGroupIdsRef = currentUserRef.child("invited_group_ids")
    private val joinedGroupIdsRef = currentUserRef.child("group_ids")
    private lateinit var currentGroupRef: DatabaseReference
    //
    private lateinit var groupId: String

    private var listening = false

    companion object {
        @JvmStatic
        fun newInstance(groupId: String): InvitedControls {
            val fragment = InvitedControls()
            val args = Bundle()
            args.putString("groupId", groupId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.invited_controls, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = arguments.getString("groupId")
        currentGroupRef = FirebaseDatabase.getInstance().reference.child("groups").child(groupId)

        startListeningForInviteCancelation()

        acceptInviteButton.setOnClickListener {
            acceptInvite()
        }

        declineInviteButton.setOnClickListener {
            declineInvite()
        }
    }

    val onInvitationCancelListener = object : ChildEventListener {
        //When invite was canceled from leader while this user was viewing the group
        override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
            if (dataSnapshot != null) {
                if (dataSnapshot.key == groupId) {
                    refresh()
                }
            }
        }

        override fun onCancelled(p0: DatabaseError?) {}

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {}

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}
    }

    private fun startListeningForInviteCancelation() {
        listening = true
        currentUserRef.child(F_INVITED_GROUP_IDS).addChildEventListener(onInvitationCancelListener)
    }

    private fun stopListeningForInviteCancelation() {
        if (listening) {
            listening = false
            currentUserRef.child(F_INVITED_GROUP_IDS).removeEventListener(onInvitationCancelListener)
        }
    }

    private fun acceptInvite() {
        stopListeningForInviteCancelation()
        // Add to joined groups
        joinedGroupIdsRef.child(groupId).setValue(false)
        // Remove from invited groups
        invitedGroupIdsRef.child(groupId).removeValue()
        // Remove user id from group's invited_member_ids
        currentGroupRef.child(F_INVITED_MEMBER_IDS).child(General.currentUserId).removeValue()
        // Add user id to group's memberIds
        currentGroupRef.child(F_MEMBER_IDS).child(General.currentUserId).setValue(false)

        refresh()
    }

    private fun declineInvite() {
        stopListeningForInviteCancelation()
        // Remove from invited groups
        invitedGroupIdsRef.child(groupId).removeValue()
        // Remove this user from group's invited users
        currentGroupRef.child("invited_member_ids").child(General.currentUserId).removeValue()

        refresh()
    }

    private fun refresh() {
        val intent = activity.intent
        activity.finish()
        startActivity(intent)
    }

    override fun onDetach() {
        super.onDetach()
        stopListeningForInviteCancelation()
    }
}