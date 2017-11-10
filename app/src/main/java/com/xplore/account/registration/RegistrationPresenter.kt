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

    override fun isValidEmail(email: String): Boolean {
        return false
    }

    override fun isValidDate(date: String): Boolean {
        return false
    }

    override fun submitFields(user: User) {

    }
}