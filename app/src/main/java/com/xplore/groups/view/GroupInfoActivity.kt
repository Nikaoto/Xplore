package com.xplore.groups.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.MemberListAdapter
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.base.refreshable.RefreshableActivity
import com.xplore.database.DBManager
import com.xplore.groups.Group
import com.xplore.groups.view.controls.InvitedControls
import com.xplore.groups.view.controls.LeaderControls
import com.xplore.groups.view.controls.MemberControls
import com.xplore.groups.view.controls.OutsiderControls
import com.xplore.maps.GroupMapActivity
import com.xplore.maps.UserMarker
import com.xplore.maps.live_hike.LiveHikeMapActivity
import com.xplore.reserve.Icons
import com.xplore.reserve.ReserveInfoActivity
import com.xplore.user.User
import com.xplore.util.DateUtil
import com.xplore.util.FirebaseUtil.F_FNAME
import com.xplore.util.FirebaseUtil.F_LOCATIONS
import com.xplore.util.FirebaseUtil.groupsRef
import com.xplore.util.FirebaseUtil.usersRef
import com.xplore.util.ImageUtil
import com.xplore.util.MapUtil
import kotlinx.android.synthetic.main.group_info.*
import kotlinx.android.synthetic.main.leader_profile.*
import kotlinx.android.synthetic.main.reserve_card.*
import java.util.*

/**
* Created by Nikaoto on 2/12/2017.
*
* ჯგუფის ინფოს ნახულობს იუზერი აქ. Member-, Outsider- და InvitedControls ფრაგმენტებს ტვირთავს
* ლეიაუთის თავში ინტერაქციისთვის (კონტროლები და მოქმედებები).
*
* User views group information here. The Member-, Outsider-, and InvitedControls fragments
* are loaded at the top of the layout to allow interaction (and controls/actions).
*
*/

class GroupInfoActivity : RefreshableActivity() {

    companion object {
        private const val ARG_GROUP_ID = "groupId"

        @JvmStatic
        fun newIntent(context: Context, groupId: String): Intent
                = Intent(context, GroupInfoActivity::class.java).putExtra("groupId", groupId)
    }

    //Firebase
    private val currentGroupRef: DatabaseReference by lazy {
        groupsRef.child(groupId)
    }

    private val groupId: String by lazy {
        intent.getStringExtra(ARG_GROUP_ID)
    }
    private var memberCount = 1 // Can't have less than 1 (the leader)
    private val members = ArrayList<User>()
    private lateinit var leader: User

    private var allowRefresh = false

