package com.xplore.user

import android.content.Context
import com.xplore.R

/**
 * Created by Nikaoto on 3/1/2017.
 */

//TODO also, change User arraylists in SearchUsersActivity to UserButton arraylists

class UserCard(
        var id: String = "",
        val fname: String = "",
        val lname: String = "",
        val reputation: Int = -1,
        val profile_picture_url: String = ""
) {
    fun getFullName() = "$fname $lname"
    fun getCombinedReputationText(context: Context)
            = "$reputation ${context.resources.getString(R.string.reputation)}"
}
