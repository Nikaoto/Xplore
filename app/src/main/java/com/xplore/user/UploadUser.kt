package com.xplore.user

import com.google.firebase.database.Exclude
import com.xplore.util.FirebaseUtil
import java.util.HashMap

/**
 * Created by Nika on 9/19/2017.
 * TODO write description of this class - what it does and why.
 */
class UploadUser(id: String,
                 fname: String,
                 lname: String,
                 tel_num: String,
                 email: String,
                 profile_picture_url: String,
                 reputation: Int,
                 birth_date: Int)
    : User(id, fname, lname, tel_num, email, profile_picture_url, reputation, birth_date) {

    @Exclude
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
    }
}