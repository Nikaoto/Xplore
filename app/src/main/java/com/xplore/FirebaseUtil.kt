package com.xplore

import android.util.Log
import com.google.firebase.database.*

/**
 * Created by Nika on 9/7/2017.
 * TODO write description of this class - what it does and why.
 */

object FirebaseUtil {

    private const val TAG = "firebase_util"

    const val REP = 1

    // Main Nodes
    const val F_date = "date"
    const val F_USERS = "users"
    const val F_GROUPS = "groups"

    // Date nodes
    const val F_TIMESTAMP = "timestamp"

    // User nodes
    const val F_FNAME = "fname"
    const val F_LNAME = "lname"
    const val F_REPUTATION = "reputation"

    // Group nodes
    const val F_START_DATE = "start_date"
    const val F_END_DATE = "end_date"
    const val F_GRANTED_REPUTATION = "granted_reputation"


    @JvmField
    val usersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(F_USERS)

    @JvmStatic
    fun getUserRef(userId: String): DatabaseReference = usersRef.child(userId)

    @JvmStatic
    fun getCurrentUserRef() = getUserRef(General.currentUserId)

    @JvmStatic
    fun grantReputation(userId: String, reputationAmount: Int) {
        if (reputationAmount != 0) {
            val repRef = getUserRef(userId).child(F_REPUTATION)
            repRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if (dataSnapshot != null) {
                        val currentRep = dataSnapshot.getValue(Int::class.java)
                        if (currentRep != null) {
                            repRef.setValue(reputationAmount + currentRep)
                        } else {
                            Log.i(TAG, "grantReputation($userId,$reputationAmount): currentRep is null")
                        }
                    } else {
                        Log.i(TAG, "grantReputation($userId,$reputationAmount): dataSnapshot is null")
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }
}