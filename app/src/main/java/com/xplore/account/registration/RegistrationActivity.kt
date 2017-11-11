package com.xplore.account.registration

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.xplore.General
import com.xplore.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.activity_register_title)
        setContentView(R.layout.register_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        presenter.onCreate()
        // TODO remove this when interactor is added
        emailEditText.isEnabled = false
        emailEditText.isFocusable = false
    }

    private fun TextView.safeSetText(s: String?) {
        if (s != null) {
            this.text = s
        } else {
            this.text = ""
        }
    }

    override fun initProfilePhoto(photoUrl: String) {

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
            presenter.onDoneButtonClicked()
        }
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

    override fun highlightBorder(v: View) = v.setBackgroundResource(R.drawable.edit_text_border_red)

    override fun unHighlightBorder(v: View) = v.setBackgroundResource(R.drawable.edit_text_border)

    override fun unHighlightAllEditTexts() {
        unHighlightBorder(fnameEditText)
        unHighlightBorder(lnameEditText)
        unHighlightBorder(emailEditText)
        unHighlightBorder(mobileNumberEditText)
        unHighlightBorder(birthDateTextView)
    }

    override fun fieldError(v: View, msgResId: Int): Boolean {
        highlightBorder(v)
        scrollToView(v)

        showMessage(msgResId)
        return false
    }

    override fun fieldError(v: View): Boolean = fieldError(v, R.string.error_field_required)

    override fun scrollToView(v: View) {
        scrollView.post {
            scrollView.smoothScrollTo(0, v.bottom)
        }
    }

    override fun onBirthDateSelected() {
        // TODO add date picker by Guga
    }

    private fun TextView.str() = this.text.trim().toString()
    private fun TextView.isEmpty() = this.text.isEmpty()

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
        if (!General.isValidEmail(emailEditText.str())) {
            return fieldError(emailEditText, R.string.error_invalid_email)
        }
        if (mobileNumberEditText.isEmpty()) {
            return fieldError(mobileNumberEditText)
        }
        if (birthDateTextView.isEmpty()) {
            return fieldError(birthDateTextView)
        }
        // TODO check birth dates != 0

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Log user out
    }

    // View Getters/Setters

    override fun setFnameText(text: String) = fnameEditText.setText(text)
    override fun getFnameText() = fnameEditText.text.toString()

    override fun setLnameText(text: String) = lnameEditText.setText(text)
    override fun getLnameText() = lnameEditText.text.toString()

    override fun setEmailText(text: String) = emailEditText.setText(text)
    override fun getEmailText() = emailEditText.text.toString()

    override fun setMobileNumberText(text: String) = mobileNumberEditText.setText(text)
    override fun getMobileNumberText() = mobileNumberEditText.text.toString()

    override fun setBirthDateText(text: String) = birthDateTextView.setText(text)
    override fun getBirthDateText() = birthDateTextView.text.toString()

    // End of view Getters/Setters
}