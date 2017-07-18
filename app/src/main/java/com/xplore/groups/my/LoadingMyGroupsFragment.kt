package com.xplore.groups.my

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xplore.General
import com.xplore.R
import com.xplore.user.User

/**
 * Created by Nika on 7/17/2017.
 * TODO write description of this class - what it does and why.
 */
class LoadingMyGroupsFragment : Fragment() {

    val firebaseUsersRef = FirebaseDatabase.getInstance().reference.child("users")

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        //Loads joined group Ids for current user
        val query = firebaseUsersRef.child(General.currentUserId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        if (user.group_ids != null) {
                            if (user.group_ids.isNotEmpty()) {
                                loadMyGroupsLayout(user.group_ids)
                            } else { loadEmptyLayout() }
                        } else { loadEmptyLayout() }
                    } else { printError() }
                } else { printError() }
            }
            override fun onCancelled(p0: DatabaseError?) { }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInst: Bundle?)
            = inflater.inflate(R.layout.loading_layout, container, false)

    fun loadEmptyLayout() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EmptyGroupsFragment()).commit()
    }

    fun loadMyGroupsLayout(groupIds: ArrayList<String>) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyGroupsFragment(groupIds)).commit()
    }

    fun printError() {
        Toast.makeText(activity, "Error retrieving data", Toast.LENGTH_SHORT).show()
    }
}