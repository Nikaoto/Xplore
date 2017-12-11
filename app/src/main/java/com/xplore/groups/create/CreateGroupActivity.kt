package com.xplore.groups.create

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xplore.*
import com.xplore.General.currentUserId
import com.xplore.R
import com.xplore.base.BaseAppCompatActivity
import com.xplore.database.DBManager
import com.xplore.groups.Group
import com.xplore.maps.GroupMapActivity
import com.xplore.maps.SetDestinationMapActivity
import com.xplore.reserve.ReserveInfoActivity
import com.xplore.user.User
import kotlinx.android.synthetic.main.create_group.*
import java.util.ArrayList
import kotlin.collections.HashMap
import com.xplore.util.FirebaseUtil.groupsRef
import com.xplore.util.FirebaseUtil.getUserRef
import com.xplore.util.FirebaseUtil.getCurrentUserRef
import com.xplore.util.FirebaseUtil.F_GROUP_IDS
import com.xplore.util.FirebaseUtil.F_INVITED_GROUP_IDS
import com.xplore.util.MapUtil

/**
 * Created by Nikaoto on 2/18/2017.

 * აღწერა:
 * ეს ფრაგმენტი იხსნება როცა მომხმარებელი ახალ გუნდს ქმნის. ეს კლასი ამოწმებს მომხმარებლის შეცდომებს
 * ფორმის შევსებისას და "დასტურზე" დაჭერის შემდგომ ტვირთავს ახალ გუნდს Firebase-ს ბაზაში

 * Description:
 * This fragment opens when user is creating a group. This class checks for any errors in user's
 * group info and with "Done" uploads group info to groups Fireabase Database

 */

open class CreateGroupActivity : BaseAppCompatActivity(), DatePickerDialog.OnDateSetListener {

    //Database
    private val dbManager: DBManager by lazy { DBManager(this) }

    //Firebase
    private val joinedGroupsRef: DatabaseReference by lazy {
        getCurrentUserRef().child(F_GROUP_IDS)
    }

    companion object {
        //Request codes
        const val SEARCH_DESTINATION_REQ_CODE = 1
        const val SELECT_FROM_MAP_REQ_CODE = 2
        const val SET_MEETUP_LOCATION_REQ_CODE = 3
        const val INVITE_USERS_REQ_CODE = 4

        //Limits and restrictions to fields
        const val EXPERIENCE_ANS_DEFAULT = -1
        const val EXPERIENCE_ANS_NO = 0
        const val EXPERIENCE_ANS_YES = 1
        const val G_PREFS_CHAR_MAX = 2000
        const val G_PREFS_CHAR_MIN = 0
        const val E_INFO_CHAR_MAX = 2000
        const val E_INFO_CHAR_MIN = 0

        //MISC
        const val MEETUP_MARKER_COLOR = "green"

        //Tags for date and time pickers
        const val SELECTION_NONE = ""
        const val SELECTION_START = "start"
        const val SELECTION_END = "end"

        @JvmStatic
        fun getStartIntent(context: Context) = Intent(context, CreateGroupActivity::class.java)
    }

    //Setting chosen answer and destination to default
    private var chosenDestId = Group.DESTINATION_DEFAULT
    private var experienceAns = EXPERIENCE_ANS_DEFAULT

    private var destinationLat = 0.0
    private var destinationLng = 0.0

    private var meetupLat = 0.0
    private var meetupLng = 0.0

    private var selecting = SELECTION_NONE

    private var groupImageUrl = ""
    private var groupName = ""
    private var groupPrefs = ""
    private var extraInfo = ""

    private var invitedMemberIds = ArrayList<String>()

    val date = HikeDate()

    //TODO remove dependencies and inner objects/classes as much as possible

    init {
        TimeManager.refreshGlobalTimeStamp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)

