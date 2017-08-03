package com.xplore.groups.my

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xplore.R
import com.xplore.database.DBManager
import com.xplore.groups.GroupCard
import com.xplore.groups.GroupCardRecyclerViewAdapter
import com.xplore.user.UserCard

import kotlinx.android.synthetic.main.my_groups.*

/**
 * Created by Nika on 7/14/2017.
 *
 * აღწერა:
 * ეს კლასი აჩვენებს ჯგუფებს, რომელშიც მომხმარებელი გაწევრიანებულია.
 *
 * Description:
 * This class displays the currently joined groups of the user.
 *
 */

class MyGroupsFragment() : Fragment() {

    //Firebase References
    private val firebaseUsersRef = FirebaseDatabase.getInstance().reference.child("users")
    private val firebaseGroupsRef = FirebaseDatabase.getInstance().reference.child("groups")
    //Firebase Tags
    private val FIREBASE_TAG_MEMBER_IDS = "member_ids"

    private var joinedGroups = HashMap<String, Boolean>()
    private var invitedGroups = HashMap<String, Boolean>()
    private val groupCards = ArrayList<GroupCard>()
    private val userCards = ArrayList<UserCard>()

    //For reserve image loading
    private val dbManager: DBManager by lazy { DBManager(activity) }

    companion object {
        @JvmStatic
        fun newInstance(joinedGroupIds: HashMap<String, Boolean>,
                        invitedGroupIds: HashMap<String, Boolean>)
                : MyGroupsFragment {
            val f = MyGroupsFragment()
            val args = Bundle()
            args.putSerializable("joinedGroupIds", joinedGroupIds)
            args.putSerializable("invitedGroupIds", invitedGroupIds)
            f.arguments = args
            return  f
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInst: Bundle?)
            = inflater.inflate(R.layout.my_groups, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        //Getting bundle arguments
        joinedGroups = arguments.getSerializable("joinedGroupIds") as HashMap<String, Boolean>
        invitedGroups = arguments.getSerializable("invitedGroupIds") as HashMap<String, Boolean>

        dbManager.openDataBase()
        myGroupsRecyclerView.layoutManager = LinearLayoutManager(activity)
        myGroupsRecyclerView.adapter = GroupCardRecyclerViewAdapter(groupCards, activity)

        loadGroups(invitedGroups, true)
        loadGroups(joinedGroups, false)
    }

    fun loadGroups(groupMap: HashMap<String, Boolean>, invited: Boolean) {
        for (groupId in groupMap.keys) {
            firebaseGroupsRef.child(groupId).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            if (dataSnapshot != null) {
                                val groupCard = dataSnapshot.getValue(GroupCard::class.java)
                                if (groupCard != null) {
                                    groupCard.id = dataSnapshot.key
                                    val leaderId = dataSnapshot.child(FIREBASE_TAG_MEMBER_IDS)
                                            .child("0").getValue(String::class.java)!!

                                    groupCard.invite = invited
                                    loadLeaderCard(leaderId, groupCard)
                                } else { printError() }
                            } else { printError() }
                        }

                        override fun onCancelled(p0: DatabaseError?) { }
                    }
            )
        }
    }

    //Used to load leader info, assign it to a group AND UPDATE THE RECYCLERVIEW
    fun loadLeaderCard(leaderId: String, groupCard: GroupCard) {
        firebaseUsersRef.child(leaderId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot != null) {
                            val leaderCard = dataSnapshot.getValue(UserCard::class.java)
                            if (leaderCard != null) {
                                leaderCard.id = dataSnapshot.key

                                groupCard.leaderId = leaderCard.id
                                groupCard.leaderName = leaderCard.fname + " " + leaderCard.lname
                                groupCard.leaderReputation = leaderCard.reputation
                                groupCard.leaderImageUrl = leaderCard.profile_picture_url
                                groupCard.reserveImageId =
                                        dbManager.getImageId(groupCard.destination_id.toInt())

                                groupCards.add(groupCard)
                                myGroupsRecyclerView.adapter.notifyDataSetChanged()
                            } else { printError() }
                        } else { printError() }
                    }

                    override fun onCancelled(p0: DatabaseError?) { }
                }
        )
    }

    fun printError() {
        Toast.makeText(activity, "Error loading data", Toast.LENGTH_SHORT).show()
    }
}