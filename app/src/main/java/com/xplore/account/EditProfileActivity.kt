package com.xplore.account

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.xplore.General
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.user.User
import kotlinx.android.synthetic.main.register_layout.*

/**
 * Created by Nik on 9/5/2017.
 * TODO write description of this class - what it does and why.
 */

class EditProfileActivity : RegisterActivity() {

    private val TAG = "profile"

    private val currentUser: User by lazy {
        intent.getSerializableExtra(USER) as User
    }

    companion object {
        // Arguments
        private const val USER = "user"

        @JvmStatic
        fun getStartIntent(context: Context, user: User): Intent {
            return Intent(context, EditProfileActivity::class.java)
                    .putExtra(USER, user)
        }
    }

    init { TimeManager.refreshGlobalTimeStamp() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "fname = ${currentUser.fname}")
        Log.i(TAG, "lname = ${currentUser.lname}")
        Log.i(TAG, "email = ${currentUser.email}")
        Log.i(TAG, "telnum = ${currentUser.tel_num}")
        Log.i(TAG, "bdate = ${currentUser.birth_date}")
        Log.i(TAG, "picUrl = ${currentUser.profile_picture_url}")
    }

    override fun initLayout() {
        setContentView(R.layout.register_layout)
        titleTextView.setText(R.string.edit_profile)
    }

    override fun fillFields() {
        fnameEditText.setText(currentUser.fname)
        lnameEditText.setText(currentUser.lname)
        emailEditText.setText(currentUser.email)
        numEditText.setText(currentUser.tel_num)
        birthDateTextView.setText(General.putSlashesInDate(currentUser.birth_date))
        imagePath = Uri.parse(currentUser.profile_picture_url)
    }

    override fun initProfileImage(userProfilePicUrl: String?) {
        super.initProfileImage(currentUser.profile_picture_url)
    }

    override fun onBirthDateSelected(timeStamp: Long) {
        super.onBirthDateSelected(General.convertIntDateToTimeStamp(currentUser.birth_date))
    }

    override fun onDoneButtonClick() {
        if (fieldsChanged()) {
            super.onDoneButtonClick()
        } else {
            finish()
        }
    }

    //TODO do this
    private fun fieldsChanged(): Boolean {
        return false
    }

    override fun onBackPressed() {
        finish()
    }
}