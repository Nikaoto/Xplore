package com.xplore.groups.my

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xplore.R
import com.xplore.groups.Group
import com.xplore.groups.GroupCard
import com.xplore.user.UserCard

/**
 * Created by Nika on 7/14/2017.
 *
 * აღწერა:
 * ეს კლასი აჩვენებს ჯგუფებს, რომელშიც მომხმარებელი გაწევრიანებულია.
 *
 * Description:
 * This class displays the currently joined groups of the user.
 *
 */

class MyGroupsFragment() : Fragment() {

    //Firebase
    val FIREBASE_START_DATE_TAG = "start_date"
    val FIREBASE_MEMBER_IDS_TAG = "member_ids"
    val firebaseGroupsRef = FirebaseDatabase.getInstance().reference.child("groups")
    val firebaseUsersRef = FirebaseDatabase.getInstance().reference.child("users")

    val groupIds = ArrayList<String>()
    val groupCards = ArrayList<GroupCard>()
    val userCards = ArrayList<UserCard>()

    constructor(groupIds: ArrayList<String>) : this() {
        this.groupIds.addAll(groupIds)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInst: Bundle?)
            = inflater.inflate(R.layout.my_groups, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

    }

    fun loadGroups(groupIds: ArrayList<String>) {
        for (groupId in groupIds) {
            firebaseGroupsRef.child(groupId).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            if (dataSnapshot != null) {
                                val group = dataSnapshot.getValue(Group::class.java)
                                if (group != null) {
                                    //group.
                                } else { printError() }
                            } else { printError() }
                        }

                        override fun onCancelled(p0: DatabaseError?) { }
                    }
            )
        }
    }

    fun loadLeaderInfo() {

    }

    fun printError() {
        Toast.makeText(activity, "Error loading data", Toast.LENGTH_SHORT).show()
    }

}