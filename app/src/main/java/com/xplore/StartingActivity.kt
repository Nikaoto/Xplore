package com.xplore

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.xplore.intro.LanguageSelectActivity

/**
 * Created by Nik on 8/26/2017.
 * TODO write description of this class - what it does and why.
 */

class StartingActivity : AppCompatActivity() {

    private val PREFS_BOOT = "prefs_boot"
    private val PREFS_BOOLEAN_FIRST_BOOT = "first_boot"

    val TAG = "jiga"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "oncreate startingact")

        finish()

        if (isFirstBoot()) {
            Log.i(TAG, "is first boot, starting language select")

            startActivity(Intent(this, LanguageSelectActivity::class.java))
        } else {
            Log.i(TAG, "is not first boot, starting main act")

            startActivity(Intent(this, MainActivityK::class.java))
        }

        //finish()
    }

    //Returns true if first ever app boot and opens intro
    private fun isFirstBoot(): Boolean {
        val bootPrefs = getSharedPreferences(PREFS_BOOT, 0)
        if (bootPrefs.getBoolean(PREFS_BOOLEAN_FIRST_BOOT, true)) {
            bootPrefs.edit().putBoolean(PREFS_BOOLEAN_FIRST_BOOT, true).apply()
            return true
        } else {
            return false
        }
    }
}
