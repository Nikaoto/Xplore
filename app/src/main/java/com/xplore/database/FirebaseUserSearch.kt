package com.xplore.database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xplore.user.UserCard

/**
 * Created by Nika on 8/6/2017.
 * TODO write description of this class - what it does and why.
 */

class FirebaseUserSearch(var displayUserCards: ArrayList<UserCard>, val displayAction: () -> Unit) {

    //Firebase final values
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")
    private val FIREBASE_FNAME_TAG = "fname"
    private val FIREBASE_LNAME_TAG = "lname"

    private fun getUsersRefQuery(tag: String, value: String)
            = usersRef.orderByChild(tag).startAt(value).endAt(value + "\uf8ff")
    private fun getFnameQuery(fname: String) = getUsersRefQuery(FIREBASE_FNAME_TAG, fname)
    private fun getLnameQuery(lname: String) = usersRef.orderByChild(FIREBASE_LNAME_TAG).startAt(lname).endAt(lname+"\uf8ff")

    private var dataFound = false

    fun prepareForSearch() {
        dataFound = false
    }

    //Loads users with lname and filters with fname
    fun loadUsersWithFullName(fname: String, lname: String) {
        getLnameQuery(lname).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
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
                    if (dataFound) {
                        displayAction()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) { }
        })
    }

    //Takes one query and searches them in firebase with given tags
    fun loadUsersWithTags(query: String, tag: String, tag2: String, resummon: Boolean) {
        getUsersRefQuery(tag, query).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    for (userSnapshot in dataSnapshot.children) {
                        val userCard = userSnapshot.getValue(UserCard::class.java)
                        if (userCard != null) {
                            userCard.id = userSnapshot.key
                            displayUserCards.add(userCard)
                            dataFound = true
                        }
                    }
                }
                if (resummon) {
                    loadUsersWithTags(query, tag2, "", false)
                } else if (dataFound) {
                    displayAction()
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }
}