package com.xplore.groups

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.ImageUtil
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.groups.view.GroupInfoActivity

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

    val startDatePrefix: String
    val startDateSuffix: String

    init {
        TimeManager.refreshGlobalTimeStamp()

        val prefix = activity.resources.getString(R.string.group_card_start_date_prefix)
        if (prefix.isNotEmpty()) {
            startDatePrefix = prefix + " "
        }
        else
            startDatePrefix = ""

        val suffix = activity.resources.getString(R.string.group_card_start_date_suffix)
        if (suffix.isNotEmpty())
            startDateSuffix = " " + suffix
        else
            startDateSuffix = ""
    }

    class ResultsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //Leader
        internal val leaderLayout: RelativeLayout = itemView.leaderLayout
        internal val leaderName: TextView = itemView.leaderNameTextView
        internal val leaderImage: ImageView = itemView.leaderImageView
        internal val leaderReputation: TextView = itemView.leaderRepCombinedTextView
        //Group
        internal val groupImage: ImageView = itemView.reserveImageView
        //Group marks
        internal val beenHereMark: ImageView = itemView.beenHereMark
        internal val invitedMark: ImageView = itemView.invitedMark
        //Footer marks
        internal val memberCount: TextView = itemView.memberCountTextView
        internal val startDate: TextView = itemView.startDateTextView
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
                .transform(ImageUtil.tinyCircle(activity))
                .into(holder.leaderImage)
        holder.leaderImage.setOnClickListener { General.openUserProfile(activity, group.leaderId) }

        //Group image
        //TODO change this to just map or submitted image
        holder.groupImage.setImageResource(group.reserveImageId)

        //Marks
        if (group.invite) { holder.invitedMark.visibility = View.VISIBLE }
        if (group.experienced) { holder.beenHereMark.visibility = View.VISIBLE }

        //Member count
        holder.memberCount.text = group.memberCount.toString()
        val days = group.getStartInDays()
        if (days < 30) {
            when (days) {
                0 -> holder.startDate.text = activity.resources.getString(R.string.today)
                1 -> holder.startDate.text = activity.resources.getString(R.string.tomorrow)
                2 -> holder.startDate.text = activity.resources.getString(R.string.overmorrow)
                3 -> holder.startDate.text = activity.resources.getString(R.string.overovermorrow)
                else -> holder.startDate.text = "$startDatePrefix$days$startDateSuffix"
            }
        } else if (days/30 == 1) {
            holder.startDate.text = activity.resources.getString(R.string.group_card_start_date_one_month)
        } else {
            holder.startDate.text = "$startDatePrefix${days/30} ${activity.resources.getString(R.string.group_card_start_date_month_suffix)}"
        }
    }

    override fun getItemCount(): Int {
        return groupCards.size
    }
}