package com.xplore.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xplore.util.DateUtil
import com.xplore.General
import com.xplore.util.ImageUtil
import com.xplore.R
import com.xplore.account.EditProfileActivity
import com.xplore.base.refreshable.RefreshableActivity
import com.xplore.util.FirebaseUtil.usersRef
import kotlinx.android.synthetic.main.user_profile.*

/**
 * Created by Nikaoto on 7/5/2017.
 *
 * აღწერა:
 * ეს მოქმედება აჩვენებს ნებისმიერი მომხმარებლის პროფილის გვერდს მისი ID-ს მიხედვით.
 * ინტენტიდან იღებს "userId" სტრინგს.
 *
 * Description:
 * This activity displays any user's profile page with the given ID.
 * Receives "userId" string from intent.
 *
 */

class UserProfileActivity : RefreshableActivity() {

    companion object {

        private const val ARG_USER_ID = "userId"

        @JvmStatic
        fun newIntent(context: Context, userId: String): Intent {
            return Intent(context, UserProfileActivity::class.java)
                    .putExtra(ARG_USER_ID, userId)
        }
    }

    private val userId: String by lazy { intent.getStringExtra(ARG_USER_ID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initRefreshLayout(findViewById<SwipeRefreshLayout>(R.id.refreshLayout), true)
        setLoading(true)

        fetchUserInfo(userId)
    }

    private fun fetchUserInfo(userId: String){
        val query = usersRef.child(userId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    val tempUser = dataSnapshot.getValue(User::class.java)
                    if (tempUser != null) {
                        tempUser.id = dataSnapshot.key
                        displayUserInfo(tempUser)

                        // If user viewing his/her profile
                        if (userId == General.currentUserId) {
                            configureEditProfileButton(tempUser)
                        }
                    } else { printError() }
                } else { printError() }
            }

            override fun onCancelled(p0: DatabaseError?) { }
        })
    }

    private fun displayUserInfo(user: User){
        // Remove small profile image from cache
        Picasso.with(this).invalidate(user.profile_picture_url)
        // Load profile picture
        Picasso.with(this)
                .load(user.profile_picture_url)
                .transform(ImageUtil.largeCircle(this))
                .placeholder(R.drawable.picasso_load_anim)
                .into(userImageView)

        // Load textual data
        fullNameTextView.text = user.getFullName()
        reputationCombinedTextView.text = user.reputation.toString() +
                " "+ resources.getString(R.string.reputation)
        birthDateTextView.text = DateUtil.putSlashesInDate(user.birth_date)
        telephoneTextView.text = user.tel_num
        emailTextView.text = user.email

        onFinishedLoading()
    }

    // Called when all layout and data loading is finished
    private fun onFinishedLoading() {
        setLoading(false)
    }

    private fun configureEditProfileButton(user: User) {
        editProfileButton.visibility = View.VISIBLE
        editProfileButton.setOnClickListener {
            startActivity(EditProfileActivity.getStartIntent(this@UserProfileActivity, user))
        }
    }

    fun printError() {
        Toast.makeText(this@UserProfileActivity, R.string.error, Toast.LENGTH_SHORT).show()
    }

    override fun onRefreshed() {
        super.onRefreshed()

        val intent = intent
        finish()
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}