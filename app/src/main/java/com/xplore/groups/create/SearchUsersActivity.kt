package com.xplore.groups.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xplore.CircleTransformation
import com.xplore.General
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.user.User

import java.util.ArrayList

import kotlinx.android.synthetic.main.search_layout.*
import com.xplore.groups.create.CreateGroupFragment.invitedMembers


/**
 * Created by Nikaoto on 3/1/2017.

 * არწერა:
 * ეს მოქმედება იხსნება როდესაც მომხარებელი ჯგუფს ქმნის სა იწყებს თანამოლაშქრეების ძებნას რომ ჩაამატოს.

 * Description:
 * This activity opens up when the user is creating a group and starts searching for members to add.

 */

class SearchUsersActivity : Activity(), TextView.OnEditorActionListener {

    private val DB_FNAME_TAG = "fname"
    private val DB_LNAME_TAG = "lname"

    private var memberAdded: Boolean = false
    private val userList = ArrayList<User>() //replace with UserButtons

    /*
    private String USERBASE_KEY;
    private String USERBASE_APPID;
    private String USERBASE_URL;

    DatabaseReference dbRef;
    FirebaseApp userBaseApp;
    FirebaseOptions userBaseOptions;
    */

    internal val firebaseUsersReference = FirebaseDatabase.getInstance().reference.child("users")

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_layout)

        TimeManager.refreshGlobalTimeStamp()

        searchEditText.setSingleLine(true)
        searchEditText.setHint(R.string.search_users)
        searchEditText.setOnEditorActionListener(this)

        //buildUserBase();
    }

    private fun loadUsersWithTag(queries: Array<String>, tags: Array<String>) {
        if (queries.size > tags.size) {
            nothingFound()
        } else {
            for (query in queries) {
                for (tag in tags) {
                    val dbQuery = firebaseUsersReference.orderByChild(tag).startAt(query).endAt(query + "\uf8ff") //UF8FF needed for unicode search
                    dbQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            if (dataSnapshot!!.exists()) {
                                var tempUser: User
                                for (userSnapshot in dataSnapshot.children) {
                                    tempUser = userSnapshot.getValue(User::class.java)!!
                                    tempUser.id = userSnapshot.key
                                    userList.add(tempUser)
                                }
                            }
                            displayUserList()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            nothingFound()
                        }
                    })
                }
            }
        }
    }

    private fun nothingFound() {
        Toast.makeText(this@SearchUsersActivity, R.string.search_no_results, Toast.LENGTH_SHORT).show()
    }

    private fun displayUserList() {
        resultsListView.adapter = UserListAdapter(filterDuplicates(userList)) //change User to UserButtons
        //listView.getAdapter().notifyAll();
    }

    //Filters duplicate users from given array and returns filtered list
    // Also removes the currently logged in user from the list
    private fun filterDuplicates(list: ArrayList<User>): ArrayList<User> {
        var foundDuplicate: Boolean
        val ansList = ArrayList<User>()
        //Filtering duplicates into ansList
        for (i in list.indices) {
            foundDuplicate = false
            //Checking if list[i] is a duplicate
            for (j in i..list.size - 1) {
                if (i != j && list[j].id == list[i].id) {
                    foundDuplicate = true
                    break
                }
            }
            //If we have a user that is not a duplicate and not the user itself
            if (!foundDuplicate && list[i].id != General.currentUserId) {
                ansList.add(list[i])
            }
        }
        return ansList
    }

    private inner class UserListAdapter(private val answerList: ArrayList<User>) : ArrayAdapter<User>(this@SearchUsersActivity, R.layout.group_list_item, answerList) { //change to userbutton
        private val imgSize: Int

        init {
            imgSize = Math.round(resources.getDimension(R.dimen.user_profile_image_small_size))
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            if (itemView == null) {
                itemView = layoutInflater
                        .inflate(R.layout.user_list_item, parent, false)
            }
            val currentUser = answerList[position]

            //Loading Data///////////////////////////////////////////////////////////////////////

            //Reputation
            val rep_text = itemView!!.findViewById(R.id.user_rep_text_combined) as TextView
            rep_text.text = "${currentUser.reputation.toString()} ${resources.getString(R.string.reputation)}"

            //First Name
            val fname_text = itemView.findViewById(R.id.user_fname_text) as TextView
            fname_text.text = currentUser.fname

            //Last Name
            val lname_text = itemView.findViewById(R.id.user_lname_text) as TextView
            lname_text.text = currentUser.lname

            //Age
            val age_text = itemView.findViewById(R.id.user_age_text) as TextView
            age_text.text = "${resources.getString(R.string.age)} ${General.calculateAge(TimeManager.globalTimeStamp, currentUser.birth_date)}"


            //Profile Picture
            val userImage = itemView.findViewById(R.id.user_profile_image) as ImageView
            val userImageRef = currentUser.profile_picture_url
            Picasso.with(context)
                    .load(userImageRef)
                    .transform(CircleTransformation(imgSize, imgSize))
                    .into(userImage)

            //Configuring Clicks
            itemView.setOnClickListener {
                if (userAlreadyInvited(currentUser)) {
                    Toast.makeText(this@SearchUsersActivity, R.string.member_already_added,
                            Toast.LENGTH_SHORT).show()
                } else {
                    memberAdded = true
                    invitedMembers.add(currentUser)
                    Toast.makeText(this@SearchUsersActivity, R.string.member_added,
                            Toast.LENGTH_SHORT).show()
                }
            }

            return itemView
        }
    }

    fun userAlreadyInvited(user: User): Boolean {
        for (u in invitedMembers) {
            if (u.id == user.id)
                return true
        }
        return false
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
        resultIntent.putExtra("member_added", memberAdded)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    //Returns passed string with the first letter in uppercase
    private fun firstLetterUpper(str: String): String {
        if (str.isEmpty())
            return str.toUpperCase()
        else
            return str.substring(0, 1).toUpperCase() + str.substring(1)
    }

    //Takes string array and returns every element with first letter uppercase
    private fun firstLetterUpper(strings: Array<String>) =
            Array<String>(strings.size, {firstLetterUpper(strings[it])})

    override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent?): Boolean {
        userList.clear()
        val searchQuery = textView.text.toString().toLowerCase()

        //TODO add search types and check here (search by..)
        val tags = arrayOf(DB_FNAME_TAG, DB_LNAME_TAG)
        val queries: Array<String>
        //Splitting searchQuery into queries
        if (searchQuery.contains(" ")) {
            queries = firstLetterUpper(searchQuery.split(" ".toRegex(), 2).toTypedArray())
        } else {
            queries = arrayOf(firstLetterUpper(searchQuery))
        }

        loadUsersWithTag(queries, tags)

        return false
    }
}
