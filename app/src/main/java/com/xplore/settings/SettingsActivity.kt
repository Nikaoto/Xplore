package com.xplore.settings

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.xplore.ApiManager
import com.xplore.General

import com.xplore.R
import com.xplore.MainActivity

class SettingsActivity : AppCompatPreferenceActivity() {

    val googleApiClient: GoogleApiClient
            by lazy { ApiManager.getGoogleAuthApiClient(this) }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.settings_title)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, MainPreferenceFragment(googleApiClient)).commit()
    }

    override fun onResume() {
        super.onResume()

        if (MainActivity.languagePrefsChanged) {
            finish()
        }
    }

    class MainPreferenceFragment() : PreferenceFragment() {

        private var googleApiClient: GoogleApiClient? = null

        constructor(authClient: GoogleApiClient) : this() {
            this.googleApiClient = authClient
        }

        fun logOut() {
            if(General.accountStatus == General.LOGGED_IN){
                FirebaseAuth.getInstance().signOut()

                //TODO find which service was used to sign in and sign out accordingly
                //If (signed in with Google)
                Auth.GoogleSignInApi.signOut(googleApiClient)
                //else if (signed in with Facebook) ...

                General.currentUserId = ""
                General.accountStatus = General.NOT_LOGGED_IN
                Toast.makeText(activity.applicationContext, "Logged Out", Toast.LENGTH_SHORT).show() //TODO string resources
                activity.finish()
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            /*  */
            addPreferencesFromResource(R.xml.prefs_main)

            /* Click Listeners for Log out , tutorial etc , Language  ... */
            val selectLanguage = findPreference("select_language")
            findPreference("logout").setOnPreferenceClickListener {
                logOut()
                true
            }

            selectLanguage.setOnPreferenceClickListener({
                startActivity(Intent(activity, LanguageSettingsActivity::class.java))
                true
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return true
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }
}