        setTitle(R.string.activity_create_group_title)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initMemberRecyclerView()
        initClickEvents()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    private fun initMemberRecyclerView() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        invitedMemberList.setHasFixedSize(true)
        invitedMemberList.layoutManager = layoutManager
    }

    private fun initClickEvents() {

        groupNameHelpButton.setOnClickListener {
            showHelp(R.string.group_name, R.string.group_name_help, R.string.okay)
        }

        chooseDestinationButton.setOnClickListener {
            showDestinationDialog()
        }

        groupImageView.setOnClickListener {
            if (chosenDestId != Group.DESTINATION_DEFAULT) {
                startActivity(ReserveInfoActivity.getStartIntent(this, chosenDestId))
            } else if (groupImageUrl.isNotEmpty()) {
                if (destinationLat != 0.0) {
                    GroupMapActivity.newIntent(
                            this@CreateGroupActivity,
                            true,
                            resources.getString(R.string.destination),
                            destinationLat,
                            destinationLng
                    )
                }
            }
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

        inviteButton.setOnClickListener {
            startActivityForResult(SearchUsersActivity.getStartIntent(this, invitedMemberIds),
                    INVITE_USERS_REQ_CODE)
        }

        prefs_help.setOnClickListener {
            showHelp(R.string.group_preferences, R.string.group_prefs_help, R.string.okay)
        }

        description_help.setOnClickListener {
            showHelp(R.string.extra_info, R.string.extra_info_help, R.string.okay)
        }

        doneButton.setOnClickListener {
            General.hideKeyboard(this)
            getDescriptions()
            if (checkFields()) {
                val key = groupsRef.push().key
                uploadGroupData(key)
                addLeaderToGroup(key)
                sendInvites(key)
                Toast.makeText(this, R.string.group_created, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        radioGroup.setOnCheckedChangeListener { _, i ->
            if (i == R.id.yes_rb)
                experienceAns = EXPERIENCE_ANS_YES
            else if (i == R.id.no_rb)
                experienceAns = EXPERIENCE_ANS_NO
        }
    }

    private fun startSettingMeetupLocation() {
        if (meetupLat != 0.0 && meetupLng != 0.0) {
            startActivityForResult(
                    SetDestinationMapActivity.getStartIntent(this,
                            resources.getString(R.string.meetup_location),
                            meetupLat, meetupLng),
                    SET_MEETUP_LOCATION_REQ_CODE)
        } else {
            startActivityForResult(SetDestinationMapActivity.getStartIntent(this),
                    SET_MEETUP_LOCATION_REQ_CODE)
        }
    }

    private fun showDestinationDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.activity_choose_destination_title)
                .setMessage(R.string.choose_from)
                .setPositiveButton(R.string.activity_library_title) {_, _ ->
                    startActivityForResult(
                            Intent(this@CreateGroupActivity, SearchDestinationActivity::class.java),
                            SEARCH_DESTINATION_REQ_CODE)
                }
                .setNegativeButton(R.string.activity_maps_title) {_, _ ->
                    if (destinationLat != 0.0 && destinationLng != 0.0) {
                        startActivityForResult(
                                SetDestinationMapActivity.getStartIntent(this,
                                        resources.getString(R.string.destination),
                                        destinationLat, destinationLng),
                                SELECT_FROM_MAP_REQ_CODE)
                    } else {
                        startActivityForResult(SetDestinationMapActivity.getStartIntent(this),
                                SELECT_FROM_MAP_REQ_CODE)
                    }
                }
                .create().show()
    }

    private fun showDatePicker(code: String) {
        if (TimeManager.globalTimeStamp != 0L) {
            selecting = code
            DatePickerFragment(this@CreateGroupActivity, TimeManager.globalTimeStamp, 0)
                    .show(fragmentManager, "")
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

    public override fun onResume() {
        super.onResume()

        if (!General.isNetConnected(this)) {
            General.createNetErrorDialog(this)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    SEARCH_DESTINATION_REQ_CODE -> {
                        chosenDestId = data.getIntExtra("chosen_destination_id",
                                Group.DESTINATION_DEFAULT)

                        dbManager.openDataBase()
                        Picasso.with(this)
                                .load(dbManager.getImageId(chosenDestId))
                                .into(groupImageView)
                        dbManager.close()
                    }

                    SELECT_FROM_MAP_REQ_CODE -> {
                        chosenDestId = Group.DESTINATION_DEFAULT

                        //TODO replace all 0.0 s with MapUtil.DEFAULT_LAT_LNG
                        //Getting image
                        destinationLat = data.getDoubleExtra(SetDestinationMapActivity.RESULT_DEST_LAT, 0.0)
                        destinationLng = data.getDoubleExtra(SetDestinationMapActivity.RESULT_DEST_LNG, 0.0)

                        //Checking if data retrieval failed
                        if (destinationLat != 0.0 || destinationLng != 0.0) {
                            groupImageUrl = MapUtil.getMapUrl(destinationLat, destinationLng)

                            Picasso.with(this).load(groupImageUrl).into(groupImageView)
                        } else {
                            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                        }
                    }

                    SET_MEETUP_LOCATION_REQ_CODE -> {
                        //TODO replace all 0.0 s with MapUtil.DEFAULT_LAT_LNG
                        //Getting chosen meetup location
                        meetupLat = data.getDoubleExtra(SetDestinationMapActivity.RESULT_DEST_LAT, 0.0)
                        meetupLng = data.getDoubleExtra(SetDestinationMapActivity.RESULT_DEST_LNG, 0.0)

                        //Checking if locations aren't 0.0
                        if (meetupLat != 0.0 || meetupLng != 0.0) {
                            Picasso.with(this)
                                    .load(MapUtil.getMeetupMapUrl(meetupLat, meetupLng))
                                    .into(meetupLocationImageView)
                        } else {
                            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                        }
                    }

                    INVITE_USERS_REQ_CODE -> {
                        invitedMemberIds = data
                                .getStringArrayListExtra(SearchUsersActivity.ARG_INVITED_MEMBER_IDS)
                        populateMembersList(invitedMemberIds)
                    }
                }
            }
        }
    }

    private fun ArrayList<String>.toMap(): HashMap<String, Boolean> {
        val temp = HashMap<String, Boolean>(this.size)
        for(item in this) {
            temp.put(item, true)
        }
        return temp
    }

    //Creates uploadable group
    private fun createUploadGroup(key: String): UploadableGroup {
        //Member ids of the group
        val member_ids = HashMap<String, Boolean>(1)
        member_ids.put(currentUserId, true) //Adding leader to members list

        //get experience question
        val exp = experienceAns != EXPERIENCE_ANS_NO

        /* TODO when uploading group, add lowercase name as 'search name' (to enable
        case-insensitive searching) and 'display name' as inputted name. Only for Firebase though
         */
        return UploadableGroup(
                key, //Firebase Unique Group Key
                groupName,
                exp, //Group Experienced Boolean
                date.getStartDate(), //Start Date
                date.startTime, //Start Time
                date.getEndDate(), //End Date
                date.endTime,
                chosenDestId, //Chosen Destination Id
                destinationLat, destinationLng, //Destination location
                meetupLat, meetupLng, //Meetup location
                groupImageUrl, //Image url
                extraInfo, //Group Extra Info
                groupPrefs, //Group Preferences
                member_ids, //Group Member Ids (only the leader)
                invitedMemberIds.toMap()) //Invited members
    }

    private fun addLeaderToGroup(key: String) {
        joinedGroupsRef.child("/" + key).setValue(true)
    }

    private fun uploadGroupData(key: String) {
        //Group
        val groupData = createUploadGroup(key).toMap()
        val groupUpdates = HashMap<String, Any>()
        groupUpdates.put("/" + key, groupData)
        groupsRef.updateChildren(groupUpdates)
    }

    private fun sendInvites(groupId: String) {
        for (memberId in invitedMemberIds) {
            getUserRef(memberId).child(F_INVITED_GROUP_IDS).child("/" + groupId).setValue(true)
        }
    }

    private fun populateMembersList(memberIds: ArrayList<String>) {
        val membersToDisplay = ArrayList<User>(memberIds.size)
        var memberCount = memberIds.size
        for (memberId in memberIds) {
            getUserRef(memberId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot?) {
                    if (userSnapshot != null) {
                        val member = userSnapshot.getValue(User::class.java)
                        member?.id = userSnapshot.key
                        if (member != null) {
                            membersToDisplay.add(member)
                            memberCount--
                            if (memberCount == 0) {
                                //Display list
                                invitedMemberList.visibility = View.VISIBLE
                                val adapter = MemberListAdapter(this@CreateGroupActivity, membersToDisplay, true, invitedMemberIds)
                                invitedMemberList.adapter = adapter
                            }
                        }
                    }
                }
                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }

    private fun getDescriptions() {
        if (groupNameEditText.text.toString().isEmpty()) {
            dbManager.openDataBase()
            groupName = dbManager.getStr(chosenDestId, DBManager.NAME)
        } else {
            groupName = groupNameEditText.text.toString()
        }
        groupPrefs = preferencesEditText.text.toString()
        extraInfo = descriptionEditText.text.toString()
    }

    private fun showHelp(title: Int, text: Int, butt_text: Int) {
        val alert = AlertDialog.Builder(this)
                .setMessage(resources.getString(text))
                .setTitle(resources.getString(title))
                .setCancelable(false)
                .setPositiveButton(resources.getString(butt_text), null)
        alert.show()
    }

    private fun checkFields(): Boolean {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(R.string.okay, null)

        if (chosenDestId == Group.DESTINATION_DEFAULT && groupImageUrl.isEmpty()) {
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
        } else if (experienceAns == EXPERIENCE_ANS_DEFAULT) {
            builder.setMessage(R.string.exp_field_incomplete)
                    .show()
            return false
        } else if (groupPrefs.length < G_PREFS_CHAR_MIN || extraInfo.length < E_INFO_CHAR_MIN) {
            builder.setMessage(R.string.text_field_incomplete)
                    .show()
            return false
        } else if (date.getStartDate() < General.getDateInt(TimeManager.globalTimeStamp)
                || date.getEndDate() < General.getDateInt(TimeManager.globalTimeStamp)) {
            builder.setMessage(R.string.date_past_invalid)
                    .show()
            return false
        } else {
            return true
        }
    }
}