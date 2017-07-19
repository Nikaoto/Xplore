package com.xplore.settings

import android.os.Build
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.MenuItem
import com.xplore.MainActivity
import com.xplore.R
import java.util.*

class LanguageSettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.settings_language_title)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, LanguagePreferenceFragment()).commit()
    }


    class LanguagePreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            //TODO create a singleton PreferenceManager and store these two in there
            val prefs = activity.getSharedPreferences("lang", 0)
            val currentLanguage = prefs.getString("lang", "null")

            /*  */
            addPreferencesFromResource(R.xml.prefs_language)



            /* click listener , TODO implement OnPreferenceClickLisneter and handle logic with switch */
            val preferenceGeorgian = findPreference("language_georgian")
            val preferenceEnglish  = findPreference("language_english")
            val preferenceRussian = findPreference("language_russian")

            preferenceGeorgian.setOnPreferenceClickListener({
                onPrefClick(MainActivity.GEORGIAN_LANG_CODE, currentLanguage)
                true
            })

            preferenceEnglish.setOnPreferenceClickListener({
                onPrefClick(MainActivity.ENGLISH_LANG_CODE, currentLanguage)
                true
            })

            preferenceRussian.setOnPreferenceClickListener({
                onPrefClick(MainActivity.RUSSIAN_LANG_CODE, currentLanguage)
                true
            })
        }

        fun onPrefClick(languageCode: String, currentLanguage: String) {
            if (currentLanguage != languageCode) {
                changeLocale(languageCode)
            }
        }

        fun changeLocale(language_code: String) {
            val preferences = activity.getSharedPreferences("lang", 0)
            val prefEditor = preferences.edit()

            val res = activity.resources
            val config = res.configuration

            prefEditor.putString("lang", language_code)
            prefEditor.commit()

            val locale = Locale(preferences.getString("lang", MainActivity.ENGLISH_LANG_CODE))
            Locale.setDefault(locale)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
            } else {
                //Meant for lower-end devices
                @Suppress("DEPRECATION")
                config.locale = locale
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, res.displayMetrics)
            }

            MainActivity.languagePrefsChanged = true
            activity.finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return true
    }
}
