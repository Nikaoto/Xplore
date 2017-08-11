package com.xplore.groups.requests

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.ImageUtil
import com.xplore.R
import kotlinx.android.synthetic.main.received_join_request_list_item.view.*
import kotlinx.android.synthetic.main.request_list.*

/**
 * Created by Nikaoto on 8/8/2017.
 *
 * აღწერა:
 * აქედან შუძლია ლიდერს რომ მოაწესრიგოს ხალხისან მიღებული ამ გუნდში გაწევრიანების თხოვნები
 *
 * Description:
 * From here, the leader can manage received join requests from other users for his group
 *
 */
class ReceivedRequestsFragment() : RequestListFragment() {

    companion object {
        @JvmStatic
        fun newInstance(groupId: String, userIds: ArrayList<String>): ReceivedRequestsFragment {
            val fragment = ReceivedRequestsFragment()
            val args = Bundle()
            args.putString(ARG_GROUP_ID, groupId)
            args.putStringArrayList(ARG_USER_IDS, userIds)
            fragment.arguments = args
            return fragment
        }
    }

/*  TODO test loading with onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }*/

    private fun print(s: String) = Log.println(Log.INFO, "brejk", s)

    override fun displayCards() {
        super.displayCards()
        userCardRecyclerView.adapter = ReceivedRequestAdapter()
    }

    private inner class ReceivedRequestAdapter
        : RecyclerView.Adapter<ReceivedRequestAdapter.ReceivedRequestViewHolder>() {

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
                acceptRequest(currentCard.id)
                removeCardAt(position)
            }
            //Reject join request button
            holder.rejectButton.setOnClickListener {
                rejectRequest(currentCard.id)
                removeCardAt(position)
            }
        }

        private fun acceptRequest(userId: String) {
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
        }

        private fun rejectRequest(userId: String) {
            //Removing join request from group
            currentGroupRef.child(F_INVITED_MEMBER_IDS).child(userId).removeValue()

            //Removing join request from user
            usersRef.child(userId).child(F_INVITED_GROUP_IDS).child(groupId).removeValue()
        }

        override fun getItemCount() = userCards.size
    }
}