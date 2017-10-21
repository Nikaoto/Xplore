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


        //Returns true if first ever app boot
        @JvmStatic
        fun shouldShowWelcomeScreen(context: Context) =
                context.getSharedPreferences(PREFS_BOOT, 0)
                        .getBoolean(PREFS_BOOLEAN_FIRST_BOOT, true)

        @JvmStatic
        fun stopShowingWelcomeScreen(context: Context) =
                 context.getSharedPreferences(StartingActivity.PREFS_BOOT, 0)
                         .edit()
                         .putBoolean(PREFS_BOOLEAN_FIRST_BOOT, false)
                         .commit()
    }

    val TAG = "jiga"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "oncreate startingact")

        finish()

        General.refreshAccountStatus()

        if (shouldShowWelcomeScreen(this)) {
            Log.i(TAG, "is first boot, starting language select")

            startActivity(Intent(this, LanguageSelectActivity::class.java))
        } else if (!General.isUserLoggedIn()) {
            Log.i(TAG, "is not first boot and user not signed in, starting signing act")

            startActivity(SignInActivity.getStartIntent(this, true))
        } else {
            Log.i(TAG, "is not first boot and user is signed in, starting main act")

            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
