package com.xplore.settings

import android.app.Activity
import android.content.Context

/**
 * Created by Nik on 8/25/2017.
 * Language Utilities
 */

class LanguageUtil {
    companion object {
        @JvmField
        var languagePrefsChanged = false

        const val PREFS_LANGUAGE = "prefs_lang"
        const val PREFS_STRING_LANGUAGE = "lang"

        const val ENGLISH_LANGUAGE_CODE = "en"
        const val GEORGIAN_LANGUAGE_CODE = "ka"
        const val RUSSIAN_LANGUAGE_CODE = "ru"

        @JvmStatic
        fun changeLocale(languageCode: String, activity: Activity) {
            //Updating language in preferences
            val preferences = activity.getSharedPreferences(PREFS_LANGUAGE, 0)
            preferences.edit()
                    .putString(PREFS_STRING_LANGUAGE, languageCode)
                    .commit()

/*            //Updating locale
            val res = context.resources
            val dm = res.displayMetrics
            val config = res.configuration
            val locale = Locale(languageCode)
            config.setLocale(locale)
            res.updateConfiguration(config, dm)
            //Locale.setDefault(locale)*/

            languagePrefsChanged = true
            activity.finish()
        }

        @JvmStatic
        fun getCurrentLanguage(context: Context): String {
            return context.getSharedPreferences(PREFS_LANGUAGE, 0)
                    .getString(PREFS_STRING_LANGUAGE, ENGLISH_LANGUAGE_CODE)
        }
    }
}