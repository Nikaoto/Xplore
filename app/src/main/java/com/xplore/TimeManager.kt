package com.xplore

import com.google.firebase.database.*
import java.util.*

/**
* Created by Nikaoto on 6/17/2017.
* TODO write description of this class - what it does and why.
*/

class TimeManager {
    init {
        refreshGlobalTimeStamp()
    }

    companion object {
        //UNIX timestamp used for current server (global) time
        var globalTimeStamp: Long = 0L

        //Integer timestamp used for calculating age and simple things...
        var intTimeStamp: Int = 0

        //Refreshes the timestamp
        fun refreshGlobalTimeStamp() {
            val ref = FirebaseDatabase.getInstance().reference
            val dateValue = HashMap<String, Any>()
            dateValue.put("timestamp", ServerValue.TIMESTAMP)
            ref.child("date").setValue(dateValue)
            val query = ref.child("date").child("timestamp")
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val temp = dataSnapshot.getValue(Long::class.java)
                    //Checking for null
                    if(temp == null) {
                        globalTimeStamp = 0L
                    } else {
                        globalTimeStamp = temp
                        setIntTimeStamp(temp)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    globalTimeStamp = 0L
                }
            })
        }

        fun setIntTimeStamp(timeStamp: Long) {
            val calendar = Calendar.getInstance()
            calendar.time = Date(timeStamp);
            val y = calendar.get(Calendar.YEAR)
            val m = calendar.get(Calendar.MONTH) + 1
            val d = calendar.get(Calendar.DAY_OF_MONTH)
            intTimeStamp = y*1000 + m*100 + d
        }
    }
}