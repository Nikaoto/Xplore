package com.xplore.intro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.xplore.R
import com.xplore.base.BaseActivity
import com.xplore.settings.LanguageUtil
import kotlinx.android.synthetic.main.intro_language_select.*

/**
 * Created by Nik on 8/25/2017.
 *
 * Language selection on first boot
 *
 */

class LanguageSelectActivity : BaseActivity() {

    private fun startWelcomeActivity() {
        Log.i(TAG, "language selected, starting main act")
        finish()
        startActivity(Intent(this, WelcomeActivity::class.java))
    }

    val TAG = "jiga"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_language_select)

        Log.i(TAG, "oncreate language select")
        //Georgian Language selected
        georgianCard.setOnClickListener {
            LanguageUtil.changeLocale(LanguageUtil.GEORGIAN_LANGUAGE_CODE, this)
            startWelcomeActivity()
        }

        //English Language selected
        englishCard.setOnClickListener {
            LanguageUtil.changeLocale(LanguageUtil.ENGLISH_LANGUAGE_CODE, this)
            startWelcomeActivity()
        }

        //Russian Language selected
        russianCard.setOnClickListener {
            LanguageUtil.changeLocale(LanguageUtil.RUSSIAN_LANGUAGE_CODE, this)
            startWelcomeActivity()
        }
    }
}
