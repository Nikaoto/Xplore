package com.xplore.groups.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xplore.*
import com.xplore.R
import com.xplore.database.DBManager
import com.xplore.groups.Group
import com.xplore.groups.view.controls.InvitedControls
import com.xplore.groups.view.controls.LeaderControls
import com.xplore.groups.view.controls.MemberControls
import com.xplore.groups.view.controls.OutsiderControls
import com.xplore.maps.MapActivity
import com.xplore.reserve.Icons
import com.xplore.reserve.ReserveInfoActivity
import com.xplore.user.User

import java.util.ArrayList

import kotlinx.android.synthetic.main.group_info2.*
import kotlinx.android.synthetic.main.reserve_list_item.*
import kotlinx.android.synthetic.main.leader_profile.*

/**
* Created by Nikaoto on 2/12/2017.
* TODO write description of this class - what it does and why.
*/

class GroupInfoActivity : Activity() {

    //Firebase
    private val FIREBASE_TAG_MEMBER_IDS = "member_ids"
    private val FIREBASE_TAG_GROUP_IDS = "group_ids"
    private val DBref = FirebaseDatabase.getInstance().reference
    private val firebaseGroupsRef = DBref.child("groups")
    private val firebaseUsersRef = DBref.child("users")

    private var groupId = ""
    private var memberCount = 1
    private val members = ArrayList<User>()
    private lateinit var leader: User

    private var allowRefresh = false

    //The variables which contain the current group/member info
    private var currentGroup = Group()

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String)
                = Intent(context, GroupInfoActivity::class.java).putExtra("groupId", groupId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_info2)
        //buildUserBase();

        toolbar.setNavigationOnClickListener {
            finish()
        }

        //Receives group data from last intent
        val intent = this.intent
        groupId = intent.getStringExtra("groupId")

        initMemberList()
        loadGroupData(groupId)
    }

    override fun onResume() {
        super.onResume()
        if (allowRefresh) {
            allowRefresh = false
            refresh()
        } else {
            allowRefresh = true
        }
    }

    private fun refresh() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun configureControls(group: Group) {
        if (group.leaderId == General.currentUserId) {
            configureLeaderControls()
        } else if(group.getMember_ids().contains(General.currentUserId)) {
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

    private fun configureMemberControls() {
        fragmentManager.beginTransaction()
                .replace(R.id.controls_container, MemberControls.newInstance(groupId)).commit()
    }

    private fun configureInvitedControls() {
        fragmentManager.beginTransaction()
                .replace(R.id.controls_container, InvitedControls.newInstance(groupId)).commit()
    }

    private fun configureOutsiderControls(awaitingRequest: Boolean = false) {
        fragmentManager.beginTransaction()
                .replace(R.id.controls_container,
                        OutsiderControls.newInstance(groupId, awaitingRequest)).commit()
    }

    private fun configureLeaderControls() {
        fragmentManager.beginTransaction()
                .replace(R.id.controls_container, LeaderControls.newInstance(groupId)).commit()
    }

    //Gets destination data (reserve id or custom location latlng) and displays it as the image
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
                startActivity(MapActivity.getStartIntent(this, true, currentGroup.name,
                        currentGroup.destination_latitude, currentGroup.destination_longitude))
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

    private fun loadGroupData(groupId: String) {
        firebaseGroupsRef.child(groupId).addListenerForSingleValueEvent(object : ValueEventListener {
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
                    //TODO string resources
                    Toast.makeText(applicationContext,
                            "The group does not exist",
                            Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getLeaderInfo(id: String) {
        firebaseUsersRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
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
        firebaseUsersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                //Checking if member exists
                if (dataSnapshot != null) {
                    val tempMember = dataSnapshot.getValue(User::class.java)
                    //Getting member info
                    tempMember?.let {
                        tempMember.setId(userId) //Setting user Id
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
        //TODO string resources
        Toast.makeText(applicationContext,
                "There was an error retreiving the members",
                Toast.LENGTH_SHORT).show()
        finish()
    }

    //Displays the already-retrieved data of the group
    private fun applyGroupData() {

        //Displaying leader
        //Profile picture
        Picasso.with(this).invalidate(leader.getProfile_picture_url())
        Picasso.with(this)
                .load(leader.getProfile_picture_url())
                .transform(ImageUtil.mediumCircle(this))
                .into(leaderImageView)
        leaderImageView.setOnClickListener {
            General.openUserProfile(this, leader.id)
        }

        //Name
        leaderNameTextView.text = "${leader.fname} ${leader.lname}"

        //Age
        val age = General.calculateAge(TimeManager.globalTimeStamp, leader.getBirth_date())
        leaderAgeTextView.text = "${getString(R.string.age)}: $age"

        //Telephone
        leaderTelTextView.text = "${getString(R.string.tel)}: ${leader.getTel_num()}"

        //Reputation
        leaderRepCombinedTextView.text = "${leader.reputation} ${resources.getString(R.string.reputation)}"

        //Setting experienced icon
        //TODO experienced icon
/*        if (currentGroup.isExperienced) {
            groupExpImageView.setImageResource(R.drawable.ic_check)
        } else {
            groupExpImageView.setImageResource(R.drawable.ic_x)
        }

        groupExpImageView.setOnClickListener {
            popExperienceInfoDialog()
        }*/

        //Dates
        dateCombinedTextView.text = General.putSlashesInDate(currentGroup.getStart_date()) + " - " +
                General.putSlashesInDate(currentGroup.getEnd_date())
        meetupTimeTextView.text = General.putColonInTime(currentGroup.getStart_time())

        descriptionTextView.text = currentGroup.extra_info
        preferencesTextView.text = currentGroup.group_preferences

        //Displaying members
        if (members.isEmpty()) {
            memberListCardView.visibility = View.GONE
        } else {
            val adapter = MemberListAdapter(this, members)
            membersRecyclerView.adapter = adapter
        }
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
