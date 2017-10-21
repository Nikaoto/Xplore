package com.xplore.intro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import com.xplore.R
import com.xplore.StartingActivity
import com.xplore.account.SignInActivity
import com.xplore.base.XploreContextWrapper
import com.xplore.settings.LanguageUtil

/**
 * Created by Nik on 8/24/2017.
 *
 * This is the welcome/tutorial screen for first time users; This is the first real interaction that
 * the user has with Xplore, so make it count!
 *
 */

class WelcomeActivity : IntroActivity() {

    val TAG = "welcome_act"

    // If this is true, signin act is launched when this activity finishes
    private var shouldOpenSignInAct = false

    override fun attachBaseContext(newBase: Context) {
        val context = XploreContextWrapper.wrap(newBase, LanguageUtil.getCurrentLanguage(newBase))
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        isFullscreen = true
        super.onCreate(savedInstanceState)

        isButtonBackVisible = false
        isButtonNextVisible = true
        buttonNextFunction = BUTTON_NEXT_FUNCTION_NEXT_FINISH

        addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float,
                                        positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position == slides.size - 1) {
                    Log.i(TAG, "last slide")
                    if (StartingActivity.shouldShowWelcomeScreen(this@WelcomeActivity)) {
                        StartingActivity.stopShowingWelcomeScreen(this@WelcomeActivity)
                        shouldOpenSignInAct = true
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        addSlide(SimpleSlide.Builder()
                .image(R.drawable.about_page_banner)
                .title(R.string.welcome)
                .description(R.string.slide1_text)
                .background(R.color.slide1_background)
                .backgroundDark(R.color.slide1_background_dark)
                .scrollable(false)
                .build())

        addSlide(FragmentSlide.Builder()
                .background(R.color.slide2_background)
                .backgroundDark(R.color.slide2_background_dark)
                .fragment(R.layout.slide2_fragment, R.style.Theme_AppCompat_Light)
                .build())

        addSlide(FragmentSlide.Builder()
                .background(R.color.slide3_background)
                .backgroundDark(R.color.slide3_background_dark)
                .fragment(R.layout.slide3_fragment, R.style.Theme_AppCompat_Light)
                .build())

/*        val loginSlide = FragmentSlide.Builder()
                .background(R.color.slide4_background)
                .backgroundDark(R.color.slide4_background_dark)
                .fragment(R.layout.signin, R.style.Theme_AppCompat_Light)
                .canGoForward(General.isUserSignedIn())
                .build()

        addSlide(loginSlide)*/

    }

    // Opens main act if shouldOpenSignInAct
    override fun onDestroy() {
        super.onDestroy()
        if (shouldOpenSignInAct) {
            startActivity(SignInActivity.getStartIntent(this, true))
        }
    }
}