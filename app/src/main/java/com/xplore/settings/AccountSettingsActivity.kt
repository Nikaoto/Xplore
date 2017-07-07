package com.xplore.settings

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.xplore.ApiManager
import com.xplore.General

/**
 * Created by Nika on 7/7/2017.
 * TODO write description of this class - what it does and why.
 */

class AccountSettingsActivity : Activity() {

    private val googleApiClient: GoogleApiClient
            by lazy { ApiManager.getGoogleAuthApiClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.account_settings)
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    //TODO add Account field to Settings and LogOut() from there
    fun logOut() {
        if(General.accountStatus == General.LOGGED_IN){
            FirebaseAuth.getInstance().signOut()

            //TODO find which service was used to sign in and sign out accordingly
            //If (signed in with Google)
            Auth.GoogleSignInApi.signOut(googleApiClient)
            //else if (signed in with Facebook) ...

            General.currentUserId = ""
            General.accountStatus = General.NOT_LOGGED_IN
            Toast.makeText(applicationContext, "Logged Out", Toast.LENGTH_SHORT).show() //TODO string resources
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }
}