package com.xplore.groups

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.database.DBManager
import com.xplore.groups.view.GroupInfoActivity
import com.xplore.util.ImageUtil
import kotlinx.android.synthetic.main.group_card.view.*
import java.util.*

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

    private val dbManager = DBManager(activity)

    private val GROUP_NAME_MAX_CHARS = 35
    private val LEADER_NAME_MAX_CHARS = 20

    private val startDatePrefix: String
    private val startDateSuffix: String

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
        internal val groupName: TextView = itemView.groupNameTextView
        internal val groupImage: ImageView = itemView.reserveImageView
        //Group marks
        internal val beenHereMark: ImageView = itemView.beenHereMark
        internal val invitedMark: ImageView = itemView.invitedMark
        //Footer marks
        internal val memberCount: TextView = itemView.memberCountTextView
        internal val startDate: TextView = itemView.startDateTextView
        internal val duration: TextView = itemView.durationTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        return ResultsViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.group_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        dbManager.openDataBase()
        val group = groupCards[position]

        //Leader layout
        holder.leaderLayout.setOnClickListener { General.openUserProfile(activity, group.leaderId) }

        //On card click
        holder.itemView.setOnClickListener {
            General.hideKeyboard(activity)

            //Starting intent
            activity.startActivity(
                    GroupInfoActivity.newIntent(activity, group.id)
            )
        }

        //Leader name
        holder.leaderName.setText(group.leaderName, LEADER_NAME_MAX_CHARS)

        //Leader reputation
        holder.leaderReputation.text = "${group.leaderReputation} ${activity.resources.getString(R.string.reputation)}"

        //Leader image
        Picasso.with(activity)
                .load(group.leaderImageUrl)
                .transform(ImageUtil.tinyCircle(activity))
                .into(holder.leaderImage)
        holder.leaderImage.setOnClickListener { General.openUserProfile(activity, group.leaderId) }

        //Group name
        holder.groupName.setText(group.name, GROUP_NAME_MAX_CHARS)

        //Group image
        if (group.destination_id == Group.DESTINATION_DEFAULT) {
            Picasso.with(activity).load(group.group_image_url).into(holder.groupImage)
        } else {
            Picasso.with(activity)
                    .load(dbManager.getImageId(group.destination_id))
                    .into(holder.groupImage)
        }

        //Marks
        if (group.invite) { holder.invitedMark.visibility = View.VISIBLE }
        if (group.experienced) { holder.beenHereMark.visibility = View.VISIBLE }

        //Member count
        holder.memberCount.text = group.memberCount.toString()

        //Days from today
        val daysFromNow = group.getStartInDays()
        if (daysFromNow < 0) {
            holder.startDate.text = "-"
        } else if (daysFromNow < 30) {
            when (daysFromNow) {
                0 -> holder.startDate.text = activity.resources.getString(R.string.today)
                1 -> holder.startDate.text = activity.resources.getString(R.string.tomorrow)
                2 -> holder.startDate.text = activity.resources.getString(R.string.overmorrow)
                3 -> holder.startDate.text = activity.resources.getString(R.string.overovermorrow)
                else -> holder.startDate.text = "$startDatePrefix$daysFromNow$startDateSuffix"
            }
        } else if (daysFromNow/30 == 1) {
            holder.startDate.setText(R.string.group_card_start_date_one_month)
        } else {
            holder.startDate.text = "$startDatePrefix${daysFromNow/30} ${activity.resources.getString(R.string.group_card_start_date_month_suffix)}"
        }

        //Duration
        val durationInDays = group.getDurationInDays()
        if (durationInDays < 1) {
            holder.duration.setText(R.string.duration_zero_day)
        } else {
            holder.duration.text = "$durationInDays ${activity.resources.getString(R.string.duration_days)}"
        }

        dbManager.close()
    }

    fun TextView.setText(s: String, limitChars: Int) {
        if (s.length < limitChars) {
            this.text = s
        } else {
            this.text = "${s.substring(0, limitChars)}..."
        }
    }

    override fun getItemCount(): Int {
        return groupCards.size
    }
}