package com.xplore

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast

import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

import java.util.HashMap

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import com.xplore.TimeManager.Companion.globalTimeStamp
import com.xplore.TimeManager.Companion.refreshGlobalTimeStamp
import kotlinx.android.synthetic.main.register_layout.*

/**
* Created by Nikaoto on 3/11/2017.
* TODO write description of this class - what it does and why.
*/

class RegisterActivity : Activity(), DatePickerDialog.OnDateSetListener {

    //TODO add age restriction constant to resources
    private val ageRestriction: Int = 16

    private var bYear: Int = 0
    private var bMonth: Int = 0
    private var bDay: Int = 0

    init {
        refreshGlobalTimeStamp()
    }

    internal val DBref = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)

        //Getting User info from SignInActivity
        val userId = intent.getStringExtra("userId")
        val userFullName = intent.getStringExtra("fullName")
        val userEmail = intent.getStringExtra("email")
        val userProfilePicUrl = intent.getStringExtra("photoUrl")

        //Loading data into views
        fnameEditText.setText(separateFullName(userFullName, 0))
        lnameEditText.setText(separateFullName(userFullName, 1))
        emailEditText.setText(userEmail)

        //Birth date selector
        bdateTextView.setOnClickListener {
            //Creating new DialogFragment
            val fragment = com.xplore.DatePickerDialogFragment(this, globalTimeStamp, ageRestriction)
            fragment.show(fragmentManager, "datePicker")
        }
        //TODO CHOOSE GALLERY IMAGE OR TAKE PHOTO
        //TODO AI check for face in photo?
        if(userProfilePicUrl != "") {
            Picasso.with(this@RegisterActivity)
                    .load(userProfilePicUrl)
                    .transform(RoundedCornersTransformation(
                            resources.getInteger(R.integer.pic_big_angle),
                            resources.getInteger(R.integer.pic_big_margin)))
                    .into(profileImageView)
        }

        doneButton.setOnClickListener {
            if (checkFields()) {
                //TODO upload image to db if changed
                addUserEntryToDataBase(
                        UploadUser(userId, fnameEditText.str(), lnameEditText.str(),
                                numEditText.str(), userEmail,
                                General.getDateLong(bYear, bMonth, bDay), userProfilePicUrl)
                )
            }
        }
    }

    //Adds zero to Day or Month number if needed
    fun addZero(num: Int): String {
        if(num < 10){
            return "0$num"
        }
        else
            return "$num"
    }

    override fun onDateSet(datePicker: DatePicker, year: Int, month: Int, day: Int) {
        if (General.isNetConnected(this@RegisterActivity)) {

            //Checking if age is OK
            if (General.calculateAge(globalTimeStamp, year, month, day) >= ageRestriction) {
                bYear = year
                bMonth = month
                bDay = day

                bdateTextView.text = "$bYear/${addZero(bMonth)}/${addZero(bDay)}"
            } else
                Toast.makeText(this@RegisterActivity,
                        "You must be of age $ageRestriction to use Xplore",
                        Toast.LENGTH_SHORT).show() //TODO string resources
        } else
            General.createNetErrorDialog(this@RegisterActivity)
    }

    private fun addUserEntryToDataBase(user: UploadUser) {
        val userData = user.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates.put("/users/" + user.uid, userData)
        Log.println(Log.INFO, "BREJK", "uid: " + user.uid)
        DBref.updateChildren(childUpdates)
        setResult(Activity.RESULT_OK)
        finish()
    }

    //TODO change init after converting User class to kotlin
    private inner class UploadUser(val uid: String, fname: String, lname: String, tel_num: String,
                                   email: String, birth_date: Int,
                                   profile_picture_url: String? = "") : User() {

        init {
            this.id = uid
            this.fname = fname
            this.lname = lname
            this.tel_num = tel_num;
            this.email = email
            this.birth_date = birth_date

            //Setting profile picture URL
            if (profile_picture_url != null) {
                this.profile_picture_url = profile_picture_url
            }

            //Every new user starts with 0 reputation
            this.reputation = 0
        }

        @Exclude
        fun toMap(): Map<String, Any> {
            val result = HashMap<String, Any>()
            result.put("fname", this.fname)
            result.put("lname", this.lname)
            result.put("profile_picture_url", this.profile_picture_url)
            result.put("birth_date", this.birth_date)
            result.put("tel_num", this.tel_num)
            result.put("reputation", this.reputation)
            result.put("email", this.email)
            return result
        }
    }

    private fun checkFields(): Boolean {
        //TODO check fields
        return true
    }

    //Splits a full name and returns the i-th part of it. Returns fullName back if it has no space
    private fun separateFullName(fullName: String, i: Int): String {
        var name = arrayOf<String>(fullName,"")
        if (fullName.contains(" "))
            name = fullName.split(" ".toRegex(), 2).toTypedArray()

        return name[i]
    }

    fun EditText.str() = this.text.toString() //TODO take this to general
}
