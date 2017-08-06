package com.xplore.groups.view.controls

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.database.*
import com.xplore.General
import com.xplore.R
import com.xplore.database.FirebaseUserSearch
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
    private val F_FNAME_TAG = "fname"
    private val F_LNAME_TAG = "lname"
    private lateinit var currentGroupRef: DatabaseReference

    private lateinit var groupId: String

    private var displayUserCards = ArrayList<UserCard>()
    private var excludedMemberIds = ArrayList<String>()
    private var selectedMemberIds = ArrayList<String>()

    //For searching users
    private val fUserSearch = FirebaseUserSearch(displayUserCards, { displayUserList() } )

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
        setTitle(R.string.invite_members)
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.invite_members, menu)
        return true
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (!v.text.isBlank()) {
            val searchQuery = v.text.toString().toLowerCase()

            displayUserCards.clear()

            //TODO search by..
            fUserSearch.prepareForSearch()

            if (searchQuery.contains(" ")) {
                val parts = searchQuery.split(" ".toRegex(), 2)
                //Search with 1 -> 2
                fUserSearch.loadUsersWithFullName(parts[0], parts[1])
                fUserSearch.loadUsersWithFullName(firstLetterUpper(parts[0]), firstLetterUpper(parts[1]))
                //Now the other way around (2 -> 1)
                fUserSearch.loadUsersWithFullName(parts[1], parts[0])
                fUserSearch.loadUsersWithFullName(firstLetterUpper(parts[1]), firstLetterUpper(parts[0]))
            } else {
                fUserSearch.loadUsersWithTags(searchQuery, F_FNAME_TAG, F_LNAME_TAG, true)
                fUserSearch.loadUsersWithTags(firstLetterUpper(searchQuery), F_FNAME_TAG, F_LNAME_TAG, true)
            }
        }
        return false
    }

    private fun displayUserList() {
        resultsListView.adapter = SelectUsersAdapter(this,
                filterDuplicates(displayUserCards, excludedMemberIds), selectedMemberIds)
    }

    private fun filterDuplicates(userCardList: ArrayList<UserCard>, exclude: ArrayList<String>)
            : ArrayList<UserCard> {
        val ans = ArrayList<UserCard>(userCardList.size)
        var duplicate = false
        for (i in 0 .. userCardList.size - 1) {
            if (!exclude.contains(userCardList[i].id)) {
                duplicate = false
                for (j in i + 1 .. userCardList.size - 1) {
                    if (userCardList[i].id == userCardList[j].id) {
                        duplicate = true
                        break
                    }
                }
                if (!duplicate) {
                    Log.println(Log.INFO, "brejk", "adding ${userCardList[i].fname}")
                    ans.add(userCardList[i])
                }
            }
        }
        return ans
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

    private fun firstLetterUpper(s: String) = s.substring(0, 1).toUpperCase() + s.substring(1)
}