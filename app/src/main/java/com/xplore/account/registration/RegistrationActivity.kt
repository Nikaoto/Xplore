package com.xplore.account.registration

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.xplore.BirthDatePickerFragment
import com.xplore.R
import com.xplore.base.BaseAct
import com.xplore.util.FirebaseUtil
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
    private lateinit var startingPhotoUrl: String
    private lateinit var newPhotoUrl: String
    private var birthDate: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up layout
        setTitle(R.string.activity_register_title)
        setContentView(R.layout.register_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Retrieve passed data
        userId = intent.getStringExtra(ARG_USER_ID)
        startingPhotoUrl = intent.getStringExtra(ARG_PHOTO_URL)
        newPhotoUrl = startingPhotoUrl
        val userFullName = intent.getStringExtra(ARG_FULL_NAME)
        val userEmail = intent.getStringExtra(ARG_EMAIL)

        // Display data
        val names = presenter.separateFullName(userFullName)
        fillUserInfo(names[0], names[1], userEmail)
        initProfilePhoto(startingPhotoUrl)

        initClickEvents()
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
            onBirthDateSelected()
        }

        mobileNumberEditText.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                presenter.onMobileNumberTouched()
            }
            false
        }

        doneButton.setOnClickListener {
            if (fieldsValid()) {
                presenter.submitUserData(fnameEditText.str(), lnameEditText.str(), emailEditText.str(),
                        mobileNumberEditText.str(), birthDate, newPhotoUrl)
            }
        }
    }

    override fun onBirthDateSelected() {
        BirthDatePickerFragment({ dp, y, m, d -> presenter.onBirthDateSet(y, m, d) }, -FirebaseUtil.MIN_AGE)
                .show(fragmentManager, "dp")
    }

    override fun fillBirthDateField(birthDateInt: Int, birthDate: String) {
        this.birthDate = birthDateInt
        birthDateTextView.text = birthDate
    }

    override fun fieldsValid(): Boolean {
        unHighlightAllFields()

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
        if (birthDateTextView.isEmpty() || birthDate == 0) {
            return fieldError(birthDateTextView)
        }

        return true
    }

    override fun profilePicChanged() = newPhotoUrl != startingPhotoUrl || newPhotoUrl.isEmpty()

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finishCancelled()
    }

    override fun finishOk() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun finishCancelled() {
        // TODO Log user out
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    /* Messages and Errors */
    override fun showNetError() {
        AlertDialog.Builder(this)
                .setMessage(R.string.wifi_connect_dialog)
                .setTitle(R.string.unable_to_connect)
                .setCancelable(false)
                .setPositiveButton(R.string.action_settings, {_,_ ->
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))})
                .setNegativeButton(R.string.cancel, null)
                .create().show()
    }

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

    override fun showBirthDateRestrictionError(ageLimit: Int) {
        showLongMessage(resources.getString(R.string.you_must_be_at_least) + " " + ageLimit + " " + resources.getString(R.string.years_to_use_xplore))
    }

    override fun showProfilePicUploadError() {
        showMessage(R.string.fail_profile_picture_upload)
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

    private fun TextView.isEmpty() = this.text.trim().isEmpty()

    private fun TextView.safeSetText(s: String?) {
        if (s != null) {
            this.text = s
        } else {
            this.text = ""
        }
    }

}