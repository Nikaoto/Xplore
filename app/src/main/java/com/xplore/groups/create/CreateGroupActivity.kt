package com.xplore.groups.create

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.DatePicker
import android.widget.Toast

import com.google.firebase.database.FirebaseDatabase
import com.xplore.database.DBManager
import com.xplore.DatePickerFragment
import com.xplore.General
import com.xplore.MemberListAdapter
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.reserve.ReserveInfoActivity
import com.xplore.user.User

import java.util.ArrayList
import java.util.HashMap

import com.xplore.General.currentUserId
import kotlinx.android.synthetic.main.create_group.*

/**
 * Created by Nikaoto on 2/18/2017.

 * აღწერა:
 * ეს ფრაგმენტი იხსნება როცა მომხმარებელი ახალ გუნდს ქმნის. ეს კლასი ამოწმებს მომხმარებლის შეცდომებს
 * ფორმის შევსებისას და "დასტურზე" დაჭერის შემდგომ ტვირთავს ახალ გუნდს Firebase-ს ბაზაში

 * Description:
 * This fragment opens when user is creating a group. This class checks for any errors in user's
 * group info and with "Done" uploads group info to groups Fireabase Database

 */

class CreateGroupActivity : Activity(), DatePickerDialog.OnDateSetListener {

    //Firebase
    private var groupsRef = FirebaseDatabase.getInstance().reference.child("groups")

    //Activity Codes
    private val SEARCH_DESTINATION_ACTIVITY_CODE = 1
    private val INVITE_USERS_ACTIVITY_CODE = 4

    //Limits and restrictions to fields
    private val CHOSEN_DEST_DEFAULT = -1
    private val EXPERIENCE_ANS_DEFAULT = -1
    private val EXPERIENCE_ANS_NO = 0
    private val EXPERIENCE_ANS_YES = 1
    private val G_PREFS_CHAR_MAX = 200
    private val G_PREFS_CHAR_MIN = 0 //TODO add selection if user doesn't have prefs
    private val E_INFO_CHAR_MAX = 200
    private val E_INFO_CHAR_MIN = 5

    //Variables for finding out which date user is selecting (start / end)
    private val SELECTION_NONE = ""
    private val SELECTION_START = "start"
    private val SELECTION_END = "end"

    //Setting chosen answer and destination to default
    private var chosenDestId = CHOSEN_DEST_DEFAULT
    private var experienceAns = EXPERIENCE_ANS_DEFAULT

    //Needed to determine which date or time the user is choosing
    private var selectingDate = SELECTION_NONE
    private var selectingTime = SELECTION_NONE

    //Stores start/end dates and times
    private object date {
        //Start
        var startYear = 0
        var startMonth = 0
        var startDay = 0
        var startTime = 0

        //End
        var endYear = 0
        var endMonth = 0
        var endDay = 0
        var endTime = 0

        //Setting dates
        fun setStartDate(y: Int, m: Int, d: Int) {
            this.startYear = y
            this.startMonth = m
            this.startDay = m
        }
        fun setEndDate(y: Int, m: Int, d: Int) {
            this.endYear = y
            this.endMonth = m
            this.endDay = m
        }

        //Getting dates as longs
        private fun getDateLong(y: Int, m: Int, d: Int) = y*10000 + m*100 + d
        fun getStartDate() = getDateLong(startYear, startMonth, startDay)
        fun getEndDate() = getDateLong(endYear, endMonth, endDay)

        //Getting dates as strings (to display on TextViews)
        fun getDateString(y: Int, m: Int, d: Int) = "$y/$m/$d"
        fun getStartDateString() = getDateString(startYear, startMonth, startDay)
        fun getEndDateString() = getDateString(endYear, endMonth, endDay)

        //
    }

    private var groupPrefs: String = ""
    private var extraInfo: String = ""

    //TODO replace reserveButton with reserveCard

    private val dbManager: DBManager by lazy { DBManager(this) }

    companion object {
        @JvmStatic
        var invitedMembers = ArrayList<User>()

        @JvmStatic
        fun getStartIntent(context: Context): Intent {
            return Intent(context, CreateGroupActivity::class.java)
        }
    }

    init {
        invitedMembers.clear()

        //Refreshing server timeStamp
        TimeManager.refreshGlobalTimeStamp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)

        setTitle(R.string.activity_create_group_title)
        dbManager.openDataBase()

