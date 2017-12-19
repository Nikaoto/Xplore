package com.xplore.base

import android.app.Activity
import android.content.Context
import com.xplore.settings.LanguageUtil

/**
 * Created by Nik on 8/26/2017.
 *
 * Base activity class with a context wrapper used throughout the whole application.
 * The context wrapper is used for changing language at runtime.
 *
 */

abstract class BaseActivity : Activity() {

    override fun attachBaseContext(newBase: Context) {
        val context = XploreContextWrapper.wrap(newBase, LanguageUtil.getCurrentLanguage(newBase))
        super.attachBaseContext(context)
    }
}