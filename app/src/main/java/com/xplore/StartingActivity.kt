package com.xplore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.xplore.account.SignInActivity
import com.xplore.intro.LanguageSelectActivity

/**
 * Created by Nikaoto on 8/26/2017.
 *
 * This is the first activity that opens when Xplore is launched.
 *
 */

class StartingActivity : AppCompatActivity() {

    companion object {
        const val PREFS_BOOT = "prefs_boot"
        const val PREFS_BOOLEAN_FIRST_BOOT = "first_boot"

        // Returns true if first ever app boot
        @JvmStatic
        fun shouldShowWelcomeScreen(context: Context) =
                context.getSharedPreferences(PREFS_BOOT, 0)
                        .getBoolean(PREFS_BOOLEAN_FIRST_BOOT, true)

        @JvmStatic
        fun stopShowingWelcomeScreen(context: Context) =
                 context.getSharedPreferences(StartingActivity.PREFS_BOOT, 0)
                         .edit()
                         .putBoolean(PREFS_BOOLEAN_FIRST_BOOT, false)
                         .apply()
    }

    val TAG = "starting-act"

    private fun start(intent: Intent) {
        startActivity(intent)
        this.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "oncreate startingact")

        General.refreshAccountStatus()

        // Welcome Screen
        if (shouldShowWelcomeScreen(this)) {
            Log.i(TAG, "IS first boot, starting language select")

            start(Intent(this, LanguageSelectActivity::class.java))
            return
        }

        // Sign In
        if (!General.isUserLoggedIn() || !General.isUserFullyRegistered(this)) {
            Log.i(TAG, "NOT first boot; user NOT signed in, starting signin act")

            start(SignInActivity.newIntent(this, true));
            return
        }

        // Main Act
        Log.i(TAG, "NOT first boot; user IS signed in, opening main act")

        start(Intent(this@StartingActivity, MainActivity::class.java))
    }
}
