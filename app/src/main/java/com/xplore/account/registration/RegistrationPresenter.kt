package com.xplore.account.registration

import com.xplore.base.BasePresenterImpl

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

    override fun checkAge(age: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun checkFieldsValid(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}