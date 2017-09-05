package com.xplore.account

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.xplore.DateUtil
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

    override fun initLayout() {
        setContentView(R.layout.register_layout)
        titleTextView.setText(R.string.edit_profile)
    }

    override fun fillFields() {
        fnameEditText.setText(currentUser.fname)
        lnameEditText.setText(currentUser.lname)
        emailEditText.setText(currentUser.email)
        numEditText.setText(currentUser.tel_num)
        birthDateTextView.setText(DateUtil.putSlashesInDate(currentUser.birth_date))

        bYear = DateUtil.getYear(currentUser.birth_date.toString())
        bMonth = DateUtil.getMonth(currentUser.birth_date.toString())
        bDay = DateUtil.getDay(currentUser.birth_date.toString())
    }

    override fun initProfileImage(userProfilePicUrl: String?) {
        super.initProfileImage(currentUser.profile_picture_url)
    }

    override fun onBirthDateSelected(timeStamp: Long, offSet: Int) {
        super.onBirthDateSelected(General.convertIntDateToTimeStamp(currentUser.birth_date), 0)
    }

    override fun onDoneButtonClick() {
        if (fieldsChanged()) {
            updateAuthEmail()
            super.onDoneButtonClick()
        } else {
            finish()
        }
    }

    // Updates auth email if user is not signed in with facebook
    private fun updateAuthEmail() {
        if (General.isValidEmail(emailEditText.text)) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                for (profile in user.providerData) {
                    if (FacebookAuthProvider.PROVIDER_ID != profile.providerId) {
                        user.updateEmail(emailEditText.text.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "User email address updated.")
                                    }
                                }
                    }
                }
            } else {
                // How the f do you even get here?
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
        } else {
            // Invalid email prompt
            makeBorderRed(emailEditText)
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fieldsChanged(): Boolean {
        if (fnameEditText.text.trim() != currentUser.fname) {
            return true
        }
        if (lnameEditText.text.trim() != currentUser.lname) {
            return true
        }
        if (emailEditText.text.trim() != currentUser.email) {
            return true
        }
        if (birthDateTextView.text != DateUtil.putSlashesInDate(currentUser.birth_date)) {
            return true
        }
        if (numEditText.text.trim() != currentUser.tel_num) {
            return true
        }
        if (imagePath != null) {
            return true
        }
        return false
    }

    override fun onBackPressed() {
        finish()
    }
}