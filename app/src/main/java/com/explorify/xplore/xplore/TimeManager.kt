package com.explorify.xplore.xplore

import android.util.Log
import com.google.firebase.database.*
import java.security.Timestamp
import java.util.*

/**
 * Created by Nika on 6/17/2017.
 */

class TimeManager {
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
                    //Log.println(Log.INFO, "brejk", "refreshGlobalStimeStamp()")

                    //Checking for null
                    if(temp == null) {
                        globalTimeStamp = 0L
                    } else {
                        globalTimeStamp = temp
                        setIntTimeStamp(temp)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) { }
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
    init {
        refreshGlobalTimeStamp()
    }
}