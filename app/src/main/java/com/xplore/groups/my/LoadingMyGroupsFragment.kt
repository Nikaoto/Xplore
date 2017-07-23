package com.xplore.groups.my

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xplore.General
import com.xplore.R
import com.xplore.user.User

/**
 * Created by Nikaoto on 7/17/2017.
 *
 * აღწერა:
 * ეს კლასი რთავს ჩატვირთვის ანიმაციას სანამ ტვირთავს მომხმარებლის ჯგუფებს. თუ მომხმარებელი არის
 * ჯგუფებში, გახსნის MyGroupsFragment-ს და აჩვენებს ჯუფებს, თუ არა - EmptyGroupsFragment-ს
 *
 * Description:
 * This class opens a loading animation while it loads the user's joined groups. If the user has
 * groups, it opens the MyGroupsFragment, otherwise, it opens EmptyMyGroupsFragment
 *
 */
class LoadingMyGroupsFragment : Fragment() {

    private val firebaseUsersRef = FirebaseDatabase.getInstance().reference.child("users")

    //Used to only get group info, instead of everything. Speeds up loading
    private class UserGroups (
            val group_ids: ArrayList<String> = ArrayList(),
            val invited_group_ids: ArrayList<String> = ArrayList())

    //Starts loading ASAP
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        //Loads joined group Ids for current user
        val query = firebaseUsersRef.child(General.currentUserId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val user = dataSnapshot.getValue(UserGroups::class.java)
                    if (user != null) {
                        if (user.group_ids.isEmpty() && user.invited_group_ids.isEmpty()) {
                            loadEmptyLayout()
                        } else {
                            loadMyGroupsLayout(user.group_ids, user.invited_group_ids)
                        }
                    } else { printError() }
                } else { printError() }
            }
            override fun onCancelled(p0: DatabaseError?) { }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInst: Bundle?)
            = inflater.inflate(R.layout.loading_layout, container, false)

    fun loadEmptyLayout()
         = fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EmptyMyGroupsFragment()).commit()

    fun loadMyGroupsLayout(joinedGroupIds: ArrayList<String>, invitedGroupIds: ArrayList<String>)
         = fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyGroupsFragment(joinedGroupIds, invitedGroupIds))
                .commit()

    fun printError() = Toast.makeText(activity, "Error retrieving data", Toast.LENGTH_SHORT).show()
}