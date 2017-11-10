package com.xplore.base

import android.app.Activity
import android.content.Context
import com.xplore.settings.LanguageUtil

/**
 * Created by Nik on 8/26/2017.
 * TODO write description of this class - what it does and why.
 */

abstract class BaseActivity : Activity() {

    override fun attachBaseContext(newBase: Context) {
        val context = XploreContextWrapper.wrap(newBase, LanguageUtil.getCurrentLanguage(newBase))
        super.attachBaseContext(context)
    }
}