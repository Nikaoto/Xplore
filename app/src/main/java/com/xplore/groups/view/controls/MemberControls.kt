package com.xplore.groups.view.controls

import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import com.xplore.General
import com.xplore.R
import com.xplore.groups.discussion.DiscussionActivity

import kotlinx.android.synthetic.main.member_controls.*

/**
 * Created by Nikaoto on 8/4/2017.
 *
 * აღწერა:
 * ეს ფრაგმენტი ჩნდება ჯგუფის ქვეშ კართში და აძლებს ჯგუფის წევრს გჯუფთან დაკავშირებულ კონტროლებს.
 *
 * Description:
 * This is a fragment that shows at the bottom of a group to provide group related controls to the
 * member.
 *
 */

class MemberControls : Fragment() {

    //Firebase
    val FIREBASE_TAG_MEMBER_IDS = "member_ids"
    val FIREBASE_TAG_GROUP_IDS = "group_ids"
    val currentUserRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(General.currentUserId)
    private lateinit var currentGroupRef: DatabaseReference;
    //
    private lateinit var groupId: String

    //TODO add discussion

    companion object {
        @JvmStatic
        fun newInstance(currentGroupId: String): MemberControls {
            val fragment = MemberControls()
            val args = Bundle()
            args.putString("groupId", currentGroupId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater.inflate(R.layout.member_controls, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        groupId = arguments.getString("groupId")
        currentGroupRef = FirebaseDatabase.getInstance().reference.child("groups").child(groupId)

        openDiscussionButton.setOnClickListener {
            startActivity(DiscussionActivity.getStartIntent(activity, groupId))
        }

        inviteMembersButton.setOnClickListener {
            startInvitingMembers()
        }

        leaveGroupButton.setOnClickListener {
            popLeaveGroupConfirmationDialog()
        }

    }

    private fun startInvitingMembers() {
        startActivity(InviteMembersActivity.getStartIntent(activity, groupId))
    }

    private fun popLeaveGroupConfirmationDialog() {
        val builder = AlertDialog.Builder(activity)
        //TODO string resources
        builder.setTitle("Leave Group?")
                .setMessage("Are you sure you want to leave this group?")
                .setPositiveButton("Yes", { _, _ -> leaveGroup() })
                .setNegativeButton("No", null)
        builder.show()
    }

    private fun leaveGroup() {
        //Removing userId from group
        currentGroupRef.child(FIREBASE_TAG_MEMBER_IDS).child(General.currentUserId).removeValue()
        //Removing groupId from user
        currentUserRef.child(FIREBASE_TAG_GROUP_IDS).child(groupId).removeValue()
        //TODO string resources
        Toast.makeText(activity, "You have left the group", Toast.LENGTH_SHORT).show()
        refresh()
    }

    private fun refresh() {
        val intent = activity.intent
        activity.finish()
        startActivity(intent)
    }

}