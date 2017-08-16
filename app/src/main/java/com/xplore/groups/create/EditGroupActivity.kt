package com.xplore.groups.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.*
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.groups.Group

/**
 * Created by Nikaoto on 8/13/2017.
 * TODO write description of this class - what it does and why.
 */
class EditGroupActivity : Activity() {

    private lateinit var groupId: String
    private lateinit var currentGroupRef: DatabaseReference

    private lateinit var currentGroup: Group

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String): Intent {
            return Intent(context, EditGroupActivity::class.java)
                    .putExtra("groupId", groupId)
        }
    }

    init {
        TimeManager.refreshGlobalTimeStamp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)

        groupId = intent.getStringExtra("groupId")
        currentGroupRef = FirebaseDatabase.getInstance().getReference("groups/$groupId")

        setTitle(R.string.edit_group)
        getGroupData()
    }

    private fun getGroupData() {
        currentGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val group = dataSnapshot.getValue(Group::class.java)
                    group?.let {
                        currentGroup = group
                    }
                } else {
                    printError()
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
        fillFields()
    }

    //Prints error and backs out
    private fun printError() {
        //TODO string resources
        Toast.makeText(applicationContext, "Error: Could not load group data",
                Toast.LENGTH_SHORT).show()
        onBackPressed()
    }

    //Fills the fields with gathered group data
    private fun fillFields() {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}