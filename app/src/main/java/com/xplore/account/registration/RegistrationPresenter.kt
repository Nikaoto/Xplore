package com.xplore.account.registration

import com.xplore.General
import com.xplore.TimeManager
import com.xplore.base.BasePresenterImpl
import com.xplore.util.DateUtil
import com.xplore.util.FirebaseUtil
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

    override fun onBirthDateClicked() {
        // TODO open guga's datepicker
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

    override fun checkBirthDateValid(birthDate: String): Boolean {
        val year = DateUtil.getYear(birthDate)
        val month = DateUtil.getMonth(birthDate)
        val day = DateUtil.getDay(birthDate)

        if (DateUtil.calculateAge(year, month, day) >= MIN_AGE) {
            val displayBirthDate = DateUtil.putSlashesInDate(birthDate)
            view?.fillBirthDateField(displayBirthDate)
            return true
        } else {
            view?.displayBirthDateRestrictionError(MIN_AGE)
            return false
        }
    }

    override fun isValidEmail(email: String): Boolean {
        // TODO check here?
        return General.isValidEmail(email)
    }

    override fun submitUserData(firstName: String, lastName: String, email: String,
                                mobileNumber: String, birthDate: Int) {
        // TODO change userPhotoUrl
        //val tempUser = User(userId, firstName, lastName, mobileNumber, email, userPhotoUrl, 0, birthDate)
    }
}