package com.xplore.groups.view.controls

import android.app.AlertDialog
import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.xplore.R
import com.xplore.groups.AllMemberIdsForGroup
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
    private val F_GROUP_IDS = "group_ids"
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")
    private val groupsRef = FirebaseDatabase.getInstance().reference.child("groups")
    private lateinit var groupId: String
    private lateinit var currentGroupRef: DatabaseReference

    //TODO add discussion
    //TODO add remove members button

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

        inviteMembersButton.setOnClickListener {
            startInvitingMembers()
        }

        editGroupButton.setOnClickListener {
            //startEditingGroup()
        }

        //TODO add red badge for total request count
        manageRequestsButton.setOnClickListener {
            startActivity(ManageRequestsActivity.getStartIntent(activity, groupId))
        }

        deleteGroupButton.setOnClickListener {
            confirmGroupDeletion()
        }
    }

    private fun startInvitingMembers() {
        startActivity(InviteMembersActivity.getStartIntent(activity, groupId))
    }

    private fun confirmGroupDeletion() {
        val builder = AlertDialog.Builder(activity)
        //TODO string resources
        builder.setTitle(R.string.delete_group)
                .setMessage("Are you sure you want to delete this group?")
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
}