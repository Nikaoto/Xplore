package com.xplore.account.registration

import com.xplore.base.BasePresenter
import com.xplore.base.BaseView
import com.xplore.user.User

/**
 * Created by Nika on 11/10/2017.
 * TODO write description of this class - what it does and why.
 */

interface RegistrationContract {

    interface View : BaseView {

        fun fillUserInfo(firstName: String, lastName: String, email: String)

        fun initProfilePhoto(photoUrl: String)

        fun initClickEvents()

        fun highlightField(v: android.view.View)

        fun unHighlightBorder(v: android.view.View)

        fun unHighlightAllFields()

        fun showLoadingMessage()

        fun showMobileNumberReason()

        fun scrollToView(v: android.view.View)

        fun fieldError(v: android.view.View, msgResId: Int): Boolean

        fun fieldError(v: android.view.View): Boolean

        fun onBirthDateSelected()

        fun fillBirthDateField(birthDate: String)

        fun showBirthDateRestrictionError(ageLimit: Int)

        fun showNetError()

        fun fieldsValid(): Boolean
    }

    interface Presenter : BasePresenter<View> {

        fun onCreate(fullName: String, email: String, photoUrl: String)

        var mobileNumberReasonShown: Boolean

        fun onBirthDateSet(year: Int, receivedMonth: Int, day: Int)

        fun onMobileNumberTouched()

        fun separateFullName(fullName: String?): Array<String>

        fun isBirthDateValid(year: Int, month: Int, day: Int): Boolean

        fun isValidEmail(email: String): Boolean

        fun submitUserData(firstName: String, lastName: String, email: String, mobileNumber: String,
                           birthDate: Int)
    }
}