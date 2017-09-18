package com.xplore.groups.requests

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.database.*
import com.xplore.R
import com.xplore.base.BaseAppCompatActivity
import com.xplore.empty.EmptyFragmentFactory
import com.xplore.groups.AllMemberIdsForGroup
import com.xplore.util.FirebaseUtil
import kotlinx.android.synthetic.main.manage_requests.*

/**
 * Created by Nikaoto on 8/8/2017.
 *
 * აღწერა:
 *
 *
 * Description:
 *
 *
 */

class ManageRequestsActivity : BaseAppCompatActivity() {

    companion object {
        private const val ARG_GROUP_ID = "groupId"
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String)
                = Intent(context, ManageRequestsActivity::class.java).putExtra("groupId", groupId)
    }

    private val currentGroupRef: DatabaseReference by lazy {
        FirebaseUtil.getGroupRef(intent.getStringExtra(ARG_GROUP_ID))
    }

    private val groupId: String by lazy {
        intent.getStringExtra(ARG_GROUP_ID)
    }
    private val invitedMemberIds = ArrayList<String>()

    //A list of ids of members who want to join the group
    private val joinRequestMemberIds = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_requests)
        title = getString(R.string.manage_requests)

        setSupportActionBar(toolbar)

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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.refresh_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_refresh) {
            refresh()
        } else {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refresh() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    private inner class RequestsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount() = 2

        override fun getItem(position: Int): Fragment {
            if (position == 0 && joinRequestMemberIds.isNotEmpty()) {
                return ReceivedRequestsFragment.newInstance(groupId, joinRequestMemberIds)
            } else if (position == 1 && invitedMemberIds.isNotEmpty()) {
                return SentRequestsFragment.newInstance(groupId, invitedMemberIds)
            }
            return EmptyFragmentFactory().getSupportFragment()
        }

        override fun getPageTitle(position: Int): CharSequence {
            if (position == 0) {
                return resources.getString(R.string.received_requests)
            } else if (position == 1) {
                return resources.getString(R.string.sent_requests)
            }
            return ""
        }
    }
}