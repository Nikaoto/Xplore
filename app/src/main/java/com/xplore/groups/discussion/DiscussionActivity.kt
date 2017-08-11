package com.xplore.groups.discussion

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.xplore.R

/**
 * Created by Nikaoto on 8/11/2017.
 * TODO write description of this class - what it does and why.
 */

class DiscussionActivity : Activity() {

    //Firebase
    val F_GROUPS = "groups"
    private lateinit var currentGroupRef: DatabaseReference
    //
    private lateinit var groupId: String

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String): Intent {
            return Intent(context, DiscussionActivity::class.java)
                    .putExtra("groupId", groupId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        groupId = intent.getStringExtra("groupId")
        currentGroupRef = FirebaseDatabase.getInstance().getReference("$F_GROUPS/$groupId")

        setContentView(R.layout.discussion)
        startListeningForMessages()
    }

    private fun startListeningForMessages() {

    }

    private fun stopListeningForMessages() {

    }

    override fun onStop() {
        super.onStop()
        stopListeningForMessages()
    }
}