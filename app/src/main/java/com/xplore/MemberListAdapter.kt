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
import com.xplore.user.User
import com.xplore.util.ImageUtil
import java.util.*

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
                        private var users: ArrayList<User>,
                        private val allowUserRemoval: Boolean = false,
                        var userIds: ArrayList<String>? = null)
    : RecyclerView.Adapter<MemberListAdapter.MemberViewHolder>() {

    override fun getItemCount() = users.size

    //The holder of the user layout
    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rep_txt: TextView = itemView.findViewById<TextView>(R.id.member_rep_text)
        var memberImage: ImageView = itemView.findViewById<ImageView>(R.id.member_profile_image)
    }

    //Inflate a user layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MemberViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.member_list_item, parent, false))

    //After inflating user layout and binding with the holder
    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        var currentMember = users[position]

        //Loading Member Reputation
        holder.rep_txt.text = currentMember.reputation.toString()

        //Loading Member Image
        Picasso.with(context).invalidate(currentMember.profile_picture_url)
        Picasso.with(context)
                .load(currentMember.profile_picture_url)
                .transform(ImageUtil.smallCircle(context))
                .into(holder.memberImage)

        //Configuring Clicks
        holder.memberImage.setOnClickListener {
            currentMember = users[position] //DO NOT REMOVE, THIS IS NECESSARY
            General.openUserProfile(context as Activity, currentMember.id)
        }

        //If is creating a group, allow user to remove members
        if (allowUserRemoval) {
            //Pops up "remove?" dialog
            holder.memberImage.setOnLongClickListener {
                currentMember = users[position]
                if (currentMember.id != General.currentUserId) {
                    General.vibrateDevice(context)
                    AlertDialog.Builder(context)
                            .setTitle(context.resources.getString(R.string.to_remove)+" "
                                    + currentMember.fname +
                                    " " + currentMember.lname)
                            .setMessage(R.string.remove_member_question)
                            .setCancelable(false)
                            .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                            .setPositiveButton(R.string.yes) { _, _ ->
                                //Removing user and updating recycler view
                                users.removeAt(position)
                                userIds?.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, users.size)
                                Toast.makeText(context, R.string.member_removed,
                                        Toast.LENGTH_SHORT).show()
                            }
                            .create().show()
                }
                false
            }
        }
    }
}
