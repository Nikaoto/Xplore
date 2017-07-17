package com.xplore

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.squareup.picasso.Picasso

import java.util.ArrayList

import com.xplore.groups.create.CreateGroupActivity.invitedMembers
import com.xplore.user.User

/**
 * Created by Nikaoto on 3/4/2017.
 *
 * აღწერა:
 * ამ ადაპტერს ვიყენებთ რომ ვაჩვენოთ ჯგუფის წევრები ჰორიზონტალური RecyclerView ში. შეიძლება
 * იყოს გამოყენებული ჯგუფის შექმნისას (allowRemoval ჭეშმარიტი უნდა იყოს) ან ჯგუფის დათვალიერებისას.
 *
 * Description:
 * This is an adapter for displaying the members of a group in a horizontal recycler view.
 * This can be used when creating a group (allowRemoval = true) and when viewing it.
 */

class MemberListAdapter(private val context: Context,
                        private val users: ArrayList<User>,
                        private val allowUserRemoval: Boolean = false)
    : RecyclerView.Adapter<MemberListAdapter.MemberViewHolder>() {

    private val imgSize = Math.round(context.resources.getDimension(R.dimen.user_profile_image_small_size))
    private var currentMember = User()

    override fun getItemCount() = users.size

    //The holder of the user layout
    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rep_txt: TextView = itemView.findViewById(R.id.member_rep_text) as TextView
        var memberImage: ImageView = itemView.findViewById(R.id.member_profile_image) as ImageView
    }

    //Inflate a user layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MemberViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.member_list_item, parent, false))

    //After inflating user layout and binding with the holder
    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        currentMember = users[position]

        //Loading Member Reputation
        holder.rep_txt.text = currentMember.getReputation().toString()

        //If leader, display higher res image
        if (position == 0) {
            Picasso.with(context).invalidate(currentMember.profile_picture_url)
        }

        //Loading Member Image
        Picasso.with(context)
                .load(currentMember.getProfile_picture_url())
                .transform(CircleTransformation(imgSize, imgSize))
                .into(holder.memberImage)

        //Configuring Clicks
        holder.memberImage.setOnClickListener {
            currentMember = users[position] //DO NOT REMOVE, THIS IS NECESSARY
            General.openUserProfile(context as Activity, currentMember.getId())
        }

        //If is creating a group, allow user to remove members
        if (allowUserRemoval) {
            //Pops up "remove?" dialog
            holder.memberImage.setOnLongClickListener {
                currentMember = users[position]
                General.vibrateDevice(context, null)
                //TODO string resources
                AlertDialog.Builder(context)
                        .setTitle("Remove " + currentMember.getFname() + " " + currentMember.getLname())
                        .setMessage("Do you wish to remove this member from your group?")
                        .setCancelable(false)
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                        .setPositiveButton("Yes") { _, _ ->
                            //Removing user and updating recycler view
                            invitedMembers.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, invitedMembers.size)
                            Toast.makeText(context, R.string.member_removed, Toast.LENGTH_SHORT).show()
                        }
                        .create().show()
                false
            }
        }
    }
}