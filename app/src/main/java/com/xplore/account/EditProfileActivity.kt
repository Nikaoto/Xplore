package com.xplore.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.xplore.util.DateUtil
import com.xplore.General
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.user.UploadUser
import com.xplore.user.User
import com.xplore.util.FirebaseUtil
import kotlinx.android.synthetic.main.register_layout.*

/**
 * Created by Nik on 9/5/2017.
 * TODO write description of this class - what it does and why.
 */

class EditProfileActivity : RegisterActivity() {

    private val TAG = "profile"

    companion object {
        // Arguments
        private const val USER = "user"

        @JvmStatic
        fun getStartIntent(context: Context, user: User): Intent {
            return Intent(context, EditProfileActivity::class.java)
                    .putExtra(USER, user)
        }
    }

    private val currentUser: User by lazy {
        intent.getSerializableExtra(USER) as User
    }

    override val userId: String by lazy {
        currentUser.id
    }

    override val userProfilePicUrl: String by lazy {
        currentUser.profile_picture_url
    }

    init { TimeManager.refreshGlobalTimeStamp() }

    override fun initLayout() {
        setContentView(R.layout.register_layout)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        titleTextView.setText(R.string.edit_profile)

        cancelButton.visibility = View.VISIBLE
        cancelButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun fillFields() {
        fnameEditText.setText(currentUser.fname)
        lnameEditText.setText(currentUser.lname)
        emailEditText.setText(currentUser.email)
        emailEditText.isEnabled = false
        emailEditText.isFocusable = false
        numEditText.setText(currentUser.tel_num)
        birthDateTextView.text = DateUtil.putSlashesInDate(currentUser.birth_date)

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
            updateAuthEmail() // Updates email and all other fields
            // This makes MainAct refresh navbar user profile views
            General.accountStatus = General.JUST_LOGGED_IN
        } else {
            finish()
        }
    }

    // Updates auth email if user is not signed in with facebook; then uploads user data
    private fun updateAuthEmail() {
        if (General.isValidEmail(emailEditText.text)) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                user.updateEmail(emailEditText.text.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User email address updated.")

                                super.onDoneButtonClick()
                            } else {
                                Toast.makeText(this, R.string.error_email_taken, Toast.LENGTH_SHORT)
                                        .show()
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

    // Overwrites all textual data in Firebase (instead of uploading a new user)
    override fun addUserEntryToDataBase(user: UploadUser) {
        val ref = FirebaseUtil.getCurrentUserRef()

        // Setting values
        ref.child(FirebaseUtil.F_FNAME).setValue(user.fname)
        ref.child(FirebaseUtil.F_LNAME).setValue(user.lname)
        ref.child(FirebaseUtil.F_PROFILE_PIC_URL).setValue(user.profile_picture_url)
        ref.child(FirebaseUtil.F_BIRTH_DATE).setValue(user.birth_date)
        ref.child(FirebaseUtil.F_TEL_NUM).setValue(user.tel_num)
        ref.child(FirebaseUtil.F_EMAIL).setValue(user.email)

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun fieldsChanged(): Boolean {
        if (fnameEditText.str() != currentUser.fname) {
            return true
        }
        if (lnameEditText.str() != currentUser.lname) {
            return true
        }
        if (emailEditText.str() != currentUser.email) {
            return true
        }
        if (birthDateTextView.text.toString() != DateUtil.putSlashesInDate(currentUser.birth_date)) {
            return true
        }
        if (numEditText.str() != currentUser.tel_num) {
            return true
        }
        if (imagePath != null) {
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
    }
}