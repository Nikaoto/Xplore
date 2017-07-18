package com.xplore.groups.search

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xplore.*
import com.xplore.database.DBManager
import com.xplore.groups.Group
import com.xplore.reserve.Icons
import com.xplore.reserve.ReserveInfoActivity
import com.xplore.user.User

import java.util.ArrayList

import kotlinx.android.synthetic.main.group_info.*
import kotlinx.android.synthetic.main.reserve_list_item.*

/**
* Created by Nikaoto on 2/12/2017.
* TODO write description of this class - what it does and why.
*/

class GroupInfoActivity : Activity() {

    //TODO convert to inferred types after testing
    private var group_id: String = ""
    private var reserveID: Int= 0 //TODO remove this and load submitted image or map image instead
    private var memberCount: Int = 0
    private val members = ArrayList<User>()

    //Firebase database references
    private val DBref = FirebaseDatabase.getInstance().reference
    private val groupsDBref = DBref.child("groups")
    private val usersDBref = DBref.child("users")

    //The variables which contain the current group/member info
    private var currentGroup = Group()
    private var tempMember = User()

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String, reserveId: Int)
                = Intent(context, GroupInfoActivity::class.java)
                        .putExtra("group_id", groupId)
                        .putExtra("reserve_id", reserveId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.group_info)
        //buildUserBase();

        resetVariables()
        receiveData()
        initMemberList()

        //reset the layout TODO remove after finishing testing and clear layouts
        resetLayout()

        loadGroupData(group_id)
        applyReserveData()
    }

    //Gets reserve data from local database and displays it on the reserve card
    private fun applyReserveData() {
        val dbManager = DBManager(this)
        dbManager.openDataBase()
        val tempReserveCard = dbManager.getReserveCard(reserveID)

        reserveCardView.setOnClickListener { startActivity(ReserveInfoActivity.getStartIntent(this, reserveID)) }
        reserveNameTextView.text = tempReserveCard.name
        groupImageView.setImageResource(tempReserveCard.imageId)
        reserveIconImageView.setImageResource(Icons.grey[tempReserveCard.iconId])
    }

    //TODO maybe remove?
    //Resets variables so a new group can be loaded from zero
    private fun resetVariables() {
        memberCount = 1
        members.clear()
    }

    //Receives group data from last intent
    private fun receiveData() {
        val intent = this.intent
        group_id = intent.getStringExtra("group_id")
        reserveID = intent.getIntExtra("reserve_id", 0)
    }

    private fun initMemberList() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        membersRecyclerView.setHasFixedSize(true)
        membersRecyclerView.layoutManager = layoutManager
    }

    private fun resetLayout() {
        //leader
        leaderFnameTextView.text = "-"
        leaderLnameTextView.text = "-"
        leaderAgeTextView.text = ""
        leaderTelTextView.text = ""
        leaderRepTextView.text = ""
        //group
        startDateTextView.text = ""
        endDateTextView.text = ""
        groupPrefsTextView.text = ""
        groupExtraInfoTextView.text = ""
    }

    /*
    //IN CASE WE'RE USING A SECOND FIREBASE DATABASE FOR USERS
    private void buildUserBase()
    {
    USERBASE_KEY = getResources().getString(R.string.user_firebase_key);
    USERBASE_APPID = getResources().getString(R.string.firebase_appid);
    USERBASE_URL = getResources().getString(R.string.user_firebase_url);
    userBaseOptions = new FirebaseOptions.Builder()
    .setApiKey(USERBASE_KEY)
    .setApplicationId(USERBASE_APPID)
    .setDatabaseUrl(USERBASE_URL)
    .build();
    try {
    if (FirebaseApp.getApps(getActivity()).get(1).equals(null)) {
    FirebaseApp.initializeApp(getActivity(), userBaseOptions, "userbase");
    } catch (IndexOutOfBoundsException e) {
    FirebaseApp.initializeApp(getActivity(), userBaseOptions, "userbase");
    }
    userBaseApp = FirebaseApp.getInstance("userbase");
    userDB = FirebaseDatabase.getInstance(userBaseApp);
    }
    */

    private fun loadGroupData(groupId: String) {
        val query = groupsDBref.child(groupId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                //Checking if group exists
                if (dataSnapshot != null) {
                    //creating the temporary (current) group
                    currentGroup = dataSnapshot.getValue(Group::class.java)!!
                    currentGroup.setGroup_id(dataSnapshot.key)
                    memberCount = currentGroup.getMember_ids()!!.size

                    for (memberId in currentGroup.getMember_ids()) {
                        getUserInfo(memberId)
                    }
                } else {//TODO string resources
                    Toast.makeText(applicationContext, "The group does not exist", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    //Gets user info from Firebase using userId
    private fun getUserInfo(userId: String) {
        val query = usersDBref.child(userId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                //Checking if member exists
                if (dataSnapshot!!.exists()) {
                    tempMember = dataSnapshot.getValue(User::class.java)!! //Getting member info
                    tempMember.setId(userId) //Setting user Id
                    members.add(tempMember) //Setting member info
                    memberCount-- //Iterating member index
                    if (memberCount == 0) { //Checking if member list retrieval finished
                        applyGroupData()
                    }
                } else {//TODO string resources
                    Toast.makeText(applicationContext, "There was an error retreiving the members", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    //Displays the already-retrieved data of the group
    private fun applyGroupData() {
        groupProgressBar.visibility = View.GONE

        val leader = members[0];
        //Displaying leader
        //Profile picture
        Picasso.with(this).invalidate(leader.getProfile_picture_url())
        Picasso.with(this)
                .load(leader.getProfile_picture_url())
                .transform(CircleTransformation(leaderImageView.width, leaderImageView.height))
                .into(leaderImageView)
        //Name
        leaderFnameTextView.text = leader.getFname()
        leaderLnameTextView.text = leader.getLname()
        //Age
        val age = General.calculateAge(TimeManager.globalTimeStamp, leader.getBirth_date())
        leaderAgeTextView.text = "${getString(R.string.age)}: $age"
        //Telephone
        leaderTelTextView.text = "${getString(R.string.tel)}: ${leader.getTel_num()}"
        //Reputation
        leaderRepTextView.text = leader.getReputation().toString()

        //Setting experienced icon
        if (currentGroup.isExperienced) {
            groupExpImageView.setImageResource(R.drawable.ic_check)
        } else {
            groupExpImageView.setImageResource(R.drawable.ic_x)
        }

        groupExpImageView.setOnClickListener {
            popExperienceInfoDialog()
        }

        startDateTextView.text = General.putSlashesInDate(currentGroup.getStart_date())
        endDateTextView.text = General.putSlashesInDate(currentGroup.getEnd_date())

        groupPrefsTextView.text = currentGroup.getGroup_preferences()
        groupExtraInfoTextView.text = currentGroup.getExtra_info()

        //Displaying members
        val adapter = MemberListAdapter(this, members)
        membersRecyclerView.adapter = adapter
    }

    //Displays information about the experience icon (X and tick)
    private fun popExperienceInfoDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.what_is_this)
                .setMessage(R.string.group_exp_help)
                .setPositiveButton(R.string.okay, null)
        builder.show()
    }
}
