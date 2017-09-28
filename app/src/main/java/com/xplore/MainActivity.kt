package com.xplore

import android.app.FragmentTransaction
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xplore.base.BaseAppCompatActivity
import com.xplore.database.DBManager
import com.xplore.groups.my.LoadingMyGroupsFragment
import com.xplore.groups.search.SearchGroupsFragment
import com.xplore.maps.BaseMapActivity
import com.xplore.notifications.BadgeDrawerArrowDrawable
import com.xplore.notifications.NotificationManager
import com.xplore.reserve.LibraryFragment
import com.xplore.settings.LanguageUtil
import com.xplore.settings.SettingsActivity
import com.xplore.user.UserCard
import com.xplore.util.ImageUtil
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by Nik on 8/25/2017.
 * Replacement for MainAct in Kotlin
 */

class MainActivity : BaseAppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val PREFS_LANGUAGE = "prefs_lang"
    private val PREFS_STRING_LANGUAGE = "lang"

    private val ENGLISH_LANGUAGE_CODE = "en"
    private val GEORGIAN_LANGUAGE_CODE = "ka"
    private val RUSSIAN_LANGUAGE_CODE = "ru"

    private val drawer: DrawerLayout by lazy {
        findViewById(R.id.drawer_layout) as DrawerLayout
    }

    private var appJustLaunched = true

    private lateinit var userImageView: ImageView
    private lateinit var userFullNameTextView: TextView

    private lateinit var notificationManager: NotificationManager

    val TAG = "jiga"

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)

        configureDBLanguage()

        setContentView(R.layout.activity_main)

        General.InitDisplayMetrics(this)
        General.refreshAccountStatus()

        //Setting up navigation view
        navigationView.setCheckedItem(R.id.nav_library)

        //Setting up user profile inside drawer header
        val navHeaderView = navigationView.getHeaderView(0)
        userImageView = navHeaderView.findViewById(R.id.drawer_image) as ImageView
        userFullNameTextView = navHeaderView.findViewById(R.id.userFullNameTextView) as TextView
        userImageView.setOnClickListener {
            if (General.isUserSignedIn()) {
                General.openUserProfile(this@MainActivity, General.currentUserId)
            } else {
                General.popSignInMenu(0.8, 0.6, currentFocus, this@MainActivity)
            }
        }
        //

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        //Used to hide keyboard when drawer is clicked
        val toggle = object : ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            override fun onDrawerClosed(drawerView: View?) {
                super.onDrawerClosed(drawerView)
                hideKeyboard()
            }

            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
                hideKeyboard()
            }
        }

        //TODO create a timer which checks for notifs every 30s and gives notification + updates badges

        //Badge for notifications
        val drawerBadge = BadgeDrawerArrowDrawable(toolbar.context)

        notificationManager = NotificationManager(
                drawerBadge,
                MenuItemCompat.getActionView(navigationView.getMenu()
                        .findItem(R.id.nav_my_groups)).findViewById(R.id.myGroupsBadge) as TextView
        )

        toggle.drawerArrowDrawable = drawerBadge

        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        if (General.isUserSignedIn()) {
            refreshUserProfileViews(this)
        }

        openHomePage()
        General.hideKeyboard(this)
    }

    private fun configureDBLanguage() {
        val prefs = getSharedPreferences(PREFS_LANGUAGE, 0)
        val currentLanguage = prefs.getString(PREFS_STRING_LANGUAGE, "")
        if (currentLanguage.isEmpty()) {
            //This only executes if LanguageSelectAct failed to write to prefs
            //Set English as language
            prefs.edit().putString(PREFS_STRING_LANGUAGE, ENGLISH_LANGUAGE_CODE).commit()
            DBManager.DB_TABLE = ENGLISH_LANGUAGE_CODE
        } else {
            DBManager.DB_TABLE = currentLanguage
        }
    }

    private fun openHomePage() {
        navigationView.setCheckedItem(R.id.nav_iliauni_library)
        fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, IliauniFragment()).commit()
        //TODO^ iliauni: change to LibraryFragment() and nav_library after event ends
    }

    override fun onResume() {
        super.onResume()

        Log.i(TAG, "onresume main act")

        //Refreshes app if language changed
        if (LanguageUtil.languagePrefsChanged) {
            Log.i(TAG, "lang prefs changed, recreating main act")

            LanguageUtil.languagePrefsChanged = false
            recreate()
        }

        //Reloads user related content when new login occurs
        if (General.accountStatus == General.JUST_LOGGED_IN) {
            notificationManager.reset()
            refreshUserProfileViews(this)
            General.accountStatus = General.LOGGED_IN
        }

        //Disables notification manager and opens homepage if logged out / not logged in
        if (General.accountStatus == General.NOT_LOGGED_IN && !appJustLaunched) {
            notificationManager.disable()
            openHomePage()
        }

        //Clears user related content whe logged out
        if (!General.isUserSignedIn()) {
            userFullNameTextView.visibility = View.GONE
            Picasso.with(this)
                    .load(R.drawable.user_default_profile_image)
                    .transform(ImageUtil.mediumCircle(this))
                    .into(userImageView);
        }

        if (appJustLaunched) {
            appJustLaunched = false
            openHomePage()
        }
    }

    private fun refreshUserProfileViews(context: Context?) {
        FirebaseDatabase.getInstance().getReference("users").child(General.currentUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        context?.let {
                            if (dataSnapshot != null) {
                                val tempUser = dataSnapshot.getValue(UserCard::class.java)
                                if (tempUser != null) {
                                    Picasso.with(it)
                                            .load(tempUser.profile_picture_url)
                                            .transform(ImageUtil.mediumCircle(it))
                                            .placeholder(R.drawable.picasso_load_anim)
                                            .into(userImageView)

                                    userFullNameTextView.visibility = View.VISIBLE
                                    userFullNameTextView.text = tempUser.getFullName()
                                }
                            } else {
                                Toast.makeText(it, R.string.error, Toast.LENGTH_SHORT)
                                        .show()
                            }
                        }
                    }
                    override fun onCancelled(p0: DatabaseError?) {}
                })
    }

    private fun hideKeyboard() = General.hideKeyboard(this@MainActivity)

    private fun popLoginMenu() = General.popSignInMenu(0.8, 0.6, currentFocus, this)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.nav_profile ->
                if (General.isUserSignedIn()) {
                    General.openUserProfile(this@MainActivity, General.currentUserId)
                } else {
                    popLoginMenu()
                }

            R.id.nav_map -> startActivity(Intent(this, BaseMapActivity::class.java))

            R.id.nav_library -> {
                navigationView.setCheckedItem(R.id.nav_library)
                fragmentManager.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container, LibraryFragment()).commit()
            }

            R.id.nav_iliauni_library -> {
                navigationView.setCheckedItem(R.id.nav_iliauni_library)
                fragmentManager.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container, IliauniFragment()).commit()
            }

            R.id.nav_my_groups ->
                if(General.isUserSignedIn()) {
                    fragmentManager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(R.id.fragment_container, LoadingMyGroupsFragment()).commit()
                } else {
                    popLoginMenu()
                }

            R.id.nav_find_create_groups ->
                if (General.isUserSignedIn()) {
                    fragmentManager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(R.id.fragment_container, SearchGroupsFragment()).commit()
                } else {
                    popLoginMenu()
                }

            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))

            R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java))
        }

        fragmentManager.executePendingTransactions()
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            openHomePage()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        hideKeyboard()
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)

    }


    //TODO check this and remove
    override fun onSaveInstanceState(outState: Bundle?) {
        //Leave empty. Bug on API 11+
    }
}