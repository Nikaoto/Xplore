package com.xplore.intro

import android.os.Bundle
import android.os.PersistableBundle
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import com.xplore.R

/**
 * Created by Nik on 8/24/2017.
 *
 * This is the welcome/tutorial screen for first time users; This is the first real interaction that
 * the user has with Xplore, so make it count! (First impressions'n all, y'know)
 *
 */

class WelcomeActivity : IntroActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        isFullscreen = true
        super.onCreate(savedInstanceState)

        isButtonBackVisible = false
        isButtonNextVisible = true
        buttonNextFunction = BUTTON_NEXT_FUNCTION_NEXT_FINISH

        addSlide(SimpleSlide.Builder()
                .image(R.drawable.about_page_banner)
                .title(R.string.welcome)
                .description(R.string.screen1)
                .background(R.color.wallet_holo_blue_light)
                .backgroundDark(R.color.material_blue_grey_900)
                .scrollable(false)
                .build())
    }
}