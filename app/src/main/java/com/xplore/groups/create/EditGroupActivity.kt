package com.xplore.groups.create

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.MemberListAdapter
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.database.DBManager
import com.xplore.groups.Group
import com.xplore.maps.MapActivity
import com.xplore.user.User

import kotlinx.android.synthetic.main.create_group.*

/**
 * Created by Nikaoto on 8/13/2017.
 * TODO write description of this class - what it does and why.
 */

class EditGroupActivity : Activity() {

    private val dbManager: DBManager by lazy { DBManager(this)}

    //Firebase
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private lateinit var groupId: String
    private lateinit var currentGroupRef: DatabaseReference

    //Activity codes
    private val SEARCH_DESTINATION_REQ_CODE = 1
    private val SELECT_FROM_MAP_REQ_CODE = 2
    private val INVITE_USERS_REQ_CODE = 4

    private lateinit var currentGroup: Group

    private val joinedMembers = ArrayList<User>()
    private val joinedMemberIds = ArrayList<String>()
    private val invitedMembers = ArrayList<User>()
    private val invitedMemberIds = ArrayList<String>()

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
        joinedMembersLayout.visibility = View.VISIBLE

        //Setting up RecyclerViews
        joinedMemberList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        invitedMemberList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        getGroupData()
    }

    private fun getGroupData() {
        currentGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val group = dataSnapshot.getValue(Group::class.java)
                    if (group != null) {
                        currentGroup = group
                        if (currentGroup.member_ids == null) {
                            currentGroup.member_ids = HashMap<String, Boolean>()
                        }
                        if (currentGroup.invited_member_ids == null) {
                            currentGroup.invited_member_ids = HashMap<String, Boolean>()
                        }
                        populateJoinedMemberList()
                        populateInvitedMemberList()
                        fillFields()
                        initClickEvents()
                    } else {
                        printError()
                    }
                } else {
                    printError()
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    private fun populateJoinedMemberList() {
        //Setting up RecyclerView
        joinedMemberList.adapter = MemberListAdapter(this, joinedMembers, true, joinedMemberIds)
        //TODO check for member removal

        //Checking if list is empty
        if (currentGroup.member_ids.isNotEmpty()) {
            joinedMemberIds.addAll(currentGroup.member_ids.keys)

            //Getting member info
            for (mId in currentGroup.member_ids) {
                usersRef.child(mId.key).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        dataSnapshot?.let {
                            val tempUser = dataSnapshot.getValue(User::class.java)
                            tempUser?.let {
                                tempUser.id = dataSnapshot.key
                                joinedMembers.add(tempUser)
                                joinedMemberList.adapter.notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {}
                })
            }
        }
    }

    private fun populateInvitedMemberList() {
        //Setting up RecyclerView
        invitedMemberList.adapter = MemberListAdapter(this, invitedMembers, true, invitedMemberIds)
        //TODO check for invite removal

        for (mId in invitedMemberIds) {
            currentGroup.invited_member_ids.put(mId, true)
        }

        //Checking if empty
        if (currentGroup.invited_member_ids != null
                && currentGroup.invited_member_ids.isNotEmpty()) {
            invitedMemberIds.addAll(currentGroup.invited_member_ids.keys)

            //Getting member info
            for (mId in currentGroup.invited_member_ids) {
                usersRef.child(mId.key).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        dataSnapshot?.let {
                            val tempUser = dataSnapshot.getValue(User::class.java)
                            tempUser?.let {
                                tempUser.id = dataSnapshot.key
                                invitedMembers.add(tempUser)
                                invitedMemberList.adapter.notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {}
                })
            }
        }
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
        groupNameEditText.setText(currentGroup.name)
        //Configuring Image
        if (currentGroup.destination_id != Group.DESTINATION_DEFAULT) {
            //Setting OnClickListener
            groupImageView.setOnClickListener {
                General.openReserveInfoFragment(currentGroup.destination_id, this)
            }
            //Loading image
            dbManager.openDataBase()
            Picasso.with(this)
                    .load(dbManager.getImageId(currentGroup.destination_id))
                    .into(groupImageView)
            dbManager.close()
        } else {
            //Setting OnClickListener
            groupImageView.setOnClickListener {
                startActivity(MapActivity.getStartIntent(this, true, currentGroup.name,
                        currentGroup.destination_latitude, currentGroup.destination_longitude))
            }
            //Loading image
            Picasso.with(this).load(currentGroup.group_image_url).into(groupImageView)
        }

        //Start date
        startDateTextView.text = General.putSlashesInDate(currentGroup.start_date)
        //Start time
        startTimeTextView.text = General.putColonInTime(currentGroup.start_time)

        //End date
        endDateTextView.text = General.putSlashesInDate(currentGroup.end_date)
        //end time
        endTimeTextView.text = General.putColonInTime(currentGroup.end_time)

        //Experience
        if (currentGroup.experienced) {
            radioGroup.check(R.id.yes_rb)
        } else {
            radioGroup.check(R.id.no_rb)
        }

        //Description
        descriptionEditText.setText(currentGroup.extra_info)
        //Preferences
        preferencesEditText.setText(currentGroup.group_preferences)
    }

    private fun initClickEvents() {
        //Invite members
        inviteButton.setOnClickListener {
            invitedMemberIds.clear()
            startActivityForResult(SearchUsersActivity.getStartIntent(this, invitedMemberIds),
                    INVITE_USERS_REQ_CODE)
        }

        //Done
        doneButton.setOnClickListener {
            if (fieldsValid()) {
                uploadGroup()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    INVITE_USERS_REQ_CODE -> {
                        currentGroup.invited_member_ids.clear()
                        val invitedMemberIds = data.getStringArrayListExtra("invitedMemberIds")
                        for (mId in invitedMemberIds) {
                            currentGroup.invited_member_ids.put(mId, true)
                        }
                        populateInvitedMemberList()
                    }

                    SEARCH_DESTINATION_REQ_CODE -> {

                    }

                    SELECT_FROM_MAP_REQ_CODE -> {

                    }
                }
            }
        }
    }

    private fun fieldsValid(): Boolean {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(R.string.okay, null)
        return true
    }

    private fun uploadGroup() {
        Log.i("brejk", "uploading group!")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}