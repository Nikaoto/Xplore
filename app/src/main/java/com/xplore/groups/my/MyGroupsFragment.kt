package com.xplore.groups.my

import android.app.FragmentTransaction
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
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
import com.xplore.TimeManager
import com.xplore.base.refreshable.RefreshableFragment
import com.xplore.database.DBManager
import com.xplore.groups.GroupCard
import com.xplore.groups.GroupCardRecyclerViewAdapter
import com.xplore.user.UserCard
import kotlinx.android.synthetic.main.loading_layout.*
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

class MyGroupsFragment() : RefreshableFragment() {

    //Firebase References
    private val firebaseUsersRef = FirebaseDatabase.getInstance().reference.child("users")
    private val firebaseGroupsRef = FirebaseDatabase.getInstance().reference.child("groups")
    //Firebase Tags
    private val FIREBASE_TAG_MEMBER_IDS = "member_ids"

    private var joinedGroups = HashMap<String, Boolean>()
    private var invitedGroups = HashMap<String, Boolean>()
    private val groupCards = ArrayList<GroupCard>()
    private val userCards = ArrayList<UserCard>()

    private var allowRefresh = false

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        TimeManager.refreshGlobalTimeStamp()

        initRefreshLayout(view.findViewById<SwipeRefreshLayout>(R.id.refreshLayout))
        setLoading(true)

        //Getting bundle arguments
        joinedGroups = arguments.getSerializable("joinedGroupIds") as HashMap<String, Boolean>
        invitedGroups = arguments.getSerializable("invitedGroupIds") as HashMap<String, Boolean>

        dbManager.openDataBase()
        myGroupsRecyclerView.layoutManager = LinearLayoutManager(activity)
        myGroupsRecyclerView.adapter = GroupCardRecyclerViewAdapter(groupCards, activity)

        loadGroups(invitedGroups, true)
        loadGroups(joinedGroups, false)
    }

    private fun loadGroups(groupMap: HashMap<String, Boolean>, invited: Boolean) {
        for (groupId in groupMap.keys) {
            firebaseGroupsRef.child(groupId).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            if (dataSnapshot != null) {
                                val groupCard = dataSnapshot.getValue(GroupCard::class.java)
                                if (groupCard != null) {
                                    groupCard.id = dataSnapshot.key
                                    groupCard.memberCount = getMemberCount(dataSnapshot)
                                    groupCard.invite = invited
                                    val leaderId = getLeaderId(dataSnapshot)
                                    if (leaderId != null) {
                                        loadLeaderCard(leaderId, groupCard)
                                    } else { printError() }
                                } else { printError() }
                            } else { printError() }
                        }

                        override fun onCancelled(p0: DatabaseError?) { }
                    }
            )
        }
    }

    private fun getLeaderId(groupSnapshot: DataSnapshot): String? {
        for (snapshot in groupSnapshot.child(FIREBASE_TAG_MEMBER_IDS).children) {
            if (snapshot.getValue(Boolean::class.java)!!) {
                return snapshot.key
            }
        }
        return null
    }

    private fun getMemberCount(groupSnapshot: DataSnapshot)
            = groupSnapshot.child(FIREBASE_TAG_MEMBER_IDS).childrenCount.toInt()

    //Used to load leader info, assign it to a group AND UPDATE THE RECYCLERVIEW
    private fun loadLeaderCard(leaderId: String, groupCard: GroupCard) {
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

                                groupCards.add(groupCard)

                                onFinishedLoading()

                                if (loadingbar != null) { //TODO throws NPE
                                    loadingbar.visibility = View.INVISIBLE
                                }
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

    override fun onResume() {
        super.onResume()

        //Checking if refresh needed
        if (allowRefresh) {
            allowRefresh = false
            fragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.fragment_container, LoadingMyGroupsFragment()).commit()
        } else {
            allowRefresh = true
        }
    }

    // Called when all layout and data loading is finished
    private fun onFinishedLoading() {
        setLoading(false)
    }

    override fun onRefreshed() {
        super.onRefreshed()
        allowRefresh = false
        fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, LoadingMyGroupsFragment()).commit()
    }
}