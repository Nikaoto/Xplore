package com.xplore.groups

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.ImageUtil
import com.xplore.R
import com.xplore.user.UserCard
import kotlinx.android.synthetic.main.user_list_item2.view.*

/**
 * Created by Nikaoto on 8/6/2017.
 * TODO write description of this class - what it does and why.
 */
class SelectUsersAdapter(val activity: Activity,
                         val userCardList: ArrayList<UserCard>,
                         var selectedUserIds: ArrayList<String>)
    : ArrayAdapter<UserCard>(activity, R.layout.user_list_item2, userCardList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View
        if (convertView == null) {
            itemView = activity.layoutInflater.inflate(R.layout.user_list_item2, parent, false)
        } else {
            itemView = convertView
        }

        val userCard = userCardList[position]

        //Profile pic
        Picasso.with(activity)
                .load(userCard.profile_picture_url)
                .transform(ImageUtil.tinyCircle(activity))
                .into(itemView.userImageView)
        //Profile pic onclick
        itemView.userImageView.setOnClickListener {
            General.openUserProfile(activity, userCard.id)
        }
        //Full name
        itemView.userFullNameTextView.text = userCard.getFullName()
        //Reputation
        itemView.userRepCombinedTextView.text = userCard.getCombinedReputationText(activity)
        //Checkbox
        itemView.checkbox.setOnCheckedChangeListener { _, isChecked ->
            val userCard = userCardList[position]
            if (isChecked) {
                selectedUserIds.add(userCard.id)
            } else {
                selectedUserIds.remove(userCard.id)
            }
        }

        return itemView
    }
}