    //The variables which contain the current group/member info
    private var currentGroup = Group()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_info)
        supportActionBar?.hide()

        initRefreshLayout(findViewById<SwipeRefreshLayout>(R.id.refreshLayout))
        setLoading(true)

        //buildUserBase();

        TimeManager.refreshGlobalTimeStamp()

        toolbar.setNavigationOnClickListener {
            finish()
        }

        initMemberList()
        loadGroupData()
    }

    override fun onResume() {
        super.onResume()
        if (allowRefresh) {
            allowRefresh = false
            onRefreshed()
        } else {
            allowRefresh = true
        }
    }

    override fun onRefreshed() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun configureControls(group: Group) {
        if (this@GroupInfoActivity != null) {
            //Conf depending on viewer type
            if (group.leaderId == General.currentUserId) {
                configureLeaderControls()
            } else if (group.getMember_ids().contains(General.currentUserId)) {
                configureMemberControls()
            } else if (group.getInvited_member_ids() != null
                    && group.getInvited_member_ids().get(General.currentUserId) != null) {
                if (group.getInvited_member_ids().get(General.currentUserId) == true) {
                    //Invited to group
                    configureInvitedControls()
                } else if (group.getInvited_member_ids().get(General.currentUserId) == false) {
                    //Sent join request to group
                    configureOutsiderControls(true)
                }
            } else {
                configureOutsiderControls()
            }
        }
    }

    // Shows card with meetup time and place
    // (not inside applyGroupData because this is only shown to members)
    private fun configureMeetupCard() {
        if (currentGroup.hasMeetupLocation()) {
            meetupCard.visibility = View.VISIBLE
            meetupLocationImageView.visibility = View.VISIBLE

            // Loading image
            Picasso.with(this)
                    .load(MapUtil.getMeetupMapUrl(currentGroup.meetup_latitude,
                            currentGroup.meetup_longitude))
                    .into(meetupLocationImageView)

            // Onclick
            meetupLocationImageView.setOnClickListener {
                startActivity(GroupMapActivity.newIntent(this, true,
                        getString(R.string.meetup_location), currentGroup.meetup_latitude,
                        currentGroup.meetup_longitude, MapUtil.MEETUP_MARKER_HUE))
            }
        }
    }

    //Shows map button if now hiking TODO change name to live hike
    private fun configureShowOnMapButton() {
        if (TimeManager.intTimeStamp >= currentGroup.start_date
                && TimeManager.intTimeStamp <= currentGroup.end_date) {

            openLiveHikeButton.visibility = View.VISIBLE
            openLiveHikeButton.setOnClickListener { startLiveHike() }
        }
    }

    private fun configureMemberControls() {
        configureMeetupCard()
        configureShowOnMapButton()
        fragmentManager.beginTransaction()
                .replace(R.id.controls_container, MemberControls.newInstance(groupId)).commit()
    }

    private fun configureInvitedControls() {
        fragmentManager.beginTransaction()
                .replace(R.id.controls_container, InvitedControls.newInstance(groupId)).commit()
    }

    private fun configureOutsiderControls(awaitingRequest: Boolean = false) {
        if (!currentGroup.hasMeetupTime()) {
            meetupCard.visibility = View.VISIBLE
        }
        fragmentManager.beginTransaction()
                .replace(R.id.controls_container,
                        OutsiderControls.newInstance(groupId, awaitingRequest)).commit()
    }

    private fun configureLeaderControls() {
        configureMeetupCard()
        //configureFinishCard()
        configureShowOnMapButton()
        fragmentManager.beginTransaction()
                .replace(R.id.controls_container, LeaderControls.newInstance(groupId)).commit()
    }

    // Gets destination data (reserve id or custom location latlng) and displays it as the image
    private fun applyDestinationData() {
        if (currentGroup.destination_id != Group.DESTINATION_DEFAULT) {
            val dbManager = DBManager(this)
            dbManager.openDataBase()
            val tempReserveCard = dbManager.getReserveCard(currentGroup.destination_id)
            dbManager.close()

            reserveCardView.setOnClickListener {
                startActivity(ReserveInfoActivity.getStartIntent(this, currentGroup.destination_id))
            }
            reserveNameTextView.text = tempReserveCard.name
            reserveImageView.setImageResource(tempReserveCard.imageId)
            //groupImageView.setImageResource(tempReserveCard.imageId)
            reserveIconImageView.setImageResource(Icons.grey[tempReserveCard.iconId])
        } else {
            //Destination name
            reserveNameTextView.text = currentGroup.name

            //Destination image
            Picasso.with(this).load(currentGroup.group_image_url).into(reserveImageView)
            reserveCardView.setOnClickListener {
                startActivity(GroupMapActivity.newIntent(this, true,
                        getString(R.string.destination), currentGroup.destination_latitude,
                        currentGroup.destination_longitude))
            }

            //Remove reserve type
            reserveIconImageView.visibility = View.INVISIBLE
        }
    }

    private fun initMemberList() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        membersRecyclerView.setHasFixedSize(true)
        membersRecyclerView.layoutManager = layoutManager
    }

    private fun loadGroupData() {
        currentGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                //Checking if group exists
                if (dataSnapshot != null) {
                    //creating the temporary (current) group
                    dataSnapshot.getValue(Group::class.java)?.let {
                        currentGroup = it
                        groupNameTextView.text = currentGroup.name
                        currentGroup.setGroup_id(dataSnapshot.key)
                        memberCount = currentGroup.getMember_ids().size

                        applyDestinationData()

                        configureControls(currentGroup)

                        for (memberId in currentGroup.getMember_ids().keys) {
                            if (memberId != null) {
                                if (memberId == currentGroup.leaderId) {
                                    getLeaderInfo(memberId)
                                } else {
                                    getUserInfo(memberId)
                                }
                            } else {
                                decrementMemberCount()
                            }
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, R.string.error, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getLeaderInfo(id: String) {
        usersRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    leader = dataSnapshot.getValue(User::class.java)!!
                    leader.id = dataSnapshot.key
                    decrementMemberCount()
                } else {
                    notFound()
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    //Gets user info from Firebase using userId
    private fun getUserInfo(userId: String) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                //Checking if member exists
                if (dataSnapshot != null) {
                    val tempMember = dataSnapshot.getValue(User::class.java)
                    //Getting member info
                    tempMember?.let {
                        tempMember.id = userId //Setting user Id
                        members.add(tempMember) //Setting member info
                    }
                    decrementMemberCount()
                } else {
                    notFound()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun decrementMemberCount() {
        memberCount--
        if (memberCount == 0) {
            applyGroupData()
        }
    }

    private fun notFound() {
        Toast.makeText(applicationContext, R.string.error, Toast.LENGTH_SHORT).show()
        finish()
    }

    // Displays the already-retrieved data of the group
    private fun applyGroupData() {
        // Displaying leader
        // Profile picture
        Picasso.with(this).invalidate(leader.profile_picture_url)
        Picasso.with(this)
                .load(leader.profile_picture_url)
                .transform(ImageUtil.mediumCircle(this))
                .into(leaderImageView)
        leaderImageView.setOnClickListener {
            General.openUserProfile(this, leader.id)
        }

        //Name
        leaderNameTextView.text = leader.getFullName()

        //Age
        val age = General.calculateAge(TimeManager.globalTimeStamp, leader.birth_date)
        leaderAgeTextView.text = "${getString(R.string.age)}: $age"

        //Telephone
        leaderTelTextView.text = "${getString(R.string.tel)}: ${leader.tel_num}"

        //Reputation
        leaderRepCombinedTextView.text = "${leader.reputation} ${resources.getString(R.string.reputation)}"
        //

        //Experienced
        if (currentGroup.experienced) {
            beenHereMark.visibility = View.VISIBLE
            beenHereMark.setOnClickListener {
                popExperienceInfoDialog()
            }
        }

        //Dates
        dateCombinedTextView.text = DateUtil.putSlashesInDate(currentGroup.getStart_date()) + " - " +
                DateUtil.putSlashesInDate(currentGroup.getEnd_date())

        //Meetup Time
        if (currentGroup.getStart_time().isEmpty()) {
            meetupTimeTextView.visibility = View.GONE
        } else {
            meetupTimeTextView.text = General.putColonInTime(currentGroup.getStart_time())
        }

        //if (currentGroup.getEnd_time().isEmpty()) { }

        descriptionTextView.text = currentGroup.extra_info
        preferencesTextView.text = currentGroup.group_preferences

        //Displaying members
        if (members.isEmpty()) {
            memberListCardView.visibility = View.GONE
        } else {
            val adapter = MemberListAdapter(this, members)
            membersRecyclerView.adapter = adapter
        }

        onFinishedLoading()
    }

    // Called when all layout and data loading is finished
    private fun onFinishedLoading() {
        setLoading(false)
    }

    private var counter = 0

    private fun startLiveHike() {
        counter = currentGroup.member_ids.size
        currentGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    // Create 'locations' node in firebase if it doesn't exist
                    if (!dataSnapshot.hasChild(F_LOCATIONS)) {
                        val locations = HashMap<String, UserMarker>(currentGroup.member_ids.size)
                        currentGroup.member_ids.forEach {
                            putUserMarker(locations, it.key)
                        }
                    }

                    startActivity(
                            LiveHikeMapActivity.newIntent(this@GroupInfoActivity,
                                    groupId,
                                    getString(R.string.destination),
                                    currentGroup.destination_latitude,
                                    currentGroup.destination_longitude)
                    )
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    /* Puts a UserMarker location object into the given hashmap with given uId
    Used to create a HashMap of member locations to upload to firebase before startLiveHike() */
    private fun putUserMarker(locations: HashMap<String, UserMarker>, uId: String) {
        usersRef.child(uId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    val fname = dataSnapshot.child(F_FNAME).getValue(String::class.java)
                    if (fname != null) {
                        locations.put(uId, UserMarker(fname))
                    } else {
                        locations.put(uId, UserMarker())
                    }
                    counter--
                    if (counter == 0) {
                        currentGroupRef.child(F_LOCATIONS).setValue(locations)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    // Displays information about the experience icon (X and tick)
    private fun popExperienceInfoDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.what_is_this)
                .setMessage(R.string.group_exp_help)
                .setPositiveButton(R.string.okay, null)
        builder.show()
    }
}
