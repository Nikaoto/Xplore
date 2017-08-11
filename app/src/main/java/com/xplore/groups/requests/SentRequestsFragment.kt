package com.xplore.groups.requests

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.ImageUtil
import com.xplore.R
import kotlinx.android.synthetic.main.request_list.*
import kotlinx.android.synthetic.main.sent_invite_list_item.view.*

/**
 * Created by Nika on 8/11/2017.
 *
 * The leader can manage sent invite requests from this class
 *
 */

class SentRequestsFragment : RequestListFragment() {

    companion object {
        @JvmStatic
        fun newInstance(groupId: String, userIds: ArrayList<String>): SentRequestsFragment {
            val fragment = SentRequestsFragment()
            val args = Bundle()
            args.putString(ARG_GROUP_ID, groupId)
            args.putStringArrayList(ARG_USER_IDS, userIds)
            fragment.arguments = args
            return fragment
        }
    }

    override fun displayCards() {
        super.displayCards()
        userCardRecyclerView.adapter = SentRequestAdapter()
    }

    private inner class SentRequestAdapter : RecyclerView.Adapter<SentRequestAdapter.SentRequestViewHolder>() {

        private inner class SentRequestViewHolder(itemView: View)
            : RecyclerView.ViewHolder(itemView) {

            internal val userImageView = itemView.userImageView
            internal val userFullName = itemView.userFullNameTextView
            internal val userReputation = itemView.userCombinedReputationTextView
            internal val cancelInviteButton = itemView.cancelInviteButton
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = SentRequestViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.sent_invite_list_item, parent, false))

        override fun onBindViewHolder(holder: SentRequestViewHolder, position: Int) {
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

            holder.cancelInviteButton.setOnClickListener {
                cancelInvite(userCards[position].id)
                removeCardAt(position)
            }
        }

        private fun cancelInvite(userId: String) {
            //Remove invited member id from group
            currentGroupRef.child(F_INVITED_MEMBER_IDS).child(userId).removeValue()

            //Remove group id from member's invited groups
            usersRef.child(userId).child(F_INVITED_GROUP_IDS).child(groupId).removeValue()
        }

        override fun getItemCount() = userCards.size
    }

}