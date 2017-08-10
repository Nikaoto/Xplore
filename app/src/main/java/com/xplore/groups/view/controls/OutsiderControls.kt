package com.xplore.groups.view.controls

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val F_INVITED_GROUP_IDS = "invited_group_ids"
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")
    lateinit private var currentGroupRef: DatabaseReference
    //
    lateinit private var groupId: String

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

        if (!arguments.getBoolean("awaitingRequest")) {
            groupId = arguments.getString("groupId")
            currentGroupRef = FirebaseDatabase.getInstance().getReference("$F_GROUPS_TAG/$groupId")
            joinGroupButton.setOnClickListener {
                sendJoinRequest(groupId)
            }
        } else {
            //Remove button
            joinGroupButton.isEnabled = false
            joinGroupButton.visibility = View.GONE
            //Show request sent text
            requestSentTextView.visibility = View.VISIBLE
        }
    }

    private fun sendJoinRequest(groupId: String) {
        //Adding current member id to invited member ids WITH FALSE VALUE
        currentGroupRef.child(F_INVITED_MEMBER_IDS).child(General.currentUserId).setValue(false)

        //Adding group id to member's invited group ids WITH FALSE VALUE
        usersRef.child(General.currentUserId).child(F_INVITED_GROUP_IDS).child(groupId)
                .setValue(false)

        refresh()
    }

    private fun refresh() {
        fragmentManager.beginTransaction().detach(this).attach(newInstance(groupId, true)).commit()
    }
}