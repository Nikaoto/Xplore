package com.xplore.groups.create

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xplore.*
import com.xplore.R
import com.xplore.base.BaseActivity
import com.xplore.database.DBManager
import com.xplore.groups.Group
import com.xplore.groups.create.CreateGroupActivity.Companion.E_INFO_CHAR_MAX
import com.xplore.groups.create.CreateGroupActivity.Companion.E_INFO_CHAR_MIN
import com.xplore.groups.create.CreateGroupActivity.Companion.G_PREFS_CHAR_MAX
import com.xplore.groups.create.CreateGroupActivity.Companion.G_PREFS_CHAR_MIN
import com.xplore.groups.create.CreateGroupActivity.Companion.INVITE_USERS_REQ_CODE
import com.xplore.groups.create.CreateGroupActivity.Companion.SEARCH_DESTINATION_REQ_CODE
import com.xplore.groups.create.CreateGroupActivity.Companion.SELECTION_END
import com.xplore.groups.create.CreateGroupActivity.Companion.SELECTION_NONE
import com.xplore.groups.create.CreateGroupActivity.Companion.SELECTION_START
import com.xplore.groups.create.CreateGroupActivity.Companion.SELECT_FROM_MAP_REQ_CODE
import com.xplore.groups.create.CreateGroupActivity.Companion.SET_MEETUP_LOCATION_REQ_CODE
import com.xplore.maps.SetDestinationMapActivity
import com.xplore.user.User
import kotlinx.android.synthetic.main.create_group.*

/**
 * Created by Nikaoto on 8/13/2017.
 * TODO write description of this class - what it does and why.
 */

class EditGroupActivity : BaseActivity(), DatePickerDialog.OnDateSetListener {

    //TODO extend CreateGroupActivity and just set every field in onCreate()

    private val dbManager: DBManager by lazy { DBManager(this)}

    //Firebase
    private val F_INVITED_GROUP_IDS = "invited_group_ids"
    private val F_GROUP_IDS = "group_ids"
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private lateinit var groupId: String
    private lateinit var currentGroupRef: DatabaseReference

    private lateinit var currentGroup: Group

    private val joinedMembers = ArrayList<User>()
    private val joinedMemberIds = ArrayList<String>()
    private val invitedMembers = ArrayList<User>()
    private val invitedMemberIds = ArrayList<String>()

    //Used to find out which members were uninvited/removed
    private val initialMemberIds = ArrayList<String>()
    private val initialInvitedMemberIds = ArrayList<String>()


