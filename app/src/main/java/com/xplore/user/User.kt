package com.xplore.user

import com.google.firebase.database.Exclude
import com.xplore.util.FirebaseUtil
import java.io.Serializable
import java.util.HashMap

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

    @Exclude
    fun getFullName() = "$fname $lname"

/*    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()

        result.put(FirebaseUtil.F_FNAME, fname)
        result.put(FirebaseUtil.F_LNAME, lname)
        result.put(FirebaseUtil.F_PROFILE_PIC_URL, profile_picture_url)
        result.put(FirebaseUtil.F_BIRTH_DATE, birth_date)
        result.put(FirebaseUtil.F_TEL_NUM, tel_num)
        result.put(FirebaseUtil.F_REPUTATION, reputation)
        result.put(FirebaseUtil.F_EMAIL, email)

        return result
    }*/
}