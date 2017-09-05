package com.xplore.settings

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.MenuItem
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.xplore.ApiManager
import com.xplore.General
import com.xplore.R
import com.xplore.intro.WelcomeActivity

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

        if (LanguageUtil.languagePrefsChanged) {
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
                // Firebase log out
                FirebaseAuth.getInstance().signOut()

                // Facebook log out
                LoginManager.getInstance().logOut()

                // Google log out
                Auth.GoogleSignInApi.signOut(googleApiClient)

                // Reset current user
                General.currentUserId = ""
                General.accountStatus = General.NOT_LOGGED_IN
                Toast.makeText(activity, R.string.logged_out, Toast.LENGTH_SHORT).show()
                activity.finish()
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            /*  */
            addPreferencesFromResource(R.xml.prefs_main)

            /* Click Listeners for Log out , tutorial etc , Language  ... */
            findPreference("tutorial").setOnPreferenceClickListener {
                startActivity(Intent(activity, WelcomeActivity::class.java))
                true
            }

            findPreference("logout").setOnPreferenceClickListener {
                logOut()
                true
            }

            findPreference("select_language").setOnPreferenceClickListener({
                startActivity(Intent(activity, LanguageSettingsActivity::class.java))
                true
            })

            findPreference("")
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
