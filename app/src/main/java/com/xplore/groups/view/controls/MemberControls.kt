package com.xplore.groups.view.controls

import android.app.AlertDialog
import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import com.xplore.FirebaseUtil
import com.xplore.General
import com.xplore.R
import com.xplore.TimeManager
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

    //Firebase
    val F_MEMBER_IDS = "member_ids"
    val F_GROUP_IDS = "group_ids"
    val F_END_DATE = "end_date"
    val F_GRANTED_REPUTATION = "granted_reputation"
    val currentUserRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(General.currentUserId)
    private val currentGroupRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("groups").child(groupId)
    }

    private val groupId: String by lazy {
        arguments.getString("groupId")
    }

    init {
        TimeManager.refreshGlobalTimeStamp()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
            : View = inflater.inflate(R.layout.member_controls, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        checkReputationGranted()

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

    private fun checkReputationGranted() {
        currentGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    val repGranted = it.child(FirebaseUtil.F_GRANTED_REPUTATION).child(General.currentUserId)
                            .getValue(Boolean::class.java)
                    if (repGranted == null || !repGranted) {
                        // Check if hike finished
                        val endDate = it.child(FirebaseUtil.F_END_DATE).getValue(Int::class.java)
                        if (endDate != null) {
                            if (endDate <= TimeManager.intTimeStamp) {
                                // Grant reputation
                                FirebaseUtil.grantReputation(General.currentUserId, FirebaseUtil.REP)
                                currentGroupRef.child(FirebaseUtil.F_GRANTED_REPUTATION)
                                        .child(General.currentUserId).setValue(true)

                                General.toastReputationGain(activity, FirebaseUtil.REP)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    private fun startInvitingMembers() {
        startActivity(InviteMembersActivity.getStartIntent(activity, groupId))
    }

    private fun popLeaveGroupConfirmationDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.resources.getString(R.string.leave_group) + "?")
                .setMessage(R.string.leave_group_question)
                .setPositiveButton(R.string.yes, { _, _ -> leaveGroup() })
                .setNegativeButton(R.string.no, null)
        builder.show()
    }

    private fun leaveGroup() {
        //Removing userId from group
        currentGroupRef.child(F_MEMBER_IDS).child(General.currentUserId).removeValue()
        //Removing groupId from user
        currentUserRef.child(F_GROUP_IDS).child(groupId).removeValue()
        Toast.makeText(activity, R.string.group_left, Toast.LENGTH_SHORT).show()
        refresh()
    }

    private fun refresh() {
        val intent = activity.intent
        activity.finish()
        startActivity(intent)
    }

}