    private val date = Date()
    private var selecting = SELECTION_NONE

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String)
                = Intent(context, EditGroupActivity::class.java).putExtra("groupId", groupId)
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
        joinedMemberList.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        invitedMemberList.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        getGroupData()
    }

    private fun getGroupData() {
        currentGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val group = dataSnapshot.getValue(Group::class.java)
                    if (group != null) {
                        //Getting group
                        currentGroup = group
                        currentGroup.group_id = dataSnapshot.key

                        //Getting members and setting initial member ids
                        if (currentGroup.member_ids != null) {
                            initialMemberIds.addAll(currentGroup.member_ids.keys)
                        } else {
                            currentGroup.member_ids = HashMap<String, Boolean>()
                        }
                        if (currentGroup.invited_member_ids != null) {
                            initialInvitedMemberIds.addAll(currentGroup.invited_member_ids.keys)
                        } else {
                            currentGroup.invited_member_ids = HashMap<String, Boolean>()
                        }

                        //Displaying members
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

    private fun ArrayList<String>.toMap(value: Boolean): HashMap<String, Boolean> {
        val output = HashMap<String, Boolean>(this.size)
        this.forEach {
            output.put(it, value)
        }
        return output
    }

    private fun populateJoinedMemberList() {
        //Setting up RecyclerView
        joinedMemberList.adapter = MemberListAdapter(this, joinedMembers, true, joinedMemberIds)
        if (joinedMemberIds.isEmpty()) {
            joinedMemberIds.addAll(currentGroup.member_ids.keys)
        }

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

    private fun populateInvitedMemberList() {
        //Setting up RecyclerView
        invitedMemberList.adapter = MemberListAdapter(this, invitedMembers, true, invitedMemberIds)

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
        Toast.makeText(applicationContext, R.string.error, Toast.LENGTH_SHORT).show()
        onBackPressed()
    }

    //When the destination is a reserve
    private val onReserveClickListener = View.OnClickListener {
        General.openReserveInfoFragment(currentGroup.destination_id, this@EditGroupActivity)
    }

    private val onMapClickListener = View.OnClickListener {
        startActivity(SetDestinationMapActivity.getStartIntent(this@EditGroupActivity,
                currentGroup.name,
                currentGroup.destination_latitude,
                currentGroup.destination_longitude))
    }

    //Fills the fields with gathered group data
    private fun fillFields() {
        groupNameEditText.setText(currentGroup.name)
        //Configuring Image
        if (currentGroup.destination_id != Group.DESTINATION_DEFAULT) {
            groupImageView.setOnClickListener(onReserveClickListener)
            //Loading image
            dbManager.openDataBase()
            Picasso.with(this)
                    .load(dbManager.getImageId(currentGroup.destination_id))
                    .into(groupImageView)
            dbManager.close()
        } else {
            groupImageView.setOnClickListener(onMapClickListener)
            //Loading image
            Picasso.with(this).load(currentGroup.group_image_url).into(groupImageView)
        }

        //Start date
        startDateTextView.text = DateUtil.putSlashesInDate(currentGroup.start_date)
        date.setStartDate(currentGroup.start_date)
        Log.i("brejk", "startDate ${date.getStartDate()}")

        //Start time
        startTimeTextView.text = General.putColonInTime(currentGroup.start_time)
        date.startTime = currentGroup.start_time
        Log.i("brejk", "startTime ${date.startTime}")

        //Meetup location
        if (currentGroup.hasMeetupLocation()) {
            Picasso.with(this)
                    .load(MapUtil.getMeetupMapUrl(currentGroup.meetup_latitude,
                            currentGroup.meetup_longitude))
                    .into(meetupLocationImageView)
        }

        //End date
        endDateTextView.text = DateUtil.putSlashesInDate(currentGroup.end_date)
        date.setEndDate(currentGroup.end_date)
        Log.i("brejk", "endDate ${date.getEndDate()}")

        //end time
        endTimeTextView.text = General.putColonInTime(currentGroup.end_time)
        date.endTime = currentGroup.end_time
        Log.i("brejk", "endTime ${date.endTime}")

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
        chooseDestinationButton.setOnClickListener {
            //Opens choose destination dialog
            AlertDialog.Builder(this)
                    .setTitle(R.string.activity_choose_destination_title)
                    .setMessage(R.string.choose_from)
                    .setPositiveButton(R.string.activity_library_title) {_, _ ->
                        startActivityForResult(
                                Intent(this@EditGroupActivity, SearchDestinationActivity::class.java),
                                SEARCH_DESTINATION_REQ_CODE)
                    }
                    .setNegativeButton(R.string.activity_maps_title) {_, _ ->
                        startActivityForResult(
                                SetDestinationMapActivity.getStartIntent(this, currentGroup.name,
                                        currentGroup.destination_latitude,
                                        currentGroup.destination_longitude),
                                SELECT_FROM_MAP_REQ_CODE)
                    }
                    .create().show()
        }

        startDateButton.setOnClickListener {
            showDatePicker(SELECTION_START)
        }

        startTimeButton.setOnClickListener {
            showTimePicker(SELECTION_START)
        }

        meetupLocationButton.setOnClickListener {
            startSettingMeetupLocation()
        }

        meetupLocationImageView.setOnClickListener {
            startSettingMeetupLocation()
        }

        endDateButton.setOnClickListener {
            showDatePicker(SELECTION_END)
        }

        endTimeButton.setOnClickListener {
            showTimePicker(SELECTION_END)
        }

        prefs_help.setOnClickListener {
            showHelp(R.string.group_preferences, R.string.group_prefs_help, R.string.okay)
        }

        description_help.setOnClickListener {
            showHelp(R.string.extra_info, R.string.extra_info_help, R.string.okay)
        }

        radioGroup.setOnCheckedChangeListener {
            _, i -> currentGroup.experienced = (i == R.id.yes_rb)
        }

        //Invite members
        inviteButton.setOnClickListener {
            invitedMemberIds.clear()
            startActivityForResult(SearchUsersActivity.getStartIntent(this, invitedMemberIds),
                    INVITE_USERS_REQ_CODE)
        }

        //Done
        doneButton.setOnClickListener {
            if (fieldsValid()) {
                gatherGroupData()
                uploadGroupData()
                sendInvites()
                checkRemovedMembers()
                checkUninvitedMembers()
                finish()
            }
        }
    }

    fun startSettingMeetupLocation() {
        if (currentGroup.hasMeetupLocation()) {
            startActivityForResult(
                    SetDestinationMapActivity.getStartIntent(this,
                            resources.getString(R.string.meetup_location),
                            currentGroup.meetup_latitude, currentGroup.meetup_longitude),
                    CreateGroupActivity.SET_MEETUP_LOCATION_REQ_CODE)
        } else {
            startActivityForResult(SetDestinationMapActivity.getStartIntent(this),
                    CreateGroupActivity.SET_MEETUP_LOCATION_REQ_CODE)
        }
    }

    private fun showDatePicker(code: String) {
        if (TimeManager.globalTimeStamp != 0L) {
            selecting = code
            DatePickerFragment(this@EditGroupActivity, TimeManager.globalTimeStamp, 0)
                    .show(fragmentManager, "")
        }
    }

    private fun showTimePicker(code: String) {
        selecting = code
        TimePickerFragment(onTimeSetListener).show(fragmentManager, "")
    }

    private val onTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        when (selecting) {
            SELECTION_START -> {
                selecting = SELECTION_NONE
                date.setStartTime(hourOfDay, minute)
                startTimeTextView.text = date.getStartTimeText()
            }
            SELECTION_END -> {
                selecting = SELECTION_NONE
                date.setEndTime(hourOfDay, minute)
                endTimeTextView.text = date.getEndTimeText()
            }
        }
    }

    override fun onDateSet(view: DatePicker, year: Int, receivedMonth: Int, day: Int) {
        val month  = receivedMonth + 1 //+1 is necessary because 0 is January
        when (selecting) {
            SELECTION_START -> {
                selecting = SELECTION_NONE
                date.setStartDate(year, month, day)
                startDateTextView.text = date.getStartDateString()
            }
            SELECTION_END -> {
                selecting = SELECTION_NONE
                date.setEndDate(year, month, day)
                endDateTextView.text = date.getEndDateString()
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
                        currentGroup.destination_id = data.getIntExtra("chosen_destination_id",
                                Group.DESTINATION_DEFAULT)

                        dbManager.openDataBase()
                        Picasso.with(this)
                                .load(dbManager.getImageId(currentGroup.destination_id))
                                .into(groupImageView)
                        dbManager.close()

                        groupImageView.setOnClickListener(onReserveClickListener)
                    }

                    SET_MEETUP_LOCATION_REQ_CODE -> {
                        // Getting location
                        currentGroup.meetup_latitude =
                                data.getDoubleExtra(SetDestinationMapActivity.RESULT_DEST_LAT, 0.0)
                        currentGroup.meetup_longitude =
                                data.getDoubleExtra(SetDestinationMapActivity.RESULT_DEST_LNG, 0.0)

                        // Check if data retrieval failed
                        if (currentGroup.hasMeetupLocation()) {
                            // Load image
                            Picasso.with(this)
                                    .load(MapUtil.getMeetupMapUrl(currentGroup.meetup_latitude,
                                            currentGroup.meetup_longitude))
                                    .into(meetupLocationImageView)

                            meetupLocationImageView.setOnClickListener {
                                startActivity(SetDestinationMapActivity.getStartIntent(
                                        this@EditGroupActivity,
                                        getString(R.string.meetup_location),
                                        currentGroup.meetup_latitude,
                                        currentGroup.meetup_longitude))
                            }
                        }
                    }

                    SELECT_FROM_MAP_REQ_CODE -> {
                        currentGroup.destination_id = Group.DESTINATION_DEFAULT

                        // Getting image
                        currentGroup.destination_latitude =
                                data.getDoubleExtra(SetDestinationMapActivity.RESULT_DEST_LAT, 0.0)
                        currentGroup.destination_longitude =
                                data.getDoubleExtra(SetDestinationMapActivity.RESULT_DEST_LNG, 0.0)

                        // Checking if data retrieval failed
                        if (currentGroup.hasDestinationLocation()){
                            currentGroup.group_image_url = MapUtil.getMapUrl(
                                    currentGroup.destination_latitude,
                                    currentGroup.destination_longitude
                            )

                            Picasso.with(this)
                                    .load(currentGroup.group_image_url)
                                    .into(groupImageView)
                            groupImageView.setOnClickListener(onMapClickListener)
                        } else {
                            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun fieldsValid(): Boolean {
        TimeManager.refreshGlobalTimeStamp()
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(R.string.okay, null)

        if (currentGroup.destination_id == Group.DESTINATION_DEFAULT
                && currentGroup.group_image_url.isEmpty()) {
            builder.setMessage(R.string.dest_field_incomplete)
                    .show()
            return false
        } else if (date.startYear == 0) {
            builder.setMessage(R.string.date_start_field_incomplete)
                    .show()
            return false
        } else if (date.endYear == 0) {
            builder.setMessage(R.string.date_end_field_incomplete)
                    .show()
            return false
        } else if (date.getStartDate() > date.getEndDate()) {
            builder.setMessage(R.string.date_invalid)
                    .show()
            return false
        } else if (date.getStartDate() == date.getEndDate() && date.startTime > date.endTime) {
            builder.setMessage(R.string.fix_start_end_times)
                    .show()
            return false
        } else if (currentGroup.group_preferences.length < G_PREFS_CHAR_MIN
                || currentGroup.group_preferences.length > G_PREFS_CHAR_MAX
                || currentGroup.extra_info.length < E_INFO_CHAR_MIN
                || currentGroup.extra_info.length > E_INFO_CHAR_MAX) {
            builder.setMessage(R.string.text_field_incomplete)
                    .show()
            return false
        } else if (date.getStartDate() < General.getDateInt(TimeManager.globalTimeStamp)
                || date.getEndDate() < General.getDateInt(TimeManager.globalTimeStamp)) {
            Log.i("brejk", "nowTime ${General.getDateInt(TimeManager.globalTimeStamp)}")
            builder.setMessage(R.string.date_past_invalid)
                    .show()
            return false
        } else {
            return true
        }
    }

    private fun gatherGroupData() {
        currentGroup.name = groupNameEditText.text.toString()
        currentGroup.extra_info = descriptionEditText.text.toString()
        currentGroup.group_preferences = preferencesEditText.text.toString()

        currentGroup.start_date = date.getStartDate()
        currentGroup.start_time = date.startTime
        currentGroup.end_date = date.getEndDate()
        currentGroup.end_time = date.endTime

        //Refreshing invited members list
        currentGroup.invited_member_ids.clear()
        currentGroup.invited_member_ids.putAll(invitedMemberIds.toMap(true))

        //Refreshing members list
        currentGroup.member_ids.clear()
        currentGroup.member_ids.putAll(joinedMemberIds.toMap(false))
        //Setting leader
        currentGroup.member_ids[General.currentUserId] = true
    }

    private fun uploadGroupData() {
        currentGroupRef.setValue(currentGroup)
    }

    private fun sendInvites() {
        for (memberId in currentGroup.invited_member_ids.keys) {
            usersRef.child(memberId).child(F_INVITED_GROUP_IDS).child("/" + groupId).setValue(true)
        }
    }

    //Finds out which members were removed and sets Firebase values accordingly
    private fun checkRemovedMembers() {
        initialMemberIds.forEach {
            if (!currentGroup.member_ids.contains(it)) {
                usersRef.child(it).child(F_GROUP_IDS).child(groupId).removeValue()
            }
        }
    }

    //Finds out which invited members were removed and sets Firebase values accordingly
    private fun checkUninvitedMembers() {
        initialInvitedMemberIds.forEach {
            if (!currentGroup.invited_member_ids.contains(it)) {
                usersRef.child(it).child(F_INVITED_GROUP_IDS).child(groupId).removeValue()
            }
        }
    }

    private fun showHelp(title: Int, text: Int, butt_text: Int) {
        val alert = AlertDialog.Builder(this)
                .setMessage(resources.getString(text))
                .setTitle(resources.getString(title))
                .setCancelable(false)
                .setPositiveButton(resources.getString(butt_text), null)
        alert.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}