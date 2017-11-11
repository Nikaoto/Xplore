package com.xplore.account.registration

import com.xplore.base.BasePresenterImpl
import com.xplore.user.User
import com.xplore.util.FirebaseUtil

/**
 * Created by Nika on 11/10/2017.
 * TODO write description of this class - what it does and why.
 */

class RegistrationPresenter : BasePresenterImpl<RegistrationContract.View>(),
        RegistrationContract.Presenter {
    private lateinit var userId: String

    private lateinit var userFullName: String
    private lateinit var userEmail: String
    private lateinit var userPhotoUrl: String
    override var mobileNumberReasonShown: Boolean = false

    override fun onCreate() {
        // Retrieving passed data
        view?.returnIntent()?.let {
            userId = it.getStringExtra(RegistrationActivity.ARG_USER_ID)
            userFullName = it.getStringExtra(RegistrationActivity.ARG_FULL_NAME)
            userEmail = it.getStringExtra(RegistrationActivity.ARG_EMAIL)
            userPhotoUrl = it.getStringExtra(RegistrationActivity.ARG_PHOTO_URL)
        }

        fillFields()
    }

    override fun fillFields() {
        val names = separateFullName(userFullName)
        view?.let {
            it.setFnameText(names[0])
            it.setLnameText(names[1])
            it.setEmailText(userEmail)
        }
    }

    override fun onBirthDateClicked() {
        // TODO open guga's datepicker
    }

    override fun onMobileNumberTouched() {
        if (!mobileNumberReasonShown) {
            mobileNumberReasonShown = true
            view?.showMobileNumberReason()
        }
    }

    override fun onDoneButtonClicked() {
        view?.let {
            it.unHighlightAllEditTexts()
            if (fieldsValid()) {
                val tempUser = User(
                        userId,
                        it.getFnameText(),
                        it.getLnameText(),
                        it.getMobileNumberText(),
                        it.getEmailText(),
                        userPhotoUrl
                )

                it.showLoadingMessage()
                submitUserData(tempUser)
            }
        }
    }

    override fun fieldsValid(): Boolean {
        return false
    }

    override fun separateFullName(fullName: String?): Array<String> {
        if (fullName == null) {
            return emptyArray<String>()
        }

        val names = fullName.split(" ".toRegex(), 2).toTypedArray()

        if (names.size == 1) {
            return arrayOf(names[0], "")
        }

        return names
    }

    override fun isValidAge(age: Int): Boolean = age >= FirebaseUtil.MIN_AGE

    override fun submitUserData(user: User) {
        
    }
}