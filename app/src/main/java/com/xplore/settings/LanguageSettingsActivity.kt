package com.xplore.settings

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.view.MenuItem
import com.xplore.MainActivity
import com.xplore.R
import java.util.*

class LanguageSettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        fragmentManager.beginTransaction().replace(android.R.id.content, LanguagePreferenceFragment()).commit()
    }


    class LanguagePreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            /*  */
            addPreferencesFromResource(R.xml.prefs_language)

            /* click listener , TODO implement OnPreferenceClickLisneter and handle logic with switch */
            val preferenceGeorgian = findPreference("")
            val preferenceEnglish  = findPreference("")
            val preferenceRussian = findPreference("")

            preferenceGeorgian.setOnPreferenceClickListener({

                true
            })

            preferenceEnglish.setOnPreferenceClickListener({

                true
            })

            preferenceRussian.setOnPreferenceClickListener({

                true
            })
        }

        fun ChangeLocale(language_code: String) {
            val preferences = activity.getSharedPreferences("lang", 0)
            val prefEditor = preferences.edit()

            val res = activity.resources
            val dm = res.displayMetrics

            val config = res.configuration
            prefEditor.putString("lang", language_code)
            prefEditor.commit()

            val locale = Locale(preferences.getString("lang", MainActivity.ENGLISH_LANG_CODE))
            Locale.setDefault(locale)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
               // setSystemLocale(config, locale)
            } else {
              //  setSystemLocaleLegacy(config, locale, res, dm)
            }

            //DisableChosenLanguageButton()
            activity.recreate()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}
