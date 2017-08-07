package com.xplore.groups.my

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xplore.General
import com.xplore.R
import com.xplore.groups.AllGroupIdsForMember

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInst: Bundle?)
            = inflater.inflate(R.layout.loading_layout, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        //Loads joined group Ids for current user
        val query = firebaseUsersRef.child(General.currentUserId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val groupIds = dataSnapshot.getValue(AllGroupIdsForMember::class.java)
                    if (groupIds != null) {
                        if (groupIds.group_ids.isNotEmpty() || groupIds.invited_group_ids.isNotEmpty()) {

                            loadMyGroupsLayout(groupIds.group_ids, groupIds.invited_group_ids)

                        } else { loadEmptyLayout() }
                    } else { loadEmptyLayout() }
                } else { printError() }
            }
            override fun onCancelled(p0: DatabaseError?) { }
        })
    }

    fun loadEmptyLayout()
         = fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EmptyMyGroupsFragment()).commit()

    fun loadMyGroupsLayout(joinedGroupIds: HashMap<String, Boolean>,
                           invitedGroupIds: HashMap<String, Boolean>)
         = fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyGroupsFragment.newInstance(joinedGroupIds, invitedGroupIds))
                .commit()

    fun printError() = Toast.makeText(activity, "Error retrieving data", Toast.LENGTH_SHORT).show()
}