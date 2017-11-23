package com.xplore.groups.search

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.xplore.General
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.base.refreshable.RefreshableSearchFragment
import com.xplore.database.DBManager
import com.xplore.groups.GroupCard
import com.xplore.groups.GroupCardRecyclerViewAdapter
import com.xplore.groups.create.CreateGroupActivity
import com.xplore.user.User
import com.xplore.util.FirebaseUtil.F_DESTINATION_ID
import com.xplore.util.FirebaseUtil.F_GROUP_NAME
import com.xplore.util.FirebaseUtil.F_MEMBER_IDS
import com.xplore.util.FirebaseUtil.F_START_DATE
import com.xplore.util.FirebaseUtil.groupsRef
import com.xplore.util.FirebaseUtil.usersRef
import kotlinx.android.synthetic.main.search_layout3.*

/*
 * Created by Nikaoto on 2/8/2017.
 *
 *  მომხმარებელი ამ ფრაგმენტიდან ეძებს დაგეგმილ ლაშქრობებს ან ქმნის ახალს (FAB-ით).
 *
 */

class SearchGroupsFrag : RefreshableSearchFragment() {

    private val TAG = "search-groups-frag"

    companion object {
        private const val HIKE_SHOW_DAY_LIMIT = 100
        private const val FAB_HIDE_SCROLL_DY = 5;

        private const val ARG_DESTINATION_ID = "destId"
        private const val ARG_DESTINATION_ID_DEFAULT_VALUE = -1

        @JvmStatic
        fun newInstance(destId: Int): SearchGroupsFrag {
            val f = SearchGroupsFrag()
            val bundle = Bundle()
            bundle.putInt(ARG_DESTINATION_ID, destId)
            f.arguments = bundle
            return f
        }
    }

    private val groupCards = ArrayList<GroupCard>()
    private val displayCards = ArrayList<GroupCard>()

    // Determines whether the data should reload when user clears search text
    private var canReset = false

