package com.xplore.user

import java.io.Serializable

/*
 * Created by Nikaoto on 2/11/2017.
 */

//DO NOT CHANGE THE VARIABLE NAMES, THEY CORRESPOND TO THE FIREBASE DATABASE KEY NAMES
open class User(
        var id: String = "",
        val fname: String = "",
        val lname: String = "",
        val tel_num: String = "",
        val email: String = "",
        var profile_picture_url: String = "",
        val reputation: Int = 0,
        val birth_date: Int = 0
) : Serializable {

    fun getFullName() = "$fname $lname"
}