package com.xplore.groups.requests

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.xplore.R
import com.xplore.user.UserCard
import kotlinx.android.synthetic.main.loading_layout.*
import kotlinx.android.synthetic.main.request_list.*

/**
 * Created by Nikaoto on 8/8/2017.
 *
 * აღწერა:
 * ეს კლასი არის მშობელი ორი მსგავსი კლასის, Received- და Sent- RequestFragment-ის მშობელი.
 * შვილ კლასებს უნდა ქონდეთ newInstance სტატიკი საიდანაც იღებენ groupId-სა და userIds რომ ამ კლასმა
 * ჩატვირთოს ინფორმაცია. ასევე, შვილებს თავიანთი ადაპტერები ჭირდებათ, რომ წარმოადგინონ ინფორმაცია
 * როგორც საჭიროა.
 *
 * This is a parent class of Received- and Sent- RequestFragments' classes. The children of this
 * class must have a newInstance static void from which they grab groupId and userIds for this class
 * to load them. In addition, each child is required to have their own RecyclerView adapter to
 * display the retrieved data the way they want.
 *
 */

open class RequestListFragment : Fragment() {

    companion object {
        //Arguments
        val ARG_GROUP_ID = "groupId"
        val ARG_USER_IDS = "userIds"
    }

    //Firebase
    val F_GROUPS = "groups"
    val F_INVITED_GROUP_IDS = "invited_group_ids"
    val F_INVITED_MEMBER_IDS = "invited_member_ids"
    val F_GROUP_IDS = "group_ids"
    val F_MEMBER_IDS = "member_ids"
    val usersRef = FirebaseDatabase.getInstance().getReference("users")
    lateinit var currentGroupRef: DatabaseReference

    lateinit var groupId: String
    lateinit var userIds: ArrayList<String>

    val userCards = ArrayList<UserCard>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
            = inflater.inflate(R.layout.request_list, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = arguments.getString(ARG_GROUP_ID)
        currentGroupRef = FirebaseDatabase.getInstance().getReference("$F_GROUPS/$groupId")
        userIds = arguments.getStringArrayList(ARG_USER_IDS)

        //Start loading
        for (userId in userIds) {
            usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if (dataSnapshot != null) {
                        val userCard = dataSnapshot.getValue(UserCard::class.java)
                        if (userCard != null) {
                            userCard.id = dataSnapshot.key
                            userCards.add(userCard)
                        }
                    }
                    if (userId == userIds[userIds.size - 1]) {
                        displayCards()
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }

    open fun displayCards() {
        userCardRecyclerView.layoutManager = LinearLayoutManager(activity)
        loadingbar.visibility = View.INVISIBLE
        //This is where the child classes set their custom adapters for the RecyclerView
        //userCardRecyclerView.adapter = ReceivedRequestAdapter()
    }

    fun removeCardAt(position: Int) {
        userCards.removeAt(position)
        //Update list
        userCardRecyclerView.adapter.notifyItemRemoved(position)
        userCardRecyclerView.adapter.notifyItemRangeChanged(position, userCards.size)
        if (userCards.isEmpty()) {
            //TODO display empty layout (replacing fragment doesn't work)
        }
    }
}