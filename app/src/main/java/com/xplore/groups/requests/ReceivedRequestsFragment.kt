package com.xplore.groups.requests

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.xplore.R
import com.xplore.user.UserCard
import kotlinx.android.synthetic.main.request_list.*

/**
 * Created by Nika on 8/8/2017.
 * TODO write description of this class - what it does and why.
 */
class ReceivedRequestsFragment() : Fragment() {

    //Firebase
    private val userRef = FirebaseDatabase.getInstance().getReference("users")
    lateinit private var currentGroupRef: DatabaseReference

    lateinit private var groupId: String
    lateinit private var userIds: ArrayList<String>

    private val userCards = ArrayList<UserCard>()

    companion object {
        @JvmStatic
        fun newInstance(groupId: String, userIds: ArrayList<String>): ReceivedRequestsFragment {
            val fragment = ReceivedRequestsFragment()
            val args = Bundle()
            args.putString("groupId", groupId)
            args.putStringArrayList("userIds", userIds)
            fragment.arguments = args
            return fragment
        }
    }
/*  TODO test loading with onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
            = inflater.inflate(R.layout.request_list, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = arguments.getString("groupId")
        currentGroupRef = FirebaseDatabase.getInstance().getReference("groups/$groupId")
        userIds = arguments.getStringArrayList("userIds")

        //Start loading
        for (userId in userIds) {
            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if (dataSnapshot != null) {
                        val userCard = dataSnapshot.getValue(UserCard::class.java)
                        if (userCard != null) {
                            userCards.add(userCard)
                        }
                    }
                    if (userId == userIds[userIds.size - 1]) {
                        displayCards(userCards)
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }

    private fun displayCards(userCards: ArrayList<UserCard>) {
        //userCardRecyclerView.adapter =
    }
}