package com.xplore.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.MenuItem

import com.xplore.R
import android.preference.Preference
import com.xplore.MainActivity


class SettingsActivity
    : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        fragmentManager.beginTransaction().replace(android.R.id.content, MainPreferenceFragment()).commit()
    }

    override fun onResume() {
        super.onResume()

        if (MainActivity.languagePrefsChanged) {
            finish()
        }
    }

    class MainPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            /*  */
            addPreferencesFromResource(R.xml.prefs_main)

            /* Click Listeners for Log out , tutorial etc , Language  ... */
            val settings_button = findPreference("select_language")
            settings_button.setOnPreferenceClickListener({
                startActivity(Intent(activity, LanguageSettingsActivity::class.java))
                true
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return true
    }

}
