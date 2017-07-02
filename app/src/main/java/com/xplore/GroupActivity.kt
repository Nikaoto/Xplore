package com.xplore

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

import java.util.ArrayList

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.group_info.*
import kotlinx.android.synthetic.main.reserve_list_item.*

/**
 * Created by nikao on 2/12/2017.
 */

class GroupActivity : Activity() {

    private var group_id: String = ""
    private var reserveID: Int = 0
    private var memberCount: Int = 0

    private val members = ArrayList<User>()
    internal val DBref = FirebaseDatabase.getInstance().reference
    internal val groupsDBref = DBref.child("groups").ref //TODO remove .ref
    internal val usersDBref = DBref.child("users").ref
    internal var tempGroup = Group();
    internal var tempMember = User();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.group_info)
        //buildUserBase();

        resetVariables()
        receiveData()
        initMemberList()

        //reset the layout TODO remove after finishing testing and clear layouts
        ResetLayout()

        //Loading the info
        loadGroupData(group_id)
        applyReserveData()
    }

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

    private fun ResetLayout() {
        //leader
        leaderFnameTextView.text = "-"
        leaderLnameTextView.text = "-"
        leaderAgeTextView.text = ""
        leaderTelTextView.text = ""
        leaderRepTextView.text = ""
        //member
        selectedMemberFnameTextView.text = "-"
        selectedMemberLnameTextView.text = "-"
        selectedMemberAgeTextView.text = ""
        selectedMemberTelTextView.text = ""
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
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //creating the temporary (current) group
                tempGroup = Group()
                tempGroup = dataSnapshot.getValue(Group::class.java)
                tempGroup.setGroup_id(dataSnapshot.key)

                memberCount = tempGroup.getMember_ids().size.toInt()

                for (memberId in tempGroup.getMember_ids()) { //TODO? this returns members in random order
                    getUserInfo(memberId.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    //Gets user info from Firebase using userId
    private fun getUserInfo(userId: String) {
        val query = usersDBref.child(userId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tempMember = dataSnapshot.getValue(User::class.java) //Getting member info
                members.add(tempMember) //Setting member info
                memberCount-- //Iterating member index
                if (memberCount == 0) { //Checking if member list retrieval finished
                    ApplyGroupData()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun applyReserveData() {
        val context = this@GroupActivity //TODO insert this into constructor
        val dbManager = DBManager(context)
        dbManager.openDataBase()
        val tempReserveCard = dbManager.getReserveCard(reserveID)


        reserveCardView.setOnClickListener { General.openReserveInfoFragment(reserveID, context) }
        reserveNameTextView.text = tempReserveCard.name
        reserveImageView.setImageResource(tempReserveCard.imageId)
        reserveIconImageView.setImageResource(ReserveIcons.grey[tempReserveCard.iconId])
    }

    private fun ApplyGroupData() {
        groupProgressBar.visibility = View.GONE
        Picasso.with(this)
                .load(members[0].getProfile_picture_url())
                .transform(RoundedCornersTransformation(
                        resources.getInteger(R.integer.pic_big_angle),
                        resources.getInteger(R.integer.pic_big_margin)))
                .into(leaderImageView)
        leaderFnameTextView.text = members[0].getFname()
        leaderLnameTextView.text = members[0].getLname()
        val age = General.calculateAge(TimeManager.globalTimeStamp, members[0].getBirth_date())
        leaderAgeTextView.text = "${getString(R.string.age)}: $age"
        leaderTelTextView.text = "${getString(R.string.tel)}: ${members[0].getTel_num()}"
        leaderRepTextView.text = members[0].getReputation().toString()

        //Setting experienced icon
        if (tempGroup.isExperienced) {
            groupExpImageView.setImageResource(R.drawable.ic_check)
        } else {
            groupExpImageView.setImageResource(R.drawable.ic_x)
        }

        groupExpImageView.setOnClickListener {
            popExperienceInfoDialog()
        }

        startDateTextView.text = BufferDate(tempGroup.getStart_date())
        endDateTextView.text = BufferDate(tempGroup.getEnd_date())

        groupPrefsTextView.text = tempGroup.getGroup_preferences()
        groupExtraInfoTextView.text = tempGroup.getExtra_info()

        populateMemberImageList()
    }

    //Displays information about the experience icon (X and tick)
    private fun popExperienceInfoDialog() {
        val builder = AlertDialog.Builder(this@GroupActivity)
        builder.setTitle(R.string.what_is_this)
                .setMessage(R.string.group_exp_help)
                .setPositiveButton(R.string.okay, null)
        builder.show()
    }

    private fun populateMemberImageList() {
        val adapter = MemberListAdapter(this@GroupActivity, members, selectedMemberProfileLayout)
        membersRecyclerView.adapter = adapter
    }

    //adds slashes to a date given in int (yyyy.mm.dd) without dots
    private fun BufferDate(date: Long): String {
        val sd = StringBuffer(date.toString())
        sd.insert(4, "/")
        sd.insert(7, "/")
        return sd.toString()
    }
}
