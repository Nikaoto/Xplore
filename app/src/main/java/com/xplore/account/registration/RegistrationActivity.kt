package com.xplore.account.registration

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.R
import com.xplore.base.BaseAct
import com.xplore.user.User
import com.xplore.util.ImageUtil
import kotlinx.android.synthetic.main.register_layout.*

/**
 * Created by Nika on 11/10/2017.
 * TODO write description of this class - what it does and why.
 */

class RegistrationActivity : BaseAct<RegistrationContract.View, RegistrationContract.Presenter>(),
        RegistrationContract.View {

    override var presenter: RegistrationContract.Presenter = RegistrationPresenter()

    companion object {
        // Activity Request Codes
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val GALLERY_PERMISSION_REQUEST_CODE = 2;
        private const val ACTION_SNAP_IMAGE = 3

        // Args
        const val ARG_USER_ID = "userId"
        const val ARG_FULL_NAME = "fullName"
        const val ARG_EMAIL = "email"
        const val ARG_PHOTO_URL = "photoUrl"

        // TODO pass object to this and deserialize it later
        @JvmStatic
        fun newIntent(context: Context, userId: String, fullName: String?, email: String?,
                      photoUrl: String): Intent {
            return Intent(context, RegistrationActivity::class.java)
                    .putExtra(ARG_FULL_NAME, fullName)
                    .putExtra(ARG_USER_ID, userId)
                    .putExtra(ARG_EMAIL, email)
                    .putExtra(ARG_PHOTO_URL, photoUrl)
        }
    }

    private lateinit var userId: String
    private lateinit var userPhotoUrl: String
    private var birthDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up layout
        setTitle(R.string.activity_register_title)
        setContentView(R.layout.register_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Retrieve passed data
        userId = intent.getStringExtra(ARG_USER_ID)
        userPhotoUrl = intent.getStringExtra(ARG_PHOTO_URL)
        val userFullName = intent.getStringExtra(ARG_FULL_NAME)
        val userEmail = intent.getStringExtra(ARG_EMAIL)

        // Display data
        val names = presenter.separateFullName(userFullName)
        fillUserInfo(names[0], names[1], userEmail)
        initProfilePhoto(userPhotoUrl)
    }

    override fun fillUserInfo(firstName: String, lastName: String, email: String) {
        fnameEditText.setText(firstName)
        lnameEditText.setText(lastName)
        emailEditText.setText(email)
        emailEditText.isEnabled = false
        emailEditText.isFocusable = false
    }

    override fun initProfilePhoto(photoUrl: String) {
        Picasso.with(this)
                .load(photoUrl)
                .transform(ImageUtil.mediumCircle(this))
                .into(profileImageView)
    }

    override fun initClickEvents() {
        birthDateTextView.setOnClickListener {
            presenter.onBirthDateClicked()
        }

        mobileNumberEditText.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                presenter.onMobileNumberTouched()
            }
            false
        }

        doneButton.setOnClickListener {
            presenter.submitUserData(fnameEditText.str(), lnameEditText.str(), emailEditText.str(),
                    mobileNumberEditText.str(), 19991030) // TODO birth date here
        }
    }

    override fun onBirthDateSelected() {
        // TODO add date picker by Guga
    }

    override fun fillBirthDateField(birthDate: String) {
        birthDateTextView.text = birthDate
    }

    override fun fieldsValid(): Boolean {
        if (fnameEditText.isEmpty()) {
            return fieldError(fnameEditText)
        }
        if (lnameEditText.isEmpty()) {
            return fieldError(lnameEditText)
        }
        if (emailEditText.isEmpty()) {
            return fieldError(emailEditText)
        }
        if (!presenter.isValidEmail(emailEditText.str())) {
            return fieldError(emailEditText, R.string.error_invalid_email)
        }
        if (mobileNumberEditText.isEmpty()) {
            return fieldError(mobileNumberEditText)
        }
        if (birthDateTextView.isEmpty()) {
            return fieldError(birthDateTextView)
        }

        presenter.checkBirthDateValid(birthDate)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // TODO Log user out
    }


    /* Messages and Errors */

    override fun showLoadingMessage() {
        showLongMessage(R.string.loading)
    }

    override fun showMobileNumberReason() {
        AlertDialog.Builder(this)
                .setTitle(R.string.mobile_number_reason_title)
                .setMessage(R.string.mobile_number_reason_message)
                .setPositiveButton(R.string.okay, null)
                .create().show()
    }

    override fun displayBirthDateRestrictionError(ageLimit: Int) {

    }

    override fun scrollToView(v: View) {
        scrollView.post {
            scrollView.smoothScrollTo(0, v.bottom)
        }
    }

    override fun highlightField(v: View) = v.setBackgroundResource(R.drawable.edit_text_border_red)

    override fun unHighlightBorder(v: View) = v.setBackgroundResource(R.drawable.edit_text_border)

    override fun unHighlightAllFields() {
        unHighlightBorder(fnameEditText)
        unHighlightBorder(lnameEditText)
        unHighlightBorder(emailEditText)
        unHighlightBorder(mobileNumberEditText)
        unHighlightBorder(birthDateTextView)
    }

    override fun fieldError(v: View, msgResId: Int): Boolean {
        highlightField(v)
        scrollToView(v)

        showMessage(msgResId)
        return false
    }

    override fun fieldError(v: View): Boolean = fieldError(v, R.string.error_field_required)


    /* Misc & Extension Functions */

    private fun TextView.str() = this.text.trim().toString()

    private fun TextView.isEmpty() = this.text.isEmpty()

    private fun TextView.safeSetText(s: String?) {
        if (s != null) {
            this.text = s
        } else {
            this.text = ""
        }
    }

}