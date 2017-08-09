package com.xplore.groups.requests

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.database.*
import com.xplore.R
import com.xplore.empty.EmptyFragmentFactory
import com.xplore.groups.AllMemberIdsForGroup

import kotlinx.android.synthetic.main.manage_requests.*

/**
 * Created by Nika on 8/8/2017.
 * TODO write description of this class - what it does and why.
 */
class ManageRequestsActivity : AppCompatActivity() {

    //Firebase
    lateinit private var currentGroupRef: DatabaseReference

    lateinit private var groupId: String

    private val invitedMemberIds = ArrayList<String>()
    //A list of ids of members who want to join the group
    private val joinRequestMemberIds = ArrayList<String>()

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String)
            = Intent(context, ManageRequestsActivity::class.java).putExtra("groupId", groupId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_requests)

        setSupportActionBar(toolbar)

        groupId = intent.getStringExtra("groupId")
        currentGroupRef = FirebaseDatabase.getInstance()
                .getReference("groups/$groupId")

        currentGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val allMemberIds = dataSnapshot.getValue(AllMemberIdsForGroup::class.java)
                    if (allMemberIds != null) {
                        for (memberId in allMemberIds.invited_member_ids) {
                            if (memberId.value) {
                                invitedMemberIds.add(memberId.key)
                            }
                            else {
                                joinRequestMemberIds.add(memberId.key)
                            }
                        }
                        displayTabs()
                    } else { displayTabs() }
                } else { displayTabs() }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })

        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun displayTabs() {
        page_container.adapter = RequestsPagerAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(page_container)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
        //TODO menu with refresh button
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
        //TODO refresh button
    }

    private inner class RequestsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount() = 2

        override fun getItem(position: Int): Fragment {
            if (position == 0 && joinRequestMemberIds.isNotEmpty()) {
                return ReceivedRequestsFragment.newInstance(groupId, joinRequestMemberIds)
            } else if (position == 1 && invitedMemberIds.isNotEmpty()) {
                //TODO setRequests
                //return SentRequestsFragment.newInstance(groupId, invitedMemberIds)
            }
            return EmptyFragmentFactory().getSupportFragment()
        }

        override fun getPageTitle(position: Int): CharSequence {
            //TODO string resources
            if (position == 0) {
                return "Received Requests"
            } else if (position == 1) {
                return "Sent Requests"
            }
            return ""
        }
    }
}