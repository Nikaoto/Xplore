package com.xplore.groups.requests

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.ImageUtil
import com.xplore.R
import com.xplore.empty.EmptyFragmentFactory
import com.xplore.user.UserCard
import kotlinx.android.synthetic.main.loading_layout.*
import kotlinx.android.synthetic.main.manage_requests.*
import kotlinx.android.synthetic.main.received_join_request_list_item.*
import kotlinx.android.synthetic.main.received_join_request_list_item.view.*
import kotlinx.android.synthetic.main.request_list.*

/**
 * Created by Nikaoto on 8/8/2017.
 *
 * აქედან შუძლია ლიდერს რომ მოაწესრიგოს ხალხისან მიღებული ამ გუნდში გაწევრიანების თხოვნები
 *
 * From here, the leader can manage received join requests from other users for his group
 *
 */
class ReceivedRequestsFragment() : Fragment() {

    //Firebase
    private val F_INVITED_GROUP_IDS = "invited_group_ids"
    private val F_INVITED_MEMBER_IDS = "invited_member_ids"
    private val F_GROUP_IDS = "group_ids"
    private val F_MEMBER_IDS = "member_ids"
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
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

    private fun print(s: String) = Log.println(Log.INFO, "brejk", s)

    private fun displayCards() {
        userCardRecyclerView.layoutManager = LinearLayoutManager(activity)
        userCardRecyclerView.adapter = ReceivedRequestAdapter()
        loadingbar.visibility = View.INVISIBLE
    }

    private fun refreshList() {
        userCardRecyclerView.adapter.notifyDataSetChanged()
    }

    private fun removeCardAt(position: Int) {
        userCards.removeAt(position)
        //Update list
        userCardRecyclerView.adapter.notifyItemRemoved(position)
        userCardRecyclerView.adapter.notifyItemRangeChanged(position, userCards.size)
        if (userCards.isEmpty()) {
            displayEmptyLayout()
        }
    }

    private fun displayEmptyLayout() {
        if (fragmentManager != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.page_container, EmptyFragmentFactory(0).getSupportFragment())
                    .commit()
        }
    }

    private inner class ReceivedRequestAdapter : RecyclerView.Adapter<ReceivedRequestAdapter.ReceivedRequestViewHolder>() {

        private inner class ReceivedRequestViewHolder(itemView: View)
            : RecyclerView.ViewHolder(itemView) {
            internal val userImageView = itemView.userImageView
            internal val userFullName = itemView.userFullNameTextView
            internal val userReputation = itemView.userCombinedReputationTextView
            internal val acceptButton = itemView.acceptButton
            internal val rejectButton = itemView.rejectButton
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = ReceivedRequestViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.received_join_request_list_item, parent, false))

        override fun onBindViewHolder(holder: ReceivedRequestViewHolder, position: Int) {
            val currentCard = userCards[position]

            //Profile picture
            Picasso.with(activity)
                    .load(currentCard.profile_picture_url)
                    .transform(ImageUtil.smallCircle(activity))
                    .into(holder.userImageView)
            holder.userImageView.setOnClickListener {
                General.openUserProfile(activity, currentCard.id)
            }

            //Full name
            holder.userFullName.text = currentCard.getFullName()

            //Reputation
            holder.userReputation.text = currentCard.getCombinedReputationText(activity)

            //Accept join request button
            holder.acceptButton.setOnClickListener {
                acceptRequest(userCards[position].id, position)
            }
            //Reject join request button
            holder.rejectButton.setOnClickListener {
                //rejectRequest(currentCard.id)
            }
        }

        private fun acceptRequest(userId: String, userCardPosition: Int) {
            //Removing join request from group
            currentGroupRef.child(F_INVITED_MEMBER_IDS).child(userId).removeValue()
            //Adding user to group's joined members
            currentGroupRef.child(F_MEMBER_IDS).child(userId).setValue(false)

            //Removing join request from user
            usersRef.child(userId).child(F_INVITED_GROUP_IDS).child(groupId).removeValue()
            //Adding groupId to user joined groups
            usersRef.child(userId).child(F_GROUP_IDS).child(groupId).setValue(false)

            //TODO string resources
            Toast.makeText(activity, "Member added", Toast.LENGTH_SHORT).show()

            //Remove user card from list
            removeCardAt(userCardPosition)
        }

        override fun getItemCount() = userCards.size
    }
}