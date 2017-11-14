package com.xplore.account.registration

import android.util.Log
import com.google.gson.Gson
import com.xplore.General
import com.xplore.base.BasePresenterImpl
import com.xplore.user.UploadUser
import com.xplore.util.DateUtil
import com.xplore.util.FirebaseUtil.MIN_AGE

/**
 * Created by Nika on 11/10/2017.
 * TODO write description of this class - what it does and why.
 */

class RegistrationPresenter : BasePresenterImpl<RegistrationContract.View>(),
        RegistrationContract.Presenter {

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
                val displayBirthDate = DateUtil.formatDate(year, month, day)
                view?.fillBirthDateField(displayBirthDate)
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
                                mobileNumber: String, birthDate: Int) {
        view?.showMessage("uploading user; check log.i for info")
        val tempuser = UploadUser(General.currentUserId, firstName, lastName, mobileNumber, email, "asd", 0, birthDate)

        Log.i("regact - user:", Gson().toJson(tempuser.toMap()))
        // TODO change userPhotoUrl
        //val tempUser = User(userId, firstName, lastName, mobileNumber, email, userPhotoUrl, 0, birthDate)
    }
}