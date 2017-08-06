package com.xplore.database

import android.util.Log
import com.google.firebase.database.*
import com.xplore.user.UserCard

/**
 * Created by Nika on 8/6/2017.
 * TODO write description of this class - what it does and why.
 */

class FirebaseUserSearch(val displayUserCards: ArrayList<UserCard>, val displayAction: () -> Unit) {

    //Firebase final values
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")
    private val FIREBASE_FNAME_TAG = "fname"
    private val FIREBASE_LNAME_TAG = "lname"

    private fun getNameQuery(tag: String, name: String)
            = usersRef.orderByChild(tag).startAt(name).endAt(name+"\uf8ff")
    private fun getFnameQuery(fname: String) = getNameQuery(FIREBASE_FNAME_TAG, fname)
    private fun getLnameQuery(lname: String) = usersRef.orderByChild(FIREBASE_LNAME_TAG).startAt(lname).endAt(lname+"\uf8ff")

    private var dataFound = false

    //Loads users with lname and filters with fname
    fun loadUsersWithFullName(fname: String, lname: String, displayData: Boolean) {
        getLnameQuery(lname).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataFound = false
                if (dataSnapshot != null) {
                    Log.println(Log.INFO, "bloop", "snapshot != null")
                    for (userSnapshot in dataSnapshot.children) {
                        val userCard = userSnapshot.getValue(UserCard::class.java)
                        if (userCard != null) {
                            if (userCard.fname.toLowerCase().contains(fname.toLowerCase())) {
                                userCard.id = userSnapshot.key
                                displayUserCards.add(userCard)
                                dataFound = true
                            }
                        }
                    }
                    if (dataFound && displayData) {
                        displayAction()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) { }
        })
    }
}