        initMemberRecyclerView()
        initClickEvents()
    }

    private fun initMemberRecyclerView() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        invitedMemberList.setHasFixedSize(true)
        invitedMemberList.layoutManager = layoutManager
    }

    private fun initClickEvents() {
        chooseReserveButton.setOnClickListener {
            val intent = Intent(this@CreateGroupActivity, SearchDestinationActivity::class.java)
            startActivityForResult(intent, SEARCH_DESTINATION_ACTIVITY_CODE)
        }

        reserveButton.setOnClickListener {
            if (chosenDestId != CHOSEN_DEST_DEFAULT)
                startActivity(ReserveInfoActivity.getStartIntent(this@CreateGroupActivity, chosenDestId))
        }

        startDateButton.setOnClickListener {
            if (TimeManager.globalTimeStamp != 0L) {
                selectingDate = SELECTION_START
                DatePickerFragment(this@CreateGroupActivity, TimeManager.globalTimeStamp, 0)
                        .show(fragmentManager, "startDate")
            }
        }

        startTimeButton.setOnClickListener {

        }

        endDateButton.setOnClickListener {
            if (TimeManager.globalTimeStamp != 0L) {
                selectingDate = SELECTION_END
                DatePickerFragment(this@CreateGroupActivity, TimeManager.globalTimeStamp, 0)
                        .show(fragmentManager, "endDate")
            }
        }

        endTimeButton.setOnClickListener {

        }

        inviteButton.setOnClickListener {
            val intent = Intent(this@CreateGroupActivity, SearchUsersActivity::class.java)
            startActivityForResult(intent, INVITE_USERS_ACTIVITY_CODE)
        }

        prefs_help!!.setOnClickListener {
            showHelp(R.string.group_preferences, R.string.group_prefs_help, R.string.okay,
                    resources)
        }

        extraInfo_help.setOnClickListener {
            showHelp(R.string.extra_info, R.string.extra_info_help, R.string.okay,
                    resources)
        }

        doneButton.setOnClickListener {
            GatherData()
            if (checkFields()) {
                uploadGroupData()
            }
        }

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i == R.id.yes_rb)
                experienceAns = EXPERIENCE_ANS_YES
            else if (i == R.id.no_rb)
                experienceAns = EXPERIENCE_ANS_NO
        }
    }

    public override fun onResume() {
        super.onResume()

        if (!General.isNetConnected(this)) {
            General.createNetErrorDialog(this)
        } else if (chosenDestId != CHOSEN_DEST_DEFAULT) {
            //TODO make a separate method for displaying the reserve
            reserveButton.setBackgroundResource(dbManager.getImageId(chosenDestId))
            reserveButton.text = dbManager.getStr(chosenDestId, DBManager.NAME, General.DB_TABLE)
        }
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        if (General.isNetConnected(this)) {
            if (selectingDate == SELECTION_START) {
                selectingDate = SELECTION_NONE
                date.setStartDate(year, month, day)
                startDateTextView.text = date.getStartDateString()
            } else if (selectingDate == SELECTION_END) {
                selectingDate = SELECTION_NONE
                date.setEndDate(year, month, day)
                endDateTextView.text = date.getEndDateString()
            }
        } else {
            General.createNetErrorDialog(this)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SEARCH_DESTINATION_ACTIVITY_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                chosenDestId = data.getIntExtra("chosen_destination_id", CHOSEN_DEST_DEFAULT)
            }
        } else if (requestCode == INVITE_USERS_ACTIVITY_CODE) {
            if (resultCode == Activity.RESULT_OK) {//TODO remove chosenMembers static and get intarray of UserIds from SearchUsersActivity. Return RESULT_CANCELED when no member selected
                if (data.getBooleanExtra("member_added", false)) {//checking if members added
                    PopulateMembersList()
                }
            }
        }
    }

    //Creates uploadable group
    private fun createUploadGroup(key: String): UploadableGroup {
        //getting member IDs
        val member_ids = ArrayList<String>()
        member_ids.add(currentUserId.toString())

        for (user in invitedMembers) {
            member_ids.add(user.id)
        }

        //get experience question
        val exp = experienceAns != EXPERIENCE_ANS_NO

        return UploadableGroup(
                key, //Firebase Unique Group Key
                exp, //Group Experienced Boolean
                date.getStartDate().toLong(), //Start Date
                date.getEndDate().toLong(), //End Date
                chosenDestId.toString(), //Chosen Destination Id
                extraInfo, //Group Extra Info
                groupPrefs, //Group Preferences
                member_ids)   //Group Member Ids
    }

    private fun uploadGroupData() {
        val key = groupsRef.push().key
        val groupData = createUploadGroup(key).toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates.put("/" + key, groupData)

        groupsRef.updateChildren(childUpdates)
        Toast.makeText(this, "Group Created", Toast.LENGTH_SHORT).show() //TODO add string resources
        General.HideKeyboard(this)
        finish()
    }

    private fun PopulateMembersList() {
        invitedMemberList.visibility = View.VISIBLE
        val adapter = MemberListAdapter(this, invitedMembers, true)
        invitedMemberList.adapter = adapter
    }

    private fun GatherData() {
        groupPrefs = groupPrefs_editText.text.toString()
        extraInfo = extraInfo_editText.text.toString()
    }

    private fun showHelp(title: Int, text: Int, butt_text: Int, resources: Resources) {
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

        if (chosenDestId == CHOSEN_DEST_DEFAULT) {
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
        } else if (experienceAns == EXPERIENCE_ANS_DEFAULT) {
            builder.setMessage(R.string.exp_field_incomplete)
                    .show()
            return false
        } else if (groupPrefs.length < G_PREFS_CHAR_MIN || groupPrefs.length > G_PREFS_CHAR_MAX
                || extraInfo.length < E_INFO_CHAR_MIN || extraInfo.length > E_INFO_CHAR_MAX) {
            builder.setMessage(R.string.text_field_incomplete)
                    .show()
            return false
        } else if (date.getStartDate() < General.getDateLong(TimeManager.globalTimeStamp) || date.getEndDate() < General.getDateLong(TimeManager.globalTimeStamp)) {
            builder.setMessage(R.string.date_past_invalid).show()
            return false
        } else {
            return true
        }
    }
}
