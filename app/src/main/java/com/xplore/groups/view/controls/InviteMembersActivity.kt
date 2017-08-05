package com.xplore.groups.view.controls

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.database.*
import com.xplore.R
import com.xplore.groups.SelectUsersAdapter
import com.xplore.user.UserCard
import kotlinx.android.synthetic.main.search_layout.*

/**
 * Created by Nika on 8/5/2017.
 *
 * აღწერა:
 * იხსნება როდესაც ჯგუფის წევრებს პატიჟებს მომხმარებელი.
 *
 * Desciption:
 * Opens when inviting members to a group.
 *
 */
class InviteMembersActivity : AppCompatActivity(), TextView.OnEditorActionListener {

    //TODO get friends list
    //TODO display friends and add suggestions

    //Firebase
    private val F_GROUPS_TAG = "groups"
    private lateinit var currentGroupRef: DatabaseReference

    private lateinit var groupId: String
    private var displayUserCards = ArrayList<UserCard>()
    private var excludedMemberIds = ArrayList<String>()
    private var selectedMemberIds = ArrayList<String>()

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, groupId: String): Intent {
            return Intent(context, InviteMembersActivity::class.java)
                    .putExtra("groupId", groupId)
        }
    }

    //Class to just fetch members to exclude when inviting
    private class AllMemberIds(
            val member_ids: HashMap<String, Boolean> = HashMap<String, Boolean>(),
            val invited_member_ids: HashMap<String, Boolean> = HashMap<String,Boolean>()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_layout)
        loadExcludeList()
    }

    private fun loadExcludeList() {
        groupId = intent.getStringExtra("groupId")
        currentGroupRef = FirebaseDatabase.getInstance().getReference("/$F_GROUPS_TAG/$groupId")

        //Getting member ids to exclude
        currentGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val allMemberIds = dataSnapshot.getValue(AllMemberIds::class.java)
                    if (allMemberIds != null) {
                        excludedMemberIds.addAll(allMemberIds.member_ids.keys)
                        excludedMemberIds.addAll(allMemberIds.invited_member_ids.keys)
                    }
                    loadLayout()
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })

    }

    private fun loadLayout() {
        setContentView(R.layout.invite_members)
        searchEditText.setSingleLine(true)
        searchEditText.setHint(R.string.search_name_hint)
        searchEditText.setOnEditorActionListener(this)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        resultsListView.adapter = SelectUsersAdapter(this, displayUserCards, selectedMemberIds)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.invite_members, menu)
        return true
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        TODO("add functionality here")
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_done) {
            if (selectedMemberIds.isNotEmpty()) {
                val resultIntent = Intent().putExtra("selectedMemberIds", selectedMemberIds)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                onBackPressed()
            }
        } else {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}