package com.xplore.account.registration

import com.xplore.General
import com.xplore.base.BasePresenterImpl
import com.xplore.user.UploadUser
import com.xplore.util.DateUtil
import com.xplore.util.FirebaseUtil
import com.xplore.util.FirebaseUtil.MIN_AGE
import java.sql.Time

/**
 * Created by Nika on 11/10/2017.
 * TODO write description of this class - what it does and why.
 */

class RegistrationPresenter : BasePresenterImpl<RegistrationContract.View>(),
        RegistrationContract.Presenter {


    val TAG = "reg-act"
    override var mobileNumberReasonShown: Boolean = false

    override fun onCreate(fullName: String, email: String, photoUrl: String) {
        val names = separateFullName(fullName)
        view?.fillUserInfo(names[0], names[1], email)
        view?.initProfilePhoto(photoUrl)
    }

    override fun onBirthDateSet(year: Int, receivedMonth: Int, day: Int) {
        val month = receivedMonth + 1 // Necessary because 0 is January

        if (General.isNetConnected(view?.getContext())) {
            if (isBirthDateValid(year, month, day)) {
                val birthDateInt = General.getDateInt(year, month, day)
                val displayBirthDate = DateUtil.formatDate(year, month, day)

                view?.fillBirthDateField(birthDateInt, displayBirthDate)
            } else {
                view?.showBirthDateRestrictionError(MIN_AGE)
            }
        } else {
            view?.showNetError()
        }
    }

    override fun onMobileNumberTouched() {
        if (!mobileNumberReasonShown) {
            mobileNumberReasonShown = true
            view?.showMobileNumberReason()
        }
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

    override fun isBirthDateValid(year: Int, month: Int, day: Int): Boolean {
        return DateUtil.calculateAge(year, month, day) >= MIN_AGE
    }

    override fun isValidEmail(email: String): Boolean {
        // TODO check here?
        return General.isValidEmail(email)
    }

    override fun submitUserData(firstName: String, lastName: String, email: String,
                                mobileNumber: String, birthDate: Int, photoUri: String?) {
        if (!General.isNetConnected(view?.getContext())) {
            view?.showNetError()
            return
        }

        view?.showLoadingMessage()

        // Create user with default image url
        val tempUser = UploadUser(General.currentUserId, firstName, lastName, mobileNumber, email,
                FirebaseUtil.DEFAULT_IMAGE_URL, 0, birthDate)
        if (photoUri != null && photoUri.isNotEmpty() && photoUri != FirebaseUtil.DEFAULT_IMAGE_URL
                && view?.profilePicChanged()!!) { // TODO fix this !!
            uploadProfilePic(tempUser, photoUri, { user -> addUserEntryToDataBase(user) })
        } else {
            addUserEntryToDataBase(tempUser)
        }
    }

    // Uploads user profile pic and saves new link
    private fun uploadProfilePic(user: UploadUser, input: String,
                                 onFinishUpload: (user: UploadUser) -> Unit) {
        FirebaseUtil.uploadProfilePic(user.id, input,
                { taskSnapshot ->
                    user.profile_picture_url = taskSnapshot.downloadUrl.toString()
                    onFinishUpload(user)},
                { view?.showProfilePicUploadError() }
        )
    }

    // Uploads all textual user data to Firebase
    private fun addUserEntryToDataBase(user: UploadUser) {
        FirebaseUtil.uploadUserData(user.id, user.toMap())
        view?.finishOk()
    }
}