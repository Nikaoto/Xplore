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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.xplore.R

import kotlinx.android.synthetic.main.manage_requests.*

/**
 * Created by Nika on 8/8/2017.
 * TODO write description of this class - what it does and why.
 */
class ManageRequestsActivity : AppCompatActivity() {

    //Firebase
    lateinit private var invitedMembersRef: DatabaseReference

    lateinit private var groupId: String

    private val invitedMemberIds = ArrayList<String>()
    //A list of ids of members who want to join the group
    private val joinMemberIds = ArrayList<String>()

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
        invitedMembersRef = FirebaseDatabase.getInstance()
                .getReference("groups/$groupId/invited_member_ids")


        toolbar.setNavigationOnClickListener { finish() }

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
            if (position == 0) {
                return ReceivedRequestsFragment.newInstance(groupId, joinMemberIds)
            } /*else if (position == 0) {
                return
            }*/
            return Fragment()
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