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

        fun fieldsValid(): Boolean
    }

    interface Presenter : BasePresenter<View> {

        fun onCreate()

        var mobileNumberReasonShown: Boolean

        fun onBirthDateClicked()

        fun onMobileNumberTouched()

        fun separateFullName(fullName: String?): Array<String>

        fun isValidAge(age: Int): Boolean

        fun submitUserData(firstName: String, lastName: String, email: String, mobileNumber: String,
                           birthDate: Int)
    }
}