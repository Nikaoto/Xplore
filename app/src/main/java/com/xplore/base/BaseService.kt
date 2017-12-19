package com.xplore.base

import android.app.Service
import android.content.Context
import com.xplore.settings.LanguageUtil

/**
 * Created by Nika on 12/19/2017.
 *
 * Base service class with a context wrapper used throughout the whole application.
 * The context wrapper is used for changing language at runtime.
 *
 */

abstract class BaseService : Service() {

    override fun attachBaseContext(newBase: Context) {
        val context = XploreContextWrapper.wrap(newBase, LanguageUtil.getCurrentLanguage(newBase))
        super.attachBaseContext(context)
    }

}