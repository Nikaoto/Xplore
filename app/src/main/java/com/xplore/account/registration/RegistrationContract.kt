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

        fun fillFields()

        fun initClickEvents()

        var mobileNumberMessageShown: Boolean

        fun highlightBorder(v: android.view.View)

        fun unHighlightBorder(v: android.view.View)

        fun unHighlightAllEditTexts()

        fun showMobileNumberReason()

        fun scrollToView(v: android.view.View)

        fun fieldError(v: android.view.View, msgResId: Int): Boolean

        fun fieldError(v: android.view.View): Boolean

        fun onBirthDateSelected()

        fun onDoneButtonClick()

        fun fieldsValid(): Boolean
    }

    interface Presenter : BasePresenter<View> {

        fun separateFullName(fullName: String?): Array<String>

        fun isValidAge(age: Int): Boolean

        fun isValidDate(date: String): Boolean

        fun isValidEmail(email: String): Boolean

        fun submitFields(user: User)
    }
}