    // Used to firstLoadData when searching by reserve (sent from ReserveInfoAct)
    private var shouldClearSearchOnRefresh = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
            : View = inflater.inflate(R.layout.search_layout3, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TimeManager.refreshGlobalTimeStamp()

        createGroupFAB.setOnClickListener {
            startActivity(CreateGroupActivity.getStartIntent(activity))
        }

        initRefreshLayout(refreshLayout, true)

        resultsRV.layoutManager = LinearLayoutManager(activity)
        resultsRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > FAB_HIDE_SCROLL_DY) {
                    createGroupFAB.hide()
                } else {
                    createGroupFAB.show()
                }
            }
        })

        firstLoadData()
    }

    override fun setUpSearchView(newSearchView: SearchView?) {
        if (activity != null) {
            newSearchView?.queryHint = resources.getString(R.string.search_groups_hint)
        }
    }

    private fun firstLoadData() {
        setLoading(true)
        canReset = true;

        if (!General.isNetConnected(activity)) {
            return General.createNetErrorDialog(activity)
        }

        if (activity != null) {
            prepareToLoadData()
            loadData()
        }
    }

    private fun prepareToLoadData() {
        groupCards.clear()
        displayCards.clear()
        // Displays list
        resultsRV.adapter = GroupCardRecyclerViewAdapter(displayCards, activity)
    }

    private fun shouldSearchByDestination(): Boolean {
        val destId = arguments?.getInt(ARG_DESTINATION_ID, ARG_DESTINATION_ID_DEFAULT_VALUE)
        return destId != null && destId != ARG_DESTINATION_ID_DEFAULT_VALUE
                && !shouldClearSearchOnRefresh
    }

    private fun loadData() {
        if (shouldSearchByDestination()) {
            // TODO very hacky, fix searching by destination, but for now, leave it for readability

            val destId = arguments!!.getInt(ARG_DESTINATION_ID)
            searchByDestination(destId)
        } else {
            // Just load all hikes

            //TODO change this after adding sort by options
            groupsRef.orderByChild(F_START_DATE).limitToFirst(100)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            if (dataSnapshot != null) {
                                loadDataFromSnapshot(dataSnapshot)
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {}
                    })
        }
    }

    private fun searchByDestination(destId: Int) {
        shouldClearSearchOnRefresh = true
        canReset = true

        groupsRef.orderByChild(F_DESTINATION_ID).equalTo(destId.toDouble()).limitToFirst(100)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot != null) {
                            loadDataFromSnapshot(dataSnapshot)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {}
                })
    }

    // Loads group info from passed snapshot
    private fun loadDataFromSnapshot(dataSnapshot: DataSnapshot) {
        dataSnapshot.children.forEach {
            val tempCard = it.getValue(GroupCard::class.java)

            tempCard?.id = it.key
            tempCard?.memberCount = getMemberCount(it)

            //TODO change when multiple leaders are added
            it.child(F_MEMBER_IDS).children.forEach {
                if (it.getValue(Boolean::class.java)!!) {
                    tempCard?.leaderId = it.key
                }
            }

            groupCards.add(tempCard!!)
        }
        if (activity != null) {
            sortLeaderInfo()
        }
    }

    // Gets member count of a group
    private fun getMemberCount(groupSnapshot: DataSnapshot) = groupSnapshot.child(F_MEMBER_IDS).childrenCount.toInt()

    /* Goes over every user in firebase to check which of them are leaders (to get leader image)
       and filters groups from groupCards into displayCards */
    private fun sortLeaderInfo() {
        val dbManager = DBManager(activity)
        dbManager.openDataBase()

        usersRef.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot == null) return nothingFound()

                dataSnapshot.children.forEach { userSnapshot ->
                    groupCards.forEach { groupCard ->
                        if (groupCard.leaderId == userSnapshot.key) {
                            val leader = userSnapshot.getValue(User::class.java)

                            leader?.let {
                                groupCard.leaderName = it.getFullName()
                                groupCard.leaderReputation = it.reputation
                                groupCard.leaderImageUrl = it.profile_picture_url

                                displayCards.add(groupCard)
                                resultsRV.adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }

                if (activity != null) {
                    onFinishedLoading()
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    override fun onSearch(query: String) {
        super.onSearch(query)

        createGroupFAB.show()
        canReset = true
        prepareToLoadData()

        // Search by group name
        groupsRef.orderByChild(F_GROUP_NAME).startAt(query).endAt(query+"\uf8ff").limitToFirst(100)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot == null) return nothingFound()

                        dataSnapshot.children.forEach {
                            // Get group info
                            val groupCard = it.getValue(GroupCard::class.java)
                            if (groupCard != null) {
                                groupCard.id = it.key
                                groupCard.memberCount = getMemberCount(it)

                                // Get Leader Id
                                it.child(F_MEMBER_IDS).children.forEach {
                                    val isLeader = it.getValue(Boolean::class.java)
                                    if (isLeader != null && isLeader) {
                                        groupCard.leaderId = it.key
                                    }
                                }

                                // Add groupCard to list
                                groupCards.add(groupCard)
                            }
                        }

                        if (activity != null) {
                            sortLeaderInfo()
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {}
                })
    }

    override fun onResetSearch() {
        super.onResetSearch()

        if (canReset) {
            firstLoadData()
        }
    }

    private fun nothingFound() = Toast.makeText(activity, R.string.search_no_results, Toast.LENGTH_SHORT).show()

    private fun onFinishedLoading() {
        setLoading(false)
        if (displayCards.isEmpty()) {
            nothingFound()
        }
    }

    override fun onRefreshed() {
        super.onRefreshed()

        // Reload with or without query

        if (!isCurrentQueryEmpty()) {
            onSearch(currentQuery)
        } else {
            firstLoadData()
        }
    }
}

