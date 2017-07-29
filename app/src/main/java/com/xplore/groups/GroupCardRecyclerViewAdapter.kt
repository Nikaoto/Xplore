package com.xplore.groups

import android.app.Activity
import android.opengl.Visibility
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.squareup.picasso.Picasso
import com.xplore.CircleTransformation
import com.xplore.General
import com.xplore.R
import com.xplore.groups.search.GroupInfoActivity

import java.util.ArrayList
import kotlinx.android.synthetic.main.group_card.view.*

/**
 * Created by Nika on 7/17/2017.

 * აღწერა:
 * ეს კლასი არის GroupCard კლასის RecyclerView-ს ადაპტერი. ვიყენებთ რომ ჯგუფების სია ვანახოთ
 * CardView-ებზე.

 * Description:
 * This class is a RecyclerView adapter for GroupCard arraylists.
 * It's used to show a list of groups on CardViews.

 */

class GroupCardRecyclerViewAdapter(private val groupCards: ArrayList<GroupCard>,
                                           private val activity: Activity)
    : RecyclerView.Adapter<GroupCardRecyclerViewAdapter.ResultsViewHolder>() {

    private val imgSize: Int

    init {
        imgSize = Math.round(activity.resources.getDimension(R.dimen.user_profile_image_tiny_size))
    }

    class ResultsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //Leader
        internal val leaderLayout: RelativeLayout = itemView.leaderLayout
        internal val leaderName: TextView = itemView.leaderNameTextView
        internal val leaderImage: ImageView = itemView.leaderImageView
        internal val leaderReputation: TextView = itemView.leaderRepCombinedTextView
        //Group
        internal val groupImage: ImageView = itemView.groupImageView
        //Group marks
        internal val beenHereMark: ImageView = itemView.beenHereMark
        internal val invitedMark: ImageView = itemView.invitedMark
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        return ResultsViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.group_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        val group = groupCards[position]

        //Leader layout
        holder.leaderLayout.setOnClickListener { General.openUserProfile(activity, group.leaderId) }

        //On card click
        holder.itemView.setOnClickListener {
            General.HideKeyboard(activity)

            //Starting intent
            activity.startActivity(
                    GroupInfoActivity.getStartIntent(
                            activity,
                            group.id,
                            Integer.valueOf(group.destination_id)
                    )
            )
        }

        //Leader name
        holder.leaderName.text = group.leaderName

        //Leader reputation
        holder.leaderReputation.text = "${group.leaderReputation} ${activity.resources.getString(R.string.reputation)}"

        //Leader image
        Picasso.with(activity)
                .load(group.leaderImageUrl)
                .transform(CircleTransformation(imgSize, imgSize))
                .into(holder.leaderImage)
        holder.leaderImage.setOnClickListener { General.openUserProfile(activity, group.leaderId) }

        //Group image
        //TODO change this to just map or submitted image
        holder.groupImage.setImageResource(group.reserveImageId)

        //Displaying marks
        if (group.invite) { holder.invitedMark.visibility = View.VISIBLE }
        if (group.experienced) { holder.beenHereMark.visibility = View.VISIBLE }
    }

    override fun getItemCount(): Int {
        return groupCards.size
    }
}