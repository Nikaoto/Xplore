package com.xplore.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.MenuItem
import com.xplore.R

/**
 * Created by el on 7/18/17.
 */

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
                onPrefClick(LanguageUtil.GEORGIAN_LANGUAGE_CODE, currentLanguage)
                true
            })

            preferenceEnglish.setOnPreferenceClickListener({
                onPrefClick(LanguageUtil.ENGLISH_LANGUAGE_CODE, currentLanguage)
                true
            })

            preferenceRussian.setOnPreferenceClickListener({
                onPrefClick(LanguageUtil.RUSSIAN_LANGUAGE_CODE, currentLanguage)
                true
            })
        }

        fun onPrefClick(languageCode: String, currentLanguage: String) {
            if (currentLanguage != languageCode) {
                LanguageUtil.changeLocale(languageCode, activity)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return true
    }
}
