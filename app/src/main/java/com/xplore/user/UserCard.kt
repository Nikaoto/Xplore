package com.xplore.user

import android.content.Context
import com.xplore.R

/**
 * Created by Nikaoto on 3/1/2017.
 */

//TODO add google auth to the app and after modifying user database, add more stuff here like in groupButton,
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
            = context.resources.getString(R.string.reputation) + " " + reputation
}
