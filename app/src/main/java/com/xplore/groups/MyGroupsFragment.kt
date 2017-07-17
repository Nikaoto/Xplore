package com.xplore.groups

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import com.xplore.General
import com.xplore.R
import com.xplore.user.User

/**
 * Created by Nika on 7/14/2017.
 * TODO write description of this class - what it does and why.
 */

class MyGroupsFragment : Fragment() {

    val firebaseUsersReference = FirebaseDatabase.getInstance().reference.child("users")
    var empty = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInst: Bundle?): View {

        val query = firebaseUsersReference.orderByKey().equalTo(General.currentUserId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val user = dataSnapshot.getValue(User::class.java)

                } else {
                    Toast.makeText(activity, "Error retrieving data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(p0: DatabaseError?) { }
        })

        if (empty) {
            return inflater.inflate(R.layout.my_groups_empty, container, false)
        } else {
            return inflater.inflate(R.layout.my_groups, container, false)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!empty) {
            //TODO load current joined groups with GroupCardRecyclerViewAdapter
        }
    }
}