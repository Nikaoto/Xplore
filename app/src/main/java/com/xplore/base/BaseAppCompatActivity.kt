package com.xplore.base

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.xplore.settings.LanguageUtil

/**
 * Created by Nik on 8/26/2017.
 * TODO write description of this class - what it does and why.
 */

open class BaseAppCompatActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val context = XploreContextWrapper.wrap(newBase, LanguageUtil.getCurrentLanguage(newBase))
        super.attachBaseContext(context)
    }
}