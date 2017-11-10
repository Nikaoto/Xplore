package com.xplore.account.registration

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.base.BaseAct
import kotlinx.android.synthetic.main.register_layout.*

/**
 * Created by Nika on 11/10/2017.
 * TODO write description of this class - what it does and why.
 */

class RegistrationActivity : BaseAct<RegistrationContract.View, RegistrationContract.Presenter>(),
        RegistrationContract.View {

    override var presenter: RegistrationContract.Presenter = RegistrationPresenter()

    companion object {
        const val ARG_USER_ID = "userId"
        const val ARG_FULL_NAME = "fullName"
        const val ARG_EMAIL = "email"
        const val ARG_PHOTO_URL = "photoUrl"

        // TODO pass object to this and deserialize it later
        @JvmStatic
        fun newIntent(context: Context, userId: String, fullName: String?, email: String?,
                      photoUrl: Uri?): Intent {
            return Intent(context, RegistrationActivity::class.java)
                    .putExtra(ARG_FULL_NAME, fullName)
                    .putExtra(ARG_USER_ID, userId)
                    .putExtra(ARG_EMAIL, email)
                    .putExtra(ARG_PHOTO_URL, photoUrl.toString())
        }
    }

    private val userFullName: String by lazy {
        intent.getStringExtra(ARG_FULL_NAME)
    }

    private val userId: String by lazy {
        intent.getStringExtra(ARG_USER_ID)
    }

    private val userEmail: String by lazy {
        intent.getStringExtra(ARG_EMAIL)
    }

    private val userPhotoUrl: String by lazy {
        intent.getStringExtra(ARG_PHOTO_URL)
    }

    override var mobileNumberMessageShown: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.activity_register_title)
        setContentView(R.layout.register_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fillFields()
    }

    private fun TextView.safeSetText(s: String?) {
        if (s != null) {
            this.text = s
        } else {
            this.text = ""
        }
    }

    override fun fillFields() {
        val names = presenter.separateFullName(userFullName)

        fnameEditText.safeSetText(names[0])
        lnameEditText.safeSetText(names[1])
        emailEditText.safeSetText(userEmail)

        emailEditText.isEnabled = userEmail.isEmpty()
        emailEditText.isFocusable = userEmail.isEmpty()
    }

    override fun initClickEvents() {
        //initProfileImage(userProfilePicUrl)

        birthDateTextView.setOnClickListener {
            onBirthDateSelected()
        }

        numEditText.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                showMobileNumberReason()
            }
            false
        }

        doneButton.setOnClickListener {
            onDoneButtonClick()
        }
    }

    override fun showMobileNumberReason() {
        if (!mobileNumberMessageShown) {
            mobileNumberMessageShown = true

            AlertDialog.Builder(this)
                    .setTitle(R.string.mobile_number_reason_title)
                    .setMessage(R.string.mobile_number_reason_message)
                    .setPositiveButton(R.string.okay, null)
                    .create().show()
        }
    }

    override fun highlightBorder(v: View) = v.setBackgroundResource(R.drawable.edit_text_border_red)

    override fun unHighlightBorder(v: View) = v.setBackgroundResource(R.drawable.edit_text_border)

    override fun unHighlightAllEditTexts() {
        unHighlightBorder(fnameEditText)
        unHighlightBorder(lnameEditText)
        unHighlightBorder(emailEditText)
        unHighlightBorder(numEditText)
        unHighlightBorder(birthDateTextView)
    }

    override fun onBirthDateSelected() {
        
    }

    override fun onDoneButtonClick() {

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}