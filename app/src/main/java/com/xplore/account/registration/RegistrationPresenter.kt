package com.xplore.account.registration

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.xplore.General
import com.xplore.base.BasePresenterImpl
import com.xplore.user.UploadUser
import com.xplore.util.DateUtil
import com.xplore.util.FirebaseUtil
import com.xplore.util.FirebaseUtil.MIN_AGE

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
                                mobileNumber: String, birthDate: Int, photoUri: Uri?) {
        view?.showMessage("uploading user; check log.i for info")
        val tempUser = UploadUser(General.currentUserId, firstName, lastName, mobileNumber, email,
                "OLD", 0, birthDate)

        Log.i(TAG, Gson().toJson(tempUser.toMap()))
        // TODO change userPhotoUrl
        //val tempUser = User(userId, firstName, lastName, mobileNumber, email, userPhotoUrl, 0, birthDate)
        if (photoUri != null) {
            uploadProfilePic(tempUser, photoUri, { user -> addUserEntryToDataBase(user) })
        } else {
            addUserEntryToDataBase(tempUser)
        }
    }

    // Uploads user profile pic and saves new link
    private fun uploadProfilePic(user: UploadUser, input: Uri, onFinishUpload: (user: UploadUser) -> Unit) {
        FirebaseUtil.getUserProfilePicRef(user.id).putFile(input)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    // Update download url
                    user.profile_picture_url = taskSnapshot.downloadUrl.toString()

                    onFinishUpload(user)
                }
                .addOnFailureListener {
                    view?.showProfilePicUploadError()
                }
    }

    // Uploads all textual user data to Firebase
    open fun addUserEntryToDataBase(user: UploadUser) {
        val userData = user.toMap()
        //TODO abstract this in FirebaseUtil
        val childUpdates = HashMap<String, Any>()
        childUpdates.put("/users/" + user.id, userData)
        DBref.updateChildren(childUpdates)
        setResult(Activity.RESULT_OK)
        finish()
    }
}