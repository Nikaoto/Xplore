package com.xplore.util

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.xplore.General
import com.xplore.R

/**
 * Created by Nika on 9/7/2017.
 *
 * A singleton class meant for utility functions with Firebase.
 * The "F_" prefix means it's the name of a Firebase node.
 *
 */

object FirebaseUtil {

    private const val TAG = "firebase-util"

    const val REP = 1 // Starting reputation
    // URL of default image to assign if no image uploaded upon registration
    const val DEFAULT_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/xplore-a4aa3.appspot.com/o/user_default_profile_image.jpg?alt=media&token=9ef3891f-4525-414d-8039-061cdc65654e"

    // TODO put these somewhere else
    const val FB_PROFILE_PIC_HEIGHT = 300
    const val FB_PROFILE_PIC_WIDTH = 300
    const val MIN_AGE = 15
    const val MIN_PASSWORD_LENGTH = 6

    // Firebase Storage
    const val FS_PROFILE_PIC_KB_LIMIT = 25
    const val FS_PROFILE_PIC_NAME_PREFIX = "profile_picture"
    const val FS_PROFILE_PIC_EXTENSION = ".jpg"
    const val FS_PROFILE_PIC_FULL_NAME = "$FS_PROFILE_PIC_NAME_PREFIX$FS_PROFILE_PIC_EXTENSION"
    const val FS_USERS = "users"

    // Main Nodes
    const val F_date = "date"
    const val F_USERS = "users"
    const val F_GROUPS = "groups"

    // Date nodes
    const val F_TIMESTAMP = "timestamp"

    // User nodes
    const val F_FNAME = "fname"
    const val F_LNAME = "lname"
    const val F_TEL_NUM = "tel_num"
    const val F_EMAIL = "email"
    const val F_BIRTH_DATE = "birth_date"
    const val F_REPUTATION = "reputation"
    const val F_GROUP_IDS = "group_ids"
    const val F_INVITED_GROUP_IDS = "invited_group_ids"
    const val F_PROFILE_PIC_URL = "profile_picture_url"

    // Group nodes
    const val F_GROUP_NAME = "name"
    const val F_DESTINATION_ID = "destination_id"
    const val F_START_DATE = "start_date"
    const val F_END_DATE = "end_date"
        // Locations nodes
        const val F_LOCATIONS = "locations"
        const val F_LATITUDE = "latitude"
        const val F_LONGITUDE = "longitude"
    const val F_GRANTED_REPUTATION = "granted_reputation"
    const val F_MEMBER_IDS = "member_ids"
    const val F_INVITED_MEMBER_IDS = "invited_member_ids"

    @JvmStatic
    fun getRef(s: String) = FirebaseDatabase.getInstance().getReference(s)

    @JvmField
    val usersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(F_USERS)

    @JvmField
    val groupsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(F_GROUPS)

    @JvmStatic
    fun getGroupRef(groupId: String): DatabaseReference = groupsRef.child(groupId)

    @JvmStatic
    fun getUserRef(userId: String): DatabaseReference = usersRef.child(userId)

    @JvmStatic
    fun getCurrentUserRef() = getUserRef(General.currentUserId)

    @JvmStatic
    fun getUserLocationRefString(groupId: String, userId: String) = "/$F_GROUPS/$groupId/$F_LOCATIONS/$userId/"

    // Gets 'invited group ids' node reference for given user
    @JvmStatic fun getInvitedGroupIdsRef(userId: String): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
                .child(F_USERS)
                .child(userId)
                .child(F_INVITED_GROUP_IDS)
    }

    @JvmStatic
    private fun uploadData(parentId: String, data: Map<String, Any>, ref: DatabaseReference) {
        val update = HashMap<String, Any>()
        update.put(parentId, data)
        ref.updateChildren(update)
    }

    @JvmStatic
    fun uploadUserData(userId: String, userData: Map<String, Any>) {
        uploadData(userId, userData, usersRef)
    }

    @JvmStatic
    fun grantReputation(userId: String, reputationAmount: Int) {
        if (reputationAmount != 0) {
            val repRef = getUserRef(userId).child(F_REPUTATION)
            repRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if (dataSnapshot != null) {
                        val currentRep = dataSnapshot.getValue(Int::class.java)
                        if (currentRep != null) {
                            repRef.setValue(reputationAmount + currentRep)
                        } else {
                            Log.i(TAG, "grantReputation($userId,$reputationAmount): currentRep null")
                        }
                    } else {
                        Log.i(TAG, "grantReputation($userId,$reputationAmount): dataSnapshot null")
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }

    @JvmStatic
    fun logOut(act: Activity, googleApiClient: GoogleApiClient) {
        if(General.accountStatus == General.LOGGED_IN){
            forceLogOut(act, googleApiClient)
        }
    }

    @JvmStatic
    fun forceLogOut(act: Activity, googleApiClient: GoogleApiClient) {
        // Firebase log out
        FirebaseAuth.getInstance().signOut()

        // Facebook log out
        LoginManager.getInstance().logOut()

        // Google log out
        Auth.GoogleSignInApi.signOut(googleApiClient)

        // Reset current user
        General.currentUserId = ""
        General.accountStatus = General.NOT_LOGGED_IN
        General.setRegistrationFinished(act, false)
        Toast.makeText(act, R.string.logged_out, Toast.LENGTH_SHORT).show()
    }


    /*
    * Firebase Storage
    */

    @JvmField
    val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    @JvmStatic
    fun getUserStorageRef(userId: String) = storageRef.child("$FS_USERS/$userId")

    @JvmStatic
    fun getUserProfilePicRef(userId: String) = getUserStorageRef(userId).child(FS_PROFILE_PIC_FULL_NAME)

    @JvmStatic
    fun uploadProfilePic(userId: String, input: String,
                         onSuccess: (taskSnapshot: UploadTask.TaskSnapshot) -> Unit,
                         onFail: () -> Unit) {
        getUserProfilePicRef(userId).putFile(Uri.parse(input))
                .addOnSuccessListener { taskSnapshot -> onSuccess(taskSnapshot) }
                .addOnFailureListener { onFail() }
    }


    /*
    * Stuff for events
    *
    * DO NOT DELETE, MIGHT USE THIS FOR FUTURE EVENTS
    *
    */
    const val F_CHECKINS = "checkins"
    const val F_STANDS = "stands"
    const val F_WINNERS = "winners"
    @JvmField val standsRef = FirebaseDatabase.getInstance().getReference(F_STANDS)

    @JvmStatic
    fun getStandRef(standId: String): DatabaseReference = standsRef.child(standId)

    @JvmStatic
    fun checkIn(standId: String) {
        getStandRef(standId).child(F_CHECKINS).child(General.currentUserId).setValue(true)
    }

    @JvmStatic
    fun uploadWinnerId(userId: String) {
        standsRef.child(F_WINNERS).child(userId).setValue(true)
    }
    //
}