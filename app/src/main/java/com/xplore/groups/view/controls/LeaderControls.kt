package com.xplore.groups.view.controls

import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.xplore.R
import com.xplore.groups.AllMemberIdsForGroup
import com.xplore.groups.create.CreateGroupActivity
import com.xplore.groups.create.EditGroupActivity
import com.xplore.groups.discussion.DiscussionActivity
import com.xplore.groups.requests.ManageRequestsActivity

import kotlinx.android.synthetic.main.leader_controls.*

/**
 * Created by Nikaoto on 8/4/2017.
 *
 * აღწერა:
 * ეს ფრაგმენტი ჩნდება ჯგუფის ქვეშ კართში და აძლებს ჯგუფის ლოიდერს გჯუფთან დაკავშირებულ
 * კონტროლებს.
 *
 * Description:
 * This is a fragment that shows at the bottom of a group to provide group related controls to the
 * leader.
 *
 */

class LeaderControls : Fragment() {

    //Firebase
    private val F_INVITED_GROUP_IDS = "invited_group_ids"
    private val F_INVITED_MEMBER_IDS = "invited_member_ids"
    private val F_GROUP_IDS = "group_ids"
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")
    private val groupsRef = FirebaseDatabase.getInstance().reference.child("groups")
    private lateinit var groupId: String
    private lateinit var currentGroupRef: DatabaseReference
    //
    private var listeningForJoinRequests = false
    private var joinRequestCount = 0

    companion object {
        @JvmStatic
        fun newInstance(groupId: String): LeaderControls {
            val fragment = LeaderControls()
            val args = Bundle()
            args.putString("groupId", groupId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.leader_controls, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = arguments.getString("groupId")
        currentGroupRef = groupsRef.child(groupId)

        startListeningForJoinRequests()

        openDiscussionButton.setOnClickListener {
            startActivity(DiscussionActivity.getStartIntent(activity, groupId))
        }

        inviteMembersButton.setOnClickListener {
            startInvitingMembers()
        }

        editGroupButton.setOnClickListener {
            startEditingGroup()
        }

        manageRequestsButton.setOnClickListener {
            startActivity(ManageRequestsActivity.getStartIntent(activity, groupId))
        }

        deleteGroupButton.setOnClickListener {
            confirmGroupDeletion()
        }
    }

    private val joinRequestListener  = object : ChildEventListener {
        //When new join request is received
        override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
            updateRequestCount(dataSnapshot, 1)
        }

        //When join request is removed
        override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
            updateRequestCount(dataSnapshot, -1)
        }

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}

        override fun onCancelled(p0: DatabaseError?) {}
    }

    private fun updateRequestCount(dataSnapshot: DataSnapshot?, change: Int) {
        dataSnapshot?.let {
            it.getValue(Boolean::class.java)?.let {
                if (!it) joinRequestCount += change
            }
        }
        if (joinRequestCount > 0) {
            requestCountBadge.visibility = View.VISIBLE
            requestCountBadge.text = joinRequestCount.toString()
        } else {
            requestCountBadge.visibility = View.INVISIBLE
            requestCountBadge.text = ""
        }
    }

    //Starts listening for new join requests and updates notif count
    private fun startListeningForJoinRequests() {
        if (!listeningForJoinRequests) {
            listeningForJoinRequests = true
            currentGroupRef.child(F_INVITED_MEMBER_IDS).addChildEventListener(joinRequestListener)
        }
    }

    //Gee, I wonder what this does...
    private fun stopListeningForJoinRequests(){
        if (listeningForJoinRequests) {
            listeningForJoinRequests = false
            currentGroupRef.child(F_INVITED_MEMBER_IDS).removeEventListener(joinRequestListener)
        }
    }

    private fun startInvitingMembers() {
        startActivity(InviteMembersActivity.getStartIntent(activity, groupId))
    }

    private fun startEditingGroup() {
        startActivity(EditGroupActivity.getStartIntent(activity, groupId))
    }

    private fun confirmGroupDeletion() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.delete_group)
                .setMessage(R.string.delete_group_question)
                .setPositiveButton(R.string.yes, { _, _ -> deleteGroup() })
                .setNegativeButton(R.string.no, null)
        builder.show()
    }

    private fun deleteGroup() {
        //Removing group id from other members
        currentGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val allMemberIds = dataSnapshot.getValue(AllMemberIdsForGroup::class.java)
                    if (allMemberIds != null) {
                        //Joined
                        for (memberId in allMemberIds.member_ids.keys) {
                            removeGroupIdFromMember(memberId, groupId)
                        }
                        //Invited
                        for (invMemberId in allMemberIds.invited_member_ids.keys) {
                            uninvite(invMemberId, groupId)
                        }


                        //Removing the group node
                        currentGroupRef.removeValue()

                        activity.finish()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) { }
        })
    }

    private fun removeGroupIdFromMember(memberId: String, groupId: String) {
        usersRef.child(memberId).child(F_GROUP_IDS).child(groupId).removeValue()
    }

    private fun uninvite(memberId: String, groupId: String) {
        usersRef.child(memberId).child(F_INVITED_GROUP_IDS).child(groupId).removeValue()
    }

    private fun refresh() {
        val intent = activity.intent
        activity.finish()
        startActivity(intent)
    }

    override fun onDetach() {
        super.onDetach()
        stopListeningForJoinRequests()
    }
}