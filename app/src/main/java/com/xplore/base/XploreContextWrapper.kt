package com.xplore.base

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

/**
 * Created by Nik on 8/26/2017.
 * Context wrapper for changing locale on API 25+
 */

class XploreContextWrapper(context: Context) : ContextWrapper(context) {
    companion object {
        @JvmStatic
        fun wrap(context: Context, languageCode: String): XploreContextWrapper {
            val res = context.resources
            val config = res.configuration
            val newLocale = Locale(languageCode)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(newLocale)

                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                config.locales = localeList

                return XploreContextWrapper(context.createConfigurationContext(config))
            } else {
                config.setLocale(newLocale)
                return XploreContextWrapper(context.createConfigurationContext(config))
            }
        }
    }
}