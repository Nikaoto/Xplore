package com.xplore.account.registration

import android.view.View
import com.xplore.base.BasePresenter
import com.xplore.base.BaseView
import com.xplore.user.User

/**
 * Created by Nika on 11/10/2017.
 * TODO write description of this class - what it does and why.
 */

interface RegistrationContract {

    interface View : BaseView {

        // View Getters/Setters

        fun setFnameText(text: String)
        fun getFnameText(): String

        fun setLnameText(text: String)
        fun getLnameText(): String

        fun setEmailText(text: String)
        fun getEmailText(): String

        fun setMobileNumberText(text: String)
        fun getMobileNumberText(): String

        fun setBirthDateText(text: String)
        fun getBirthDateText(): String

        // End of View Getters/Setters


        fun initProfilePhoto(photoUrl: String)

        fun initClickEvents()

        fun highlightBorder(v: android.view.View)

        fun unHighlightBorder(v: android.view.View)

        fun unHighlightAllEditTexts()

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

        fun fillFields()

        var mobileNumberReasonShown: Boolean

        fun onBirthDateClicked()

        fun onMobileNumberTouched()

        fun onDoneButtonClicked()

        fun separateFullName(fullName: String?): Array<String>

        fun isValidAge(age: Int): Boolean

        fun fieldsValid(): Boolean

        fun submitUserData(user: User)
    }
}