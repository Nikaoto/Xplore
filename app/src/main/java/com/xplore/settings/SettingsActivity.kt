package com.xplore.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.MenuItem

import com.xplore.R

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSupportActionBar().setDisplayHomeAsUpEnabled(true)

        fragmentManager.beginTransaction().replace(android.R.id.content, MainPreferenceFragment()).commit()
    }

    class MainPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            /*  */
            addPreferencesFromResource(R.xml.prefs_main)

            /* Click Listeners for Log out , tutorial etc ... */

        }

        /* Handling language settings Preferences here */

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }

}
