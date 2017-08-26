package com.xplore

import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import com.xplore.base.BaseAppCompatActivity
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

/**
 * Created by Nikaoto on 11/9/2016.
 */

class AboutActivity : BaseAppCompatActivity() {

    private infix fun Resources.string(id: Int) = this.getString(id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.nav_about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val res = resources
        val footerElement = Element().setTitle(res string R.string.happy_hiking)
                .setGravity(Gravity.LEFT)
        val aboutPage = AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.about_page_banner)
                .setDescription(res string R.string.about)
                .addItem(Element().setTitle(res string R.string.version_text))
                .addGroup(res string R.string.contact_us)
                .addWebsite(res string R.string.xplore_website)
                .addFacebook(res string R.string.xplore_facebook)
                .addInstagram(res string R.string.xplore_instagram)
                .addEmail(res string R.string.xplore_email, res string R.string.email)
                .addItem(footerElement)
                .create()
        setContentView(aboutPage)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}