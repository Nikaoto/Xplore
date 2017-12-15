package com.xplore

import android.app.Activity
import android.app.Fragment
import android.app.FragmentTransaction
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
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
import com.xplore.account.SignInActivity
import com.xplore.base.BaseAppCompatActivity
import com.xplore.database.DBManager
import com.xplore.groups.my.LoadingMyGroupsFragment
import com.xplore.groups.search.SearchGroupsFragment
import com.xplore.maps.BaseMapActivity
import com.xplore.notifications.BadgeDrawerArrowDrawable
import com.xplore.notifications.NotificationUtil
import com.xplore.reserve.LibraryFragment
import com.xplore.settings.LanguageUtil
import com.xplore.settings.SettingsActivity
import com.xplore.user.UserCard
import com.xplore.util.ImageUtil
import kotlinx.android.synthetic.main.activity_main.*

/*
 * Created by Nikaoto on 8/25/2017.
 *
 * The main activity
 *
 */

class MainActivity : BaseAppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val PREFS_LANGUAGE = "prefs_lang"
    private val PREFS_STRING_LANGUAGE = "lang"

    private val drawer: DrawerLayout by lazy {
        findViewById<DrawerLayout>(R.id.drawer_layout)
    }

    private var appJustLaunched = true

    private lateinit var userImageView: ImageView
    private lateinit var userFullNameTextView: TextView

    private lateinit var notificationUtil: NotificationUtil

    private val TAG = " main-act"

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)

        configureDBLanguage()

        setContentView(R.layout.activity_main)

        General.initDisplayMetrics(this)
        General.refreshAccountStatus()

        // Setting up user profile inside drawer header
        val navHeaderView = navigationView.getHeaderView(0)
        userImageView = navHeaderView.findViewById<ImageView>(R.id.drawer_image)
        userFullNameTextView = navHeaderView.findViewById<TextView>(R.id.userFullNameTextView)
        userImageView.setOnClickListener {
            if (General.isUserSignedIn()) {
                General.openUserProfile(this@MainActivity, General.currentUserId)
            } else {
                General.popSignInMenu(0.8, 0.6, currentFocus, this@MainActivity)
            }
        }
        //

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        // Used to hide keyboard when drawer is clicked
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

        // Badge for notifications
        val drawerBadge = BadgeDrawerArrowDrawable(toolbar.context)

        notificationUtil = NotificationUtil(
                drawerBadge,
                navigationView.menu.findItem(R.id.nav_my_groups)
                        .actionView.findViewById<TextView>(R.id.myGroupsBadge) as TextView // TODO test this possible fix
                /*MenuItemCompat.getActionView(navigationView.menu
                        .findItem(R.id.nav_my_groups)).findViewById<TextView>(R.id.myGroupsBadge)*/
        )

        toggle.drawerArrowDrawable = drawerBadge

        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        if (General.isUserSignedIn()) {
            refreshUserProfileViews(this)
        }

        General.hideKeyboard(this)
    }

    private fun configureDBLanguage() {
        val prefs = getSharedPreferences(PREFS_LANGUAGE, 0)
        val currentLanguage = prefs.getString(PREFS_STRING_LANGUAGE, "")
        if (currentLanguage.isEmpty()) {
            // This only executes if LanguageSelectAct failed to write to prefs
            // Set English as language
            prefs.edit().putString(PREFS_STRING_LANGUAGE, LanguageUtil.ENGLISH_LANGUAGE_CODE)
                    .commit()
            DBManager.DB_TABLE = LanguageUtil.ENGLISH_LANGUAGE_CODE
        } else {
            DBManager.DB_TABLE = currentLanguage
        }
    }

    private fun openHomePage() {
        openFragment(SearchGroupsFragment(), R.id.nav_find_create_groups)
    }

    private fun doIfAuthorized(func: () -> Unit) {
        if (General.isUserSignedIn()) {
            return func()
        } else {
            return popLoginMenu()
        }
    }

    private fun openFragment(f: Fragment, navId: Int) {
        navigationView.setCheckedItem(navId)
        fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, f).commit()
    }

    override fun onResume() {
        super.onResume()

        Log.i(TAG, "onresume main act")

        // Refreshes app if language changed
        if (LanguageUtil.languagePrefsChanged) {
            Log.i(TAG, "lang prefs changed, recreating main act")

            LanguageUtil.languagePrefsChanged = false
            recreate()
        }

        // Reloads user related content when new login occurs
        if (General.accountStatus == General.JUST_LOGGED_IN) {
            notificationUtil.reset()
            refreshUserProfileViews(this)
            General.accountStatus = General.LOGGED_IN
        }

        // Disables notification manager and opens homepage if logged out from settings
        if (General.accountStatus == General.NOT_LOGGED_IN && !appJustLaunched) {
            notificationUtil.disable()
            finish()
            startActivity(SignInActivity.newIntent(this, true))
        }

        // Clears user related content when logged out
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

        // Used for testing
        //startActivity(RegistrationActivity.newIntent(this, General.currentUserId, "Nika Oto", "nikaoto999@gmail.com", "https://firebasestorage.googleapis.com/v0/b/xplore-a4aa3.appspot.com/o/users%2FMhNYTmLzuEanTbOTWK8m0Ju6Yf33%2Fprofile_picture.jpg?alt=media&token=5cc495d0-a194-4ec0-be46-0ddf1fba588b"))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            super.onActivityResult(requestCode, resultCode, it)
            if (requestCode == LibraryFragment.REQ_CODE_RESERVE_INFO) {
                val searchDestId = it.getIntExtra(LibraryFragment.ARG_GROUP_SEARCH_DESTINATION_ID,
                        -1)

                if (resultCode == Activity.RESULT_OK) {
                    openFragment(SearchGroupsFragment.newInstance(searchDestId),
                            R.id.nav_find_create_groups)
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.nav_profile -> doIfAuthorized {
                General.openUserProfile(this@MainActivity, General.currentUserId)
            }

            R.id.nav_map -> startActivity(Intent(this, BaseMapActivity::class.java))

            R.id.nav_library -> openFragment(LibraryFragment(), R.id.nav_library)

            R.id.nav_my_groups -> doIfAuthorized {
                openFragment(LoadingMyGroupsFragment(), R.id.nav_my_groups)
            }

            R.id.nav_find_create_groups -> doIfAuthorized {
                openFragment(SearchGroupsFragment(), R.id.nav_find_create_groups)
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
}