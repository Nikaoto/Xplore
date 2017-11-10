package com.xplore.account.registration

import android.view.View
import com.xplore.base.BasePresenter
import com.xplore.base.BaseView

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

        fun onBirthDateSelected()

        fun onDoneButtonClick()
    }

    interface Presenter : BasePresenter<View> {

        fun separateFullName(fullName: String?): Array<String>

        fun checkAge(age: Int): Boolean

        fun checkFieldsValid(): Boolean

